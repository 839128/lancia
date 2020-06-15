import puppeteer             from 'puppeteer';
import _                     from 'lodash';
import _C                    from '../../shared/term/criteria.term.class';

export default class Render {

    static async init() {
        const browser = await puppeteer.launch({
            headless: config.debug,
            ignoreHTTPSErrors: config.ignore,
            devtools: false,
            args: [
                '--disable-gpu',
                '--disable-dev-shm-usage',
                '--disable-setuid-sandbox',
                '--no-first-run',
                '--no-sandbox',
                '--no-zygote',
                '--single-process',
                "--proxy-server='direct://'",
                '--proxy-bypass-list=*',
            ],
            sloMo: config.debug ? 250 : undefined
        });
        return browser.wsEndpoint();
    }

    /**
     * 根据参数信息，获取内容，生成文件
     *
     * @param {_opts} options.
     */
    static async render(_opts = {}) {
        const opts = _.merge({
            cookies: [],
            scrollPage: false,
            emulateScreenMedia: true,
            ignoreHttpsErrors: false,
            html: null,
            viewport: {
                width: 1600,
                height: 1200
            },
            goto: {
                waitUntil: 'networkidle0'
            },
            output: _C.FOTMAT_TYPE_PDF,
            pdf: {
                format: 'A4',
                printBackground: true
            },
            screenshot: {
                type: 'png',
                fullPage: true
            },
            failEarly: false,
            waitFor: 6000
        }, _opts);

        if (_.get(_opts, 'pdf.width') && _.get(_opts, 'pdf.height')) {
            // pdf.format 会覆盖宽度和高度，所以我们必须删除它
            opts.pdf.format = undefined;
        }

        this.logOpts(opts);

        let browserWSEndpoint = browser[Math.floor(Math.random() * config.size)];
        const chrome = await puppeteer.connect({browserWSEndpoint,ping_interval:'None',ping_timeout:'None'});
        logger.trace('RENDER => browserWSEndpoint  connect ...'+ Math.floor(Math.random() * config.size));
        const page = await chrome.newPage();

        await page.setCacheEnabled(true);

        page.on('error', (err) => {
            logger.error(`RENDER => Error event emitted: ${err}`);
            logger.error(err.stack);
            page.close();
        });

        this.failedResponses = [];
        page.on('requestfailed', (request) => {
            this.failedResponses.push(request);
            if (request.url === opts.url) {
                this.mainUrlResponse = request;
            }
        });

        page.on('response', (response) => {
            if (response.status >= 400) {
                this.failedResponses.push(response);
            }

            if (response.url === opts.url) {
                this.mainUrlResponse = response;
            }
        });

        let data;
        try {
            logger.trace('RENDER => Set browser viewport ..');
            await page.setViewport(opts.viewport);
            if (opts.emulateScreenMedia) {
                logger.trace('RENDER => Emulate @media screen ..');
                await page.emulateMedia('screen');
            }

            if (opts.cookies && opts.cookies.length > 0) {
                logger.trace('RENDER => Setting cookies ..');

                const client = await page.target().createCDPSession();

                await client.send('Network.enable');
                await client.send('Network.setCookies', {cookies: opts.cookies});
            }

            if (_.isString(opts.html)) {
                logger.trace('RENDER => Set HTML ..');
                await page.setContent(opts.html, opts.goto);
            } else {
                logger.trace(`RENDER => Goto url ${opts.url} ..`);
                await page.goto(opts.url, opts.goto);
            }

            opts.attachmentName = await page.title();

            if (_.isNumber(opts.waitFor) || _.isString(opts.waitFor)) {
                logger.trace(`RENDER => Wait for ${opts.waitFor} ..`);
                await page.waitFor(~~opts.waitFor);
            }

            if (opts.scrollPage) {
                logger.trace('RENDER => Scroll page ..');
                await this.scrollPage(page);
            }

            if (this.failedResponses.length) {
                logger.warn(`Number of failed requests: ${this.failedResponses.length}`);
                this.failedResponses.forEach((response) => {
                    logger.warn(`${response.status} ${response.url}`);
                });

                if (opts.failEarly === 'all') {
                    const err = new Error(`${this.failedResponses.length} requests have failed. See server log for more details.`);
                    err.status = 412;
                    throw err;
                }
            }
            if (opts.failEarly === 'page' && this.mainUrlResponse.status !== 200) {
                const msg = `Request for ${opts.url} did not directly succeed and returned status ${this.mainUrlResponse.status}`;
                const err = new Error(msg);
                err.status = 412;
                throw err;
            }
            logger.trace('RENDER => Rendering ..');

            let content = await page.content();
            logger.trace(`RENDER <= Content size :${content.toString().length}`);

            if (opts.output === _C.FOTMAT_TYPE_PDF) {
                data = await page.pdf(opts.pdf);
            }else if (opts.output === _C.FOTMAT_TYPE_HTML) {
                data = await page.evaluate(() => document.body.innerHTML);
            } else {
                const screenshotOpts = _.cloneDeep(_.omit(opts.screenshot, ['clip']));
                const clipContainsSomething = _.some(opts.screenshot.clip, val => !_.isUndefined(val));
                if (clipContainsSomething) {
                    screenshotOpts.clip = opts.screenshot.clip;
                }
                data = await page.screenshot(screenshotOpts);
            }
        } catch (err) {
            logger.trace(`RENDER <= Error when rendering page: ${err}`);
            logger.error(err.stack);
            throw err;
        } finally {
            logger.trace('RENDER <= Closing page..');
            await page.close();
        }
        return data;
    }

    static async scrollPage(page) {
        await page.evaluate(() => {
            const scrollInterval = 100;
            const scrollStep = Math.floor(window.innerHeight / 2);
            const bottomThreshold = 400;

            function bottomPos() {
                return window.pageYOffset + window.innerHeight;
            }

            return new Promise((resolve, reject) => {
                function scrollDown() {
                    window.scrollBy(0, scrollStep);

                    if (document.body.scrollHeight - bottomPos() < bottomThreshold) {
                        window.scrollTo(0, 0);
                        setTimeout(resolve, 500);
                        return;
                    }

                    setTimeout(scrollDown, scrollInterval);
                }

                setTimeout(reject, 30000);
                scrollDown();
            });
        });
    }


    /**
     * 获取媒体类型
     *
     * @param {opts} optinos.
     */
    static getMimeType(opts) {
        if (opts.output === _C.FOTMAT_TYPE_PDF) {
            return 'application/pdf';
        }else if (opts.output === _C.FOTMAT_TYPE_HTML) {
            return 'text/html';
        }
        const type = _.get(opts, 'screenshot.type');
        switch (type) {
            case 'png':
                return 'image/png';
            case 'jpeg':
                return 'image/jpeg';
            default:
                throw new Error(`Unknown screenshot type: ${type}`);
        }
    }

    static logOpts(opts) {
        const supressedOpts = _.cloneDeep(opts);
        if (opts.html) {
            supressedOpts.html = '...';
        }
        logger.trace('RENDER => ' + JSON.stringify({
            opts
        }));
    }

    /**
     * 获取提交参数信息
     *
     * @param {query} request.
     */
    static getOptsFromQuery(query) {
        const opts = {
            url: query.url,
            attachmentName: query.attachmentName,
            scrollPage: query.scrollPage,
            emulateScreenMedia: query.emulateScreenMedia,
            ignoreHttpsErrors: query.ignoreHttpsErrors,
            waitFor: ~~query.waitFor,
            output: query.output || _C.FOTMAT_TYPE_PDF || _C.FOTMAT_TYPE_HTML,
            viewport: {
                width: query['viewport.width'],
                height: query['viewport.height'],
                deviceScaleFactor: query['viewport.deviceScaleFactor'],
                isMobile: query['viewport.isMobile'],
                hasTouch: query['viewport.hasTouch'],
                isLandscape: query['viewport.isLandscape']
            },
            goto: {
                timeout: query['goto.timeout'],
                waitUntil: query['goto.waitUntil'],
                networkIdleInflight: query['goto.networkIdleInflight'],
                networkIdleTimeout: query['goto.networkIdleTimeout']
            },
            pdf: {
                scale: query['pdf.scale'],
                displayHeaderFooter: query['pdf.displayHeaderFooter'],
                footerTemplate: query['pdf.footerTemplate'],
                headerTemplate: query['pdf.headerTemplate'],
                landscape: Boolean(query['pdf.landscape']),
                pageRanges: query['pdf.pageRanges'],
                format: query['pdf.format'],
                width: query['pdf.width'],
                height: query['pdf.height'],
                margin: {
                    top: query['pdf.margin.top'],
                    right: query['pdf.margin.right'],
                    bottom: query['pdf.margin.bottom'],
                    left: query['pdf.margin.left']
                },
                printBackground: query['pdf.printBackground']
            },
            screenshot: {
                fullPage: query['screenshot.fullPage'],
                quality: query['screenshot.quality'],
                type: query['screenshot.type'] || 'png',
                clip: {
                    x: query['screenshot.clip.x'],
                    y: query['screenshot.clip.y'],
                    width: query['screenshot.clip.width'],
                    height: query['screenshot.clip.height']
                },
                omitBackground: query['screenshot.omitBackground']
            }
        };
        return opts;
    }

}