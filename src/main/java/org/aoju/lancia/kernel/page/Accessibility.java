/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.lancia.kernel.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.aoju.lancia.nimble.accessbility.SerializedAXNode;
import org.aoju.lancia.worker.CDPSession;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Accessibility {

    private final CDPSession client;

    public Accessibility(CDPSession client) {
        this.client = client;
    }

    public SerializedAXNode snapshot(boolean interestingOnly, ElementHandle root) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        JSONObject nodes = this.client.send("Accessibility.getFullAXTree", null, false);
        String backendNodeId = null;
        if (root != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("objectId", root.getRemoteObject().getObjectId());
            JSONObject node = this.client.send("DOM.describeNode", params, true);
            backendNodeId = node.getString("backendNodeId");
        }
        List<JSONObject> list = nodes.toJavaObject(new TypeReference<List<JSONObject>>() {
        });
        Iterator<JSONObject> elements = list.iterator();
        List<org.aoju.lancia.nimble.accessbility.AXNode> payloads = new ArrayList<>();
        while (elements.hasNext()) {
            payloads.add(JSON.toJavaObject(elements.next(), org.aoju.lancia.nimble.accessbility.AXNode.class));
        }
        AXNode defaultRoot = AXNode.createTree(payloads);
        AXNode needle = defaultRoot;
        if (StringKit.isNotEmpty(backendNodeId)) {
            String finalBackendNodeId = backendNodeId;
            needle = defaultRoot.find(node -> finalBackendNodeId.equals(node.getPayload().getBackendDOMNodeId() + ""));
            if (needle == null)
                return null;
        }
        if (!interestingOnly)
            return serializeTree(needle, null).get(0);

        Set<AXNode> interestingNodes = new HashSet<>();
        collectInterestingNodes(interestingNodes, defaultRoot, false);
        if (!interestingNodes.contains(needle))
            return null;
        return serializeTree(needle, interestingNodes).get(0);
    }

    private void collectInterestingNodes(Set<AXNode> collection, AXNode node, boolean insideControl) {
        if (node.isInteresting(insideControl))
            collection.add(node);
        if (node.isLeafNode())
            return;
        insideControl = insideControl || node.isControl();
        for (AXNode child :
                node.getChildren())
            collectInterestingNodes(collection, child, insideControl);
    }

    public List<SerializedAXNode> serializeTree(AXNode node, Set<AXNode> whitelistedNodes) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        List<SerializedAXNode> children = new ArrayList<>();
        for (AXNode child : node.getChildren())
            children.addAll(serializeTree(child, whitelistedNodes));

        if (CollKit.isNotEmpty(whitelistedNodes) && !whitelistedNodes.contains(node))
            return children;

        SerializedAXNode serializedNode = node.serialize();
        if (CollKit.isNotEmpty(children))
            serializedNode.setChildren(children);
        List<SerializedAXNode> result = new ArrayList<>();
        result.add(serializedNode);
        return result;
    }

}
