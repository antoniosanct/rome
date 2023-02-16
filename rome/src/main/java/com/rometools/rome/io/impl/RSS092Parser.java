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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.rss.Category;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Cloud;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Enclosure;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.rss.Source;
import com.rometools.utils.Strings;

public class RSS092Parser extends RSS091UserlandParser {

    public RSS092Parser() {
        this("rss_0.92");
    }

    protected RSS092Parser(final String type) {
        super(type);
    }

    @Override
    protected String getRSSVersion() {
        return "0.92";
    }

    @Override
    protected WireFeed parseChannel(final Element rssRoot, final Locale locale) {

        final Channel channel = (Channel) super.parseChannel(rssRoot, locale);

        final Element eChannel = super.getChild(rssRoot, "channel", getRSSNamespace());
        if (null != eChannel) {
	        final Element eCloud = super.getChild(eChannel, "cloud", getRSSNamespace());
	
	        if (eCloud != null) {
	            final Cloud cloud = new Cloud();
	            final Element e = (Element) eCloud;
	            final String domain = e.getAttribute("domain");
	            if (domain != null) {
	                cloud.setDomain(domain);
	            }
	
	            // getRSSNamespace()); DONT KNOW WHY DOESN'T WORK
	            final String port = e.getAttribute("port");
	            if (port != null) {
	                cloud.setPort(Integer.parseInt(port.trim()));
	            }
	
	            // getRSSNamespace()); DONT KNOW WHY DOESN'T WORK
	            final String path = e.getAttribute("path");
	            if (path != null) {
	                cloud.setPath(path);
	            }
	
	            // getRSSNamespace()); DONT KNOW WHY DOESN'T WORK
	            final String registerProcedure = e.getAttribute("registerProcedure");
	            if (registerProcedure != null) {
	                cloud.setRegisterProcedure(registerProcedure);
	            }
	
	            // getRSSNamespace()); DONT KNOW WHY DOESN'T WORK
	            final String protocol = e.getAttribute("protocol");
	            if (protocol != null) {
	                cloud.setProtocol(protocol);
	            }
	
	            channel.setCloud(cloud);
	
	        }
        }
        return channel;
    }

    @Override
    protected Item parseItem(final Element rssRoot, final Element eItem, final Locale locale) {
        final Item item = super.parseItem(rssRoot, eItem, locale);

        final Element eSource = super.getChild(eItem, "source", getRSSNamespace());
        if (eSource != null) {
            final Source source = new Source();
            final String url = eSource.getAttribute("url");
            source.setUrl(url);
            source.setValue(eSource.getTextContent());
            item.setSource(source);
        }

        // 0.92 allows one enclosure occurrence, 0.93 multiple just saving to write some code.
        final List<Element> eEnclosures = super.getChildren(eItem, "enclosure");

        if (null != eEnclosures) {

            final List<Enclosure> enclosures = new ArrayList<>(1);

            for (Element e: eEnclosures) {

                final Enclosure enclosure = new Enclosure();
                final String url = e.getAttribute("url");
                if (url != null) {
                    enclosure.setUrl(url);
                }

                final String length = e.getAttribute("length");
                enclosure.setLength(NumberParser.parseLong(length, 0L));

                final String type = e.getAttribute("type");
                if (type != null) {
                    enclosure.setType(type);
                }

                enclosures.add(enclosure);

            }

            item.setEnclosures(enclosures);
        }
        
        final List<Element> categories = super.getChildren(eItem, "category");
        item.setCategories(parseCategories(categories));

        return item;
    }

    protected List<Category> parseCategories(final List<Element> eCats) {

        final List<Category> cats = new ArrayList<Category>();

        for (final Element eCat : eCats) {
        	
            // skip categories without value
            final String text = eCat.getTextContent();
            if(Strings.isBlank(text)) {
                continue;
            }

            final Category cat = new Category();               
            final String domain = eCat.getAttribute("domain");
            if (domain != null) {
                cat.setDomain(domain);
            }
            cat.setValue(text);

            cats.add(cat);
            
        }
        
        if(cats.isEmpty()) {
            return null;
        }
        
        return cats;

    }

    @Override
    protected Description parseItemDescription(final Element rssRoot, final Element eDesc) {
        final Description desc = new Description();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eDesc.getChildNodes().getLength(); i++) {
        	final Node n = eDesc.getChildNodes().item(i); 
            switch (n.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                case Node.ENTITY_REFERENCE_NODE:
                    sb.append(n.getTextContent());
                    break;
                case Node.ELEMENT_NODE:
            		try {
            			TransformerFactory tf = TransformerFactory.newInstance();
            			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            			Transformer t = tf.newTransformer();
            			t.setOutputProperty(OutputKeys.METHOD, "xml");
            			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            			StringWriter buffer = new StringWriter();
            	        StreamResult result = new StreamResult(buffer);
            	        DOMSource source = new DOMSource(n);
            	        t.transform(source, result);
            	        sb.append(buffer.toString());
            		} catch (TransformerException | TransformerFactoryConfigurationError e) {
//            			throw new FeedException("Error outputting feed", e);
            		}
                    sb.append(n.getTextContent());
                    break;
                default:
                    // ignore
                    break;
            }
        }
        desc.setValue(sb.toString());
        String att = eDesc.getAttribute("type");
        if (att == null) {
            att = "text/html";
        }
        desc.setType(att);
        return desc;
    }

}
