/*
 * Copyright 2005 Robert Cooper, Temple of the Screaming Penguin
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
 *
 */
package com.rometools.modules.content.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.rometools.modules.content.ContentItem;
import com.rometools.modules.content.ContentModule;
import com.rometools.modules.content.ContentModuleImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;
import com.rometools.utils.DOMNodes;

public class ContentModuleParser extends ChildNavigator implements ModuleParser {
    private static final Namespace CONTENT_NS = XMLEventFactory.newDefaultFactory().createNamespace("content", ContentModule.URI);
    private static final Namespace RDF_NS = XMLEventFactory.newDefaultFactory().createNamespace("rdf", ContentModule.RDF_URI);
    
    public ContentModuleParser() {
    }

    @Override
    public String getNamespaceUri() {
        return ContentModule.URI;
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        boolean foundSomething = false;
        final ContentModule cm = new ContentModuleImpl();
        final List<Element> encodeds = super.getChildren(element, "encoded", CONTENT_NS);
        final List<String> contentStrings = new ArrayList<>(1);
        final List<String> encodedStrings = new ArrayList<>(1);

        if (!encodeds.isEmpty()) {
            foundSomething = true;

            for (int i = 0; i < encodeds.size(); i++) {
                final Element encodedElement = encodeds.get(i);
                encodedStrings.add(encodedElement.getTextContent());
                contentStrings.add(encodedElement.getTextContent());
            }
        }

        final ArrayList<ContentItem> contentItems = new ArrayList<ContentItem>();
        final List<Element> items = super.getChildren(element, "items", CONTENT_NS);

        for (int i = 0; i < items.size(); i++) {
            foundSomething = true;

            final List<Element> lis = super.getChildren(super.getChild(items.get(i), "Bag", RDF_NS), "li", RDF_NS);

            for (int j = 0; j < lis.size(); j++) {
                final ContentItem ci = new ContentItem();
                final Element li = lis.get(j);
                final Element item = super.getChild(li, "item", CONTENT_NS);
                final Element format = super.getChild(item, "format", CONTENT_NS);
                final Element encoding = super.getChild(item, "encoding", CONTENT_NS);
                final Element value = super.getChild(item, "value", RDF_NS);

                if (value != null) {
                    if (super.getAttributeNotBlank("parseType", value, RDF_NS) != null) {
                        ci.setContentValueParseType(super.getAttributeNotBlank("parseType", value, RDF_NS));
                    }

                    if (ci.getContentValueParseType() != null && ci.getContentValueParseType().equals("Literal")) {
                		ci.setContentValue(getXmlInnerText(value));
                		contentStrings.add(getXmlInnerText(value));
//                        ci.setContentValueNamespaces(value.getAdditionalNamespaces());
                    } else {
                        ci.setContentValue(value.getTextContent());
                        contentStrings.add(value.getTextContent());
                    }

                    ci.setContentValueDOM(value);
                }

                if (format != null) {
                    ci.setContentFormat(format.getAttributeNS(RDF_NS.getNamespaceURI(), "resource"));
                }

                if (encoding != null) {
                    ci.setContentEncoding(encoding.getAttributeNS(RDF_NS.getNamespaceURI(), "resource"));
                }

                if (item != null) {
                    final Attr about = item.getAttributeNodeNS(RDF_NS.getNamespaceURI(), "about");

                    if (about != null) {
                        ci.setContentAbout(about.getValue());
                    }
                }

                contentItems.add(ci);
            }
        }

        cm.setEncodeds(encodedStrings);
        cm.setContentItems(contentItems);
        cm.setContents(contentStrings);

        return foundSomething ? cm : null;
    }

    private String getXmlInnerText(Element e) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < e.getChildNodes().getLength(); i++) {
    		sb.append(DOMNodes.nodeToString(e.getChildNodes().item(i)));
    	}
    	return sb.toString();
    	
    }
    
}
