/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.rometools.rome.io.impl;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rometools.rome.feed.rss.Description;

public class RSS20Parser extends RSS094Parser {

    public RSS20Parser() {
        this("rss_2.0");
    }

    protected RSS20Parser(final String type) {
        super(type);
    }

    @Override
    protected String getRSSVersion() {
        return "2.0";
    }

    @Override
    protected boolean isHourFormat24(final Element rssRoot) {
        return false;
    }

    @Override
    protected Description parseItemDescription(final Element rssRoot, final Element eDesc) {
    	return super.parseItemDescription(rssRoot, eDesc);
    }

    @Override
    public boolean isMyType(final Document document) {
    	return isMyType(document.getFirstChild());
    	
    }

    private boolean isMyType(final Node n) {
    	if (n.getNodeType() == Node.ELEMENT_NODE) {
    		return rootElementMatches(n)
               && (versionMatches(n) || versionAbsent(n));
    	} else {
    		return isMyType(n.getNextSibling());
    	}
    }
    
    private boolean rootElementMatches(final Node node) {
        return "rss".equals(node.getNodeName());
    }

    private boolean versionMatches(final Node node) {
        final Attr version = ((Element) node).getAttributeNode("version");
        return (version != null)
               && version.getValue().trim().startsWith(getRSSVersion());
    }

    private boolean versionAbsent(final Node node) {
    	final Attr version = ((Element) node).getAttributeNode("version");
        return null == version || null == version.getTextContent();
    }
}
