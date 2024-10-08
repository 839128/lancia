/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.lancia.kernel.browser;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.lancia.Builder;

import com.sun.jna.Platform;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Revision {

    private String revision;
    /**
     * 介质路径
     */
    private String executablePath;
    /**
     * 文件路径
     */
    private String folderPath;
    /**
     * 是否本地
     */
    private boolean local;
    /**
     * 访问URL
     */
    private String url;
    /**
     * 介质信息
     */
    private String product;
    /**
     * 介质运行平台
     */
    private String platform;

    public Revision() {

    }

    public Revision(String revision, String executablePath, String folderPath, boolean local, String url,
            String product) {
        this.revision = revision;
        this.executablePath = executablePath;
        this.folderPath = folderPath;
        this.local = local;
        this.url = url;
        this.product = product;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        if (StringKit.isNotEmpty(platform)) {
            this.platform = platform;
            return;
        }
        if (platform == null) {
            if (com.sun.jna.Platform.isMac())
                this.platform = com.sun.jna.Platform.is64Bit() ? Builder.MAC_ARM64 : Builder.MAC_X64;
            else if (com.sun.jna.Platform.isLinux())
                this.platform = Builder.LINUX;
            else if (com.sun.jna.Platform.isWindows())
                this.platform = Platform.is64Bit() ? Builder.WIN64 : Builder.WIN32;
            Assert.notNull(this.platform, "Unsupported platform: " + Builder.platform());
        }
    }
}
