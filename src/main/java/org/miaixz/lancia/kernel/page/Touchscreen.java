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
package org.miaixz.lancia.kernel.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.lancia.socket.CDPSession;

/**
 * 屏幕触摸
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Touchscreen {

    private CDPSession client;

    private Keyboard keyboard;

    public Touchscreen(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
    }

    public void tap(int x, int y) {
        // Touches appear to be lost during the first frame after navigation.
        // This waits a frame before sending the tap.
        // @see https://crbug.com/613219
        Map<String, Object> params = new HashMap<>();
        params.put("expression", "new Promise(x => requestAnimationFrame(() => requestAnimationFrame(x)))");
        params.put("awaitPromise", true);
        this.client.send("Runtime.evaluate", params);

        TouchPoint touchPoint = new TouchPoint(x, y);
        List<TouchPoint> touchPoints = new ArrayList<>();
        touchPoints.add(touchPoint);
        params.clear();
        params.put("type", "touchStart");
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);

        params.clear();
        params.put("type", "touchEnd");
        params.put("touchPoints", new ArrayList<>());
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);
    }

    static class TouchPoint {

        private long x;

        private long y;

        public TouchPoint(long x, long y) {
            this.x = Math.round(x);
            this.y = Math.round(y);
        }

        public long getX() {
            return x;
        }

        public void setX(long x) {
            this.x = x;
        }

        public long getY() {
            return y;
        }

        public void setY(long y) {
            this.y = y;
        }
    }

}
