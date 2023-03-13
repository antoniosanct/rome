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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rometools.modules.content.ContentItem;
import com.rometools.modules.content.ContentModule;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleGenerator;

public class ContentModuleGenerator implements ModuleGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ContentModuleGenerator.class);

    private static final Namespace CONTENT_NS = ChildNavigator.createNamespace("content", ContentModule.URI);
    private static final Namespace RDF_NS = ChildNavigator.createNamespace("rdf", ContentModule.RDF_URI);
    private static final Set<Namespace> NAMESPACES;

    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(CONTENT_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    public ContentModuleGenerator() {
    }

    @Override
    public void generate(final Module module, final Element element) {
        // this is not necessary, it is done to avoid the namespace definition in every item.
        Element root = element;

        while (root.getParentNode() != null && root.getParentNode() instanceof Element) {
            root = (Element) root.getParentNode();
        }

//        root.addNamespaceDeclaration(CONTENT_NS);
        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + CONTENT_NS.getPrefix(), CONTENT_NS.getNamespaceURI());
        

        if (!(module instanceof ContentModule)) {
            return;
        }

        final ContentModule cm = (ContentModule) module;

        final List<String> encodeds = cm.getEncodeds();

        if (encodeds != null) {
            LOG.debug("{}", cm.getEncodeds().size());
            for (int i = 0; i < encodeds.size(); i++) {
                element.appendChild(generateCDATAElement("encoded", encodeds.get(i).toString(), element.getOwnerDocument()));
            }
        }

        final List<ContentItem> contentItems = cm.getContentItems();

        if (contentItems != null && !contentItems.isEmpty()) {
            final Element items = element.getOwnerDocument().createElementNS(CONTENT_NS.getNamespaceURI(), "items");
            items.setPrefix(CONTENT_NS.getPrefix());
            final Element bag = element.getOwnerDocument().createElementNS(RDF_NS.getNamespaceURI(), "Bag");
            bag.setPrefix(RDF_NS.getPrefix());
            items.appendChild(bag);

            for (int i = 0; i < contentItems.size(); i++) {
                final ContentItem contentItem = contentItems.get(i);
                final Element li = element.getOwnerDocument().createElementNS(RDF_NS.getNamespaceURI(), "li");
                li.setPrefix(RDF_NS.getPrefix());
                final Element item = element.getOwnerDocument().createElementNS(CONTENT_NS.getNamespaceURI(), "item");
                item.setPrefix(CONTENT_NS.getPrefix());
                
                if (contentItem.getContentAbout() != null) {
                    item.setAttributeNS(RDF_NS.getNamespaceURI(), "about", contentItem.getContentAbout());
                }

                if (contentItem.getContentFormat() != null) {
                    final Element format = element.getOwnerDocument().createElementNS(CONTENT_NS.getNamespaceURI(), "format");
                    format.setPrefix(CONTENT_NS.getPrefix());
                    format.setAttributeNS(RDF_NS.getNamespaceURI(), "resource", contentItem.getContentFormat());
                    item.appendChild(format);
                }

                if (contentItem.getContentEncoding() != null) {
                    final Element encoding = element.getOwnerDocument().createElementNS(CONTENT_NS.getNamespaceURI(), "encoding");
                    encoding.setPrefix(CONTENT_NS.getPrefix());
                    encoding.setAttributeNS(RDF_NS.getNamespaceURI(), "resource", contentItem.getContentEncoding());
                    item.appendChild(encoding);
                }

                if (null != contentItem.getContentValueDOM()) {
                	Node newN = element.getOwnerDocument().adoptNode(contentItem.getContentValueDOM().cloneNode(true));
                	item.appendChild(newN);
                } else {
                    final Element value = element.getOwnerDocument().createElementNS(RDF_NS.getNamespaceURI(), "value");
                    value.setPrefix(RDF_NS.getPrefix());
                    
                    if (contentItem.getContentValueParseType() != null) {
                    	value.setAttributeNS(RDF_NS.getNamespaceURI(), "parseType", contentItem.getContentValueParseType());
                    }

                    if (contentItem.getContentValueNamespaces() != null) {
                        final List<Namespace> namespaces = contentItem.getContentValueNamespaces();

                        for (int ni = 0; ni < namespaces.size(); ni++) {
                        	value.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + namespaces.get(ni).getPrefix(), namespaces.get(ni).getNamespaceURI());
                        }
                    }
                	value.setTextContent(contentItem.getContentValue());
                    item.appendChild(value);
                } // end value

                li.appendChild(item);
                bag.appendChild(li);
            } // end contentItems loop

            element.appendChild(items);
        }
    }

    protected Element generateSimpleElement(final String name, final String value, final Document doc) {
        final Element element = doc.createElementNS(CONTENT_NS.getNamespaceURI(), name);
        element.setPrefix(CONTENT_NS.getPrefix());
        element.setTextContent(value);
        return element;
    }

    protected Element generateCDATAElement(final String name, final String value, final Document doc) {
        final Element element = doc.createElementNS(CONTENT_NS.getNamespaceURI(), name);
        element.setPrefix(CONTENT_NS.getPrefix());
        final CDATASection cdata = doc.createCDATASection(value);
        element.appendChild(cdata);
        return element;
    }

    @Override
    public String getNamespaceUri() {
        return ContentModule.URI;
    }

    @Override
    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }
}
