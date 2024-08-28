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

import java.util.ArrayList;
import java.util.List;

public class BrowserLaunchArgumentOptions extends Timeoutable {

    private boolean headlessShell;
    /**
     * 是否是无厘头 默认是 true
     */
    private boolean headless = true;
    /**
     * 其他参数，点击 <a href="https://peter.sh/experiments/chromium-command-line-switches/">这里 </a>可以看到参数 <br/>
     */
    private List<String> args = new ArrayList<>();
    /**
     * 用户数据存储的目录 Path to a User Data Directory.
     */
    private String userDataDir;
    /**
     * 是否打开devtool,也就是F12打开的开发者工具
     */
    private boolean devtools;
    /**
     * Specify the debugging port number to use
     */
    private int debuggingPort;

    public boolean getHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getUserDataDir() {
        return userDataDir;
    }

    public void setUserDataDir(String userDataDir) {
        this.userDataDir = userDataDir;
    }

    public boolean getDevtools() {
        return devtools;
    }

    public void setDevtools(boolean devtools) {
        this.devtools = devtools;
    }

    public int getDebuggingPort() {
        return debuggingPort;
    }

    public void setDebuggingPort(int debuggingPort) {
        this.debuggingPort = debuggingPort;
    }

    public boolean getHeadlessShell() {
        return headlessShell;
    }

    public void setHeadlessShell(boolean headlessShell) {
        this.headlessShell = headlessShell;
    }

}