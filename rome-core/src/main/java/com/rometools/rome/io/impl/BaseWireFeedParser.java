/*
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rometools.rome.feed.WireFeedForeignMarkup;
import com.rometools.rome.feed.module.Extendable;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.WireFeedParser;

/**
 * Basic WireFeedParser implementation.
 */
public abstract class BaseWireFeedParser extends ChildNavigator implements WireFeedParser {
    /**
     * [TYPE].feed.ModuleParser.classes= [className] ...
     *
     */
    private static final String FEED_MODULE_PARSERS_POSFIX_KEY = ".feed.ModuleParser.classes";

    /**
     * [TYPE].item.ModuleParser.classes= [className] ...
     *
     */
    private static final String ITEM_MODULE_PARSERS_POSFIX_KEY = ".item.ModuleParser.classes";

    /**
     * [TYPE].person.ModuleParser.classes= [className] ...
     *
     */
    private static final String PERSON_MODULE_PARSERS_POSFIX_KEY = ".person.ModuleParser.classes";

    private final String type;
    private final ModuleParsers feedModuleParsers;
    private final ModuleParsers itemModuleParsers;
    private final ModuleParsers personModuleParsers;
    private final Namespace namespace;

    protected BaseWireFeedParser(final String type, final Namespace namespace) {
        this.type = type;
        this.namespace = namespace;
        feedModuleParsers = new ModuleParsers(type + FEED_MODULE_PARSERS_POSFIX_KEY, this);
        itemModuleParsers = new ModuleParsers(type + ITEM_MODULE_PARSERS_POSFIX_KEY, this);
        personModuleParsers = new ModuleParsers(type + PERSON_MODULE_PARSERS_POSFIX_KEY, this);
    }

    /**
     * Returns the type of feed the parser handles.

     *
     * @see WireFeed for details on the format of this string.

     * @return the type of feed the parser handles.
     *
     */
    @Override
    public String getType() {
        return type;
    }

    protected List<Module> parseFeedModules(final Element feedElement, final Locale locale) {
        return feedModuleParsers.parseModules(feedElement, locale);
    }

    protected List<Module> parseItemModules(final Element itemElement, final Locale locale) {
        return itemModuleParsers.parseModules(itemElement, locale);
    }

    protected List<Module> parsePersonModules(final Element itemElement, final Locale locale) {
        return personModuleParsers.parseModules(itemElement, locale);
    }

    protected List<WireFeedForeignMarkup> extractForeignMarkup(final Element e, final Extendable ext, final Namespace namespace) {
        final List<WireFeedForeignMarkup> foreignElements = new ArrayList<WireFeedForeignMarkup>(1);
        final List<Element> childs = super.getChildren(e);
        for (Element c : childs) {
        	if (null != c.getNamespaceURI() &&
        			!namespace.getNamespaceURI().equals(c.getNamespaceURI()) &&
        			ext.getModule(c.getNamespaceURI()) == null) {
                // if element not in the RSS namespace and elem was not handled by a module save it
                // as foreign markup but we can't detach it while we're iterating
                foreignElements.add(new WireFeedForeignMarkup((Element) e.getOwnerDocument().importNode(c, false)));
        	}
        }

        return foreignElements;

    }

    protected Attr getAttribute(final Element e, final String attributeName) {
        Attr attribute = e.getAttributeNode(attributeName);
        if (attribute == null) {
            attribute = e.getAttributeNodeNS(namespace.getNamespaceURI(), attributeName);
        }
        return attribute;
    }

    protected String getAttributeValue(final Element e, final String attributeName) {
    	String value = null;
        final Attr attr = getAttribute(e, attributeName);
        if (null != attr) {
            value = attr.getValue();
        }
        return value;
    }

    protected String getStyleSheet(final Document doc) {
        String styleSheet = null;
        NodeList childs = doc.getChildNodes();
        boolean found = false;
        for (int i = 0; !found && i < childs.getLength(); i++) {
        	if (childs.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE &&
        		childs.item(i).getTextContent().indexOf("text/xsl") >= 0) {
        			int begin = childs.item(i).getTextContent().indexOf("href");
        			styleSheet = childs.item(i).getTextContent().substring(begin+5).replaceAll("\"", "");
        			found = true;
        		}
        }
        return styleSheet;
    }

    protected static Namespace createNamespace(final String prefix, final String namespaceURI) {
    	return XMLEventFactory.newDefaultFactory().createNamespace(prefix, namespaceURI);
    }
    
    protected static Namespace createNamespace(final String namespaceURI) {
    	return XMLEventFactory.newDefaultFactory().createNamespace(namespaceURI);
    }
    
	protected List<Namespace> getAdditionalNamespaces(final Element element) {
		List<Namespace> namespaces = new ArrayList<>(1);
		NamedNodeMap attributes = element.getAttributes();
	    if (attributes != null) {
	        for (int i = 0; i < attributes.getLength(); i++) {
	            Node node = attributes.item(i);
	            if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
	                namespaces.add(createNamespace(node.getTextContent()));
	            }
	        }
	    }
	    return namespaces;
	    
	}
	
}
