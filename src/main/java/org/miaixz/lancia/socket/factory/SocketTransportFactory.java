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
package org.miaixz.lancia.socket.factory;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.lancia.socket.PipeTransport;
import org.miaixz.lancia.socket.SocketTransport;
import org.miaixz.lancia.socket.Transport;
/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SocketTransportFactory {

    /**
     * 创建套接字传输协议
     *
     * @param browserWSEndpoint 连接websocket的地址
     * @return WebSocketTransport/PipeTransport 客户端
     */
    public static Transport of(String browserWSEndpoint) {
        try {
            return socket(browserWSEndpoint);
        } catch (InternalException | InterruptedException e) {
            Logger.warn(e.getMessage());
            return pipe(browserWSEndpoint);
        }
    }

    /**
     * create websocket client
     *
     * @param browserWSEndpoint 连接websocket的地址
     * @return SocketTransport websocket客户端
     * @throws InterruptedException 被打断异常
     */
    public static Transport socket(String browserWSEndpoint) throws InterruptedException {
        SocketTransport client = new SocketTransport(browserWSEndpoint);
        // 保持websokcet连接
        client.setConnectionLostTimeout(0);
        client.connectBlocking();
        return client;
    }

    /**
     * 创建套接字传输协议
     */
    public static Transport pipe(String browserWSEndpoint) {
        return new PipeTransport();
    }

}
