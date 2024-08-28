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
package org.miaixz.lancia.options;

public class ScreenshotOptions {

    boolean optimizeForSpeed = false;

    /**
     * 三种样式 'png' | 'jpeg' | 'webp';
     */
    String type = "png";
    /**
     * Quality of the image, between 0-100.png不适用quality.
     */
    int quality;
    /**
     * 从表面捕获屏幕截图，而不是从视图捕获屏幕截图。
     *
     * &#064;defaultValue `true`
     */
    boolean fromSurface = true;
    /**
     * 当 true 时，截取整个页面的屏幕截图。
     */
    boolean fullPage = false;
    /**
     * 隐藏默认的白色背景并允许捕获透明的屏幕截图。
     */
    boolean omitBackground = false;

    /**
     * 要将图像保存到的文件路径。屏幕截图类型将从文件扩展名推断出来。如果 path 是相对路径，则相对于当前工作目录进行解析。如果未提供路径，则映像不会保存到磁盘。
     */
    String path;

    /**
     * 指定要剪辑的页面/元素的区域。
     */
    ClipOverwrite clip;
    /**
     * 图片的编码。'base64' | 'binary'
     */
    String encoding = "binary";

    /**
     * 捕获视口之外的屏幕截图。 如果没有 clip，则为 false。否则为 true
     */
    boolean captureBeyondViewport = true;

    public ScreenshotOptions() {
        super();
    }

    public ScreenshotOptions(String path) {
        this.path = path;
    }

    public ScreenshotOptions(String type, int quality, boolean optimizeForSpeed, boolean fromSurface,
            boolean omitBackground, boolean fullPage, String path, ClipOverwrite clip, String encoding,
            boolean captureBeyondViewport) {
        this.type = type;
        this.quality = quality;
        this.optimizeForSpeed = optimizeForSpeed;
        this.fromSurface = fromSurface;
        this.omitBackground = omitBackground;
        this.fullPage = fullPage;
        this.path = path;
        this.clip = clip;
        this.encoding = encoding;
        this.captureBeyondViewport = captureBeyondViewport;
    }

    public boolean getOptimizeForSpeed() {
        return optimizeForSpeed;
    }

    public void setOptimizeForSpeed(boolean optimizeForSpeed) {
        this.optimizeForSpeed = optimizeForSpeed;
    }

    public boolean getFromSurface() {
        return fromSurface;
    }

    public void setFromSurface(boolean fromSurface) {
        this.fromSurface = fromSurface;
    }

    public boolean getCaptureBeyondViewport() {
        return captureBeyondViewport;
    }

    public void setCaptureBeyondViewport(boolean captureBeyondViewport) {
        this.captureBeyondViewport = captureBeyondViewport;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getFullPage() {
        return fullPage;
    }

    public void setFullPage(boolean fullPage) {
        this.fullPage = fullPage;
    }

    public ClipOverwrite getClip() {
        return clip;
    }

    public void setClip(ClipOverwrite clip) {
        this.clip = clip;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean getOmitBackground() {
        return omitBackground;
    }

    public void setOmitBackground(boolean omitBackground) {
        this.omitBackground = omitBackground;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
