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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.rometools.modules.content.ContentItem;
import com.rometools.modules.content.ContentModule;
import com.rometools.modules.content.ContentModuleImpl;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

public class ContentModuleParser extends ChildNavigator implements ModuleParser {
    private static final Namespace CONTENT_NS = XMLEventFactory.newDefaultFactory().createNamespace("content", ContentModule.URI);
    private static final Namespace RDF_NS = XMLEventFactory.newDefaultFactory().createNamespace("rdf", ContentModule.RDF_URI);
    private static final Logger LOG = LoggerFactory.getLogger(ContentModuleParser.class);
    
    public ContentModuleParser() {
    }

    @Override
    public String getNamespaceUri() {
        return ContentModule.URI;
    }

    @Override
    public com.rometools.rome.feed.module.Module parse(final Element element, final Locale locale) {
        boolean foundSomething = false;
        final ContentModule cm = new ContentModuleImpl();
        final List<Element> encodeds = super.getChildren(element, "encoded", CONTENT_NS);
        final ArrayList<String> contentStrings = new ArrayList<>(1);
        final ArrayList<String> encodedStrings = new ArrayList<>(1);

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
                    if (value.getAttributeNS(RDF_NS.getNamespaceURI(), "parseType") != null) {
                        ci.setContentValueParseType(value.getAttributeNS(RDF_NS.getNamespaceURI(), "parseType"));
                    }

                    if (ci.getContentValueParseType() != null && ci.getContentValueParseType().equals("Literal")) {
                        ci.setContentValue(getXmlInnerText(value));
                        contentStrings.add(getXmlInnerText(value));
//                        ci.setContentValueNamespaces(value.getAdditionalNamespaces());
                    } else {
                        ci.setContentValue(value.getTextContent());
                        contentStrings.add(value.getTextContent());
                    }

                    ci.setContentValueDOM(super.getChildren(value));
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

    protected String getXmlInnerText(final Element e) {
        String result = null;
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        StreamResult sr = new StreamResult(new StringWriter());
	        DOMSource source = new DOMSource(e);
	        t.transform(source, sr);
	        result = sr.getWriter().toString();
		} catch (TransformerException | TransformerFactoryConfigurationError te) {
			LOG.warn("Unable to get XML inner type on " + e, te);
		}
		return result;
    }
}
