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
package org.miaixz.lancia.nimble.page;

import java.util.List;

import org.miaixz.lancia.option.data.AdFrameStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Information about the Frame on the page.
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FramePayload {

    /**
     * Frame unique identifier.
     */
    private String id;

    /**
     * Parent frame identifier.
     */
    private String parentId;
    /**
     * Identifier of the loader associated with this frame.
     */
    private String loaderId;
    /**
     * Frame's name as specified in the tag.
     */
    private String name;
    /**
     * Frame document's URL without fragment.
     */
    private String url;
    /**
     * Frame document's URL fragment including the '#'.
     */
    private String urlFragment;
    /**
     * Frame document's registered domain, taking the public suffixes list into account. Extracted from the Frame's url.
     * Example URLs: http://www.google.com/file.html -> "google.com" http://a.b.co.uk/file.html -> "b.co.uk"
     */
    private String domainAndRegistry;
    /**
     * Frame document's security origin.
     */
    private String securityOrigin;
    /**
     * Frame document's mimeType as determined by the browser.
     */
    private String mimeType;
    /**
     * If the frame failed to load, this contains the URL that could not be loaded. Note that unlike url above, this URL
     * may contain a fragment.
     */
    private String unreachableUrl;
    /**
     * Indicates whether this frame was tagged as an ad and why.
     */
    private AdFrameStatus adFrameStatus;
    /**
     * Indicates whether the main document is a secure context and explains why that is the case. ('Secure' |
     * 'SecureLocalhost' | 'InsecureScheme' | 'InsecureAncestor')
     */
    private String secureContextType;
    /**
     * Indicates whether this is a cross origin isolated context. ('SharedArrayBuffers' |
     * 'SharedArrayBuffersTransferAllowed' | 'PerformanceMeasureMemory' | 'PerformanceProfile')
     */
    private String crossOriginIsolatedContextType;
    /**
     * Indicated which gated APIs / features are available. ('SharedArrayBuffers' | 'SharedArrayBuffersTransferAllowed'
     * | 'PerformanceMeasureMemory' | 'PerformanceProfile')
     */
    private List<String> gatedAPIFeatures;

}
