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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Image;
import com.rometools.rome.feed.rss.Item;

/**
 * RSS 0.91 Userland Parser. 
 *
 */
public class RSS091UserlandParser extends RSS090Parser {

	/** The TEXTINPUT constant. */
	private static final String TEXTINPUT = "textInput";
	
	/**
	 * Public constructor.
	 */
    public RSS091UserlandParser() {
        this("rss_0.91U");
    }

    protected RSS091UserlandParser(final String type) {
        super(type, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMyType(final Document document) {
        final Element rssRoot = document.getDocumentElement();
        final Attr version = rssRoot.getAttributeNode("version");
        return rssRoot.getNodeName().equals("rss") && version != null && version.getValue().equals(getRSSVersion());
    }

    protected String getRSSVersion() {
        return "0.91";
    }

    @Override
    protected Namespace getRSSNamespace() {
        return super.createNamespace("");
    }

    /**
     * To be overriden by RSS 0.91 Netscape and RSS 0.94
     * 
     * @param rssRoot the root element of the RSS document to parse.
     * @return the result of is hour format 24.
     */
    protected boolean isHourFormat24(final Element rssRoot) {
        return true;
    }

    /**
     * Parses the root element of an RSS document into a Channel bean.
     * 
     * It first invokes super.parseChannel and then parses and injects the following properties if
     * present: language, pubDate, rating and copyright.
     * 
     *
     * @param rssRoot the root element of the RSS document to parse.
     * @param locale for date/time parsing
     * @return the parsed Channel bean.
     */
    @Override
    protected WireFeed parseChannel(final Element rssRoot, final Locale locale) {

        final Channel channel = (Channel) super.parseChannel(rssRoot, locale);

        final Element eChannel = super.getChild(rssRoot, "channel", getRSSNamespace());
        if (null != eChannel) {
	        final Element language = super.getChild(eChannel, "language", getRSSNamespace());
	        if (language != null) {
	            channel.setLanguage(language.getTextContent());
	        }
	
	        final Element atinge = super.getChild(eChannel, "rating", getRSSNamespace());
	        if (atinge != null) {
	            channel.setRating(atinge.getTextContent());
	        }
	
	        final Element copyright = super.getChild(eChannel, "copyright", getRSSNamespace());
	        if (copyright != null) {
	            channel.setCopyright(copyright.getTextContent());
	        }
	
	        final Element pubDate = super.getChild(eChannel, "pubDate", getRSSNamespace());
	        if (pubDate != null) {
	            channel.setPubDate(DateParser.parseDate(pubDate.getTextContent(), locale));
	        }
	
	        final Element lastBuildDate = super.getChild(eChannel, "lastBuildDate", getRSSNamespace());
	        if (lastBuildDate != null) {
	            channel.setLastBuildDate(DateParser.parseDate(lastBuildDate.getTextContent(), locale));
	        }
	
	        final Element docs = super.getChild(eChannel, "docs", getRSSNamespace());
	        if (docs != null) {
	            channel.setDocs(docs.getTextContent());
	        }
	
	        final Element generator = super.getChild(eChannel, "generator", getRSSNamespace());
	        if (generator != null) {
	            channel.setGenerator(generator.getTextContent());
	        }
	
	        final Element managingEditor = super.getChild(eChannel, "managingEditor", getRSSNamespace());
	        if (managingEditor != null) {
	            channel.setManagingEditor(managingEditor.getTextContent());
	        }
	
	        final Element webMaster = super.getChild(eChannel, "webMaster", getRSSNamespace());
	        if (webMaster != null) {
	            channel.setWebMaster(webMaster.getTextContent());
	        }
	
	        final Element eSkipHours = super.getChild(eChannel, "skipHours");
	        if (eSkipHours != null) {
	            final List<Integer> skipHours = new ArrayList<Integer>();
	            final List<Element> eHours = super.getChildren(eSkipHours, "hour", getRSSNamespace());
	            if (null != eHours) {
		            for (Element h : eHours) {
		                skipHours.add(Integer.valueOf(h.getTextContent().trim()));
		            }
	            }
	            channel.setSkipHours(skipHours);
	        }
	
	        final Element eSkipDays = super.getChild(eChannel, "skipDays");
	        if (eSkipDays != null) {
	            final List<String> skipDays = new ArrayList<String>();
	            final List<Element> eDays = super.getChildren(eSkipDays, "day", getRSSNamespace());
	            if (null != eDays) {
		            for (Element d : eDays) {
		            	skipDays.add(d.getTextContent().trim());
		            }
	            }
	            channel.setSkipDays(skipDays);
	        }
        }
        return channel;
    }

    /**
     * Parses the root element of an RSS document looking for image information.
     * 
     * It first invokes super.parseImage and then parses and injects the following properties if
     * present: url, link, width, height and description.
     * 
     *
     * @param rssRoot the root element of the RSS document to parse for image information.
     * @return the parsed RSSImage bean.
     */
    @Override
    protected Image parseImage(final Element rssRoot) {

        final Image image = super.parseImage(rssRoot);
        if (image != null) {

            final Element eImage = getImage(rssRoot);
            if (null != eImage) {
	            final Element width = super.getChild(eImage, "width", getRSSNamespace());
	            if (width != null) {
	                final Integer val = NumberParser.parseInt(width.getTextContent());
	                if (val != null) {
	                    image.setWidth(val);
	                }
	            }
	
	            final Element height = super.getChild(eImage, "height", getRSSNamespace());
	            if (height != null) {
	                final Integer val = NumberParser.parseInt(height.getTextContent());
	                if (val != null) {
	                    image.setHeight(val);
	                }
	            }
	
	            final Element description = super.getChild(eImage, "description", getRSSNamespace());
	            if (description != null) {
	                image.setDescription(description.getTextContent());
	            }
            }
        }

        return image;

    }

    /**
     * It looks for the 'item' elements under the 'channel' elemment.
     */
    @Override
    protected List<Element> getItems(final Element rssRoot) {
    	List<Element> items = new ArrayList<>(1);
        final Element eChannel = super.getChild(rssRoot, "channel", getRSSNamespace());

        if (eChannel != null) {
            items = super.getChildren(eChannel, "item", getRSSNamespace());
        } else {
            items = Collections.emptyList();
        }
        return items;
    }

    /**
     * It looks for the 'image' elements under the 'channel' elemment.
     */
    @Override
    protected Element getImage(final Element rssRoot) {
    	Element result = null;
        final Element eChannel = super.getChild(rssRoot, "channel", getRSSNamespace());

        if (eChannel != null) {
            result = super.getChild(eChannel, "image", getRSSNamespace());
        } 
        return result;

    }

    /**
     * To be overriden by RSS 0.91 Netscape parser
     * @return the TEXTINPUT constant.
     */
    protected String getTextInputLabel() {
        return TEXTINPUT;
    }

    /**
     * It looks for the 'textinput' elements under the 'channel' elemment.
     */
    @Override
    protected Element getTextInput(final Element rssRoot) {

        final String elementName = getTextInputLabel();

        final Element eChannel = super.getChild(rssRoot, "channel", getRSSNamespace());
        if (eChannel != null) {
            return super.getChild(eChannel, elementName, getRSSNamespace());
        } else {
            return null;
        }

    }

    /**
     * Parses an item element of an RSS document looking for item information.
     * 
     * It first invokes super.parseItem and then parses and injects the description property if
     * present.
     * 
     *
     * @param rssRoot the root element of the RSS document in case it's needed for context.
     * @param eItem the item element to parse.
     * @param locale for date/time parsing
     * @return the parsed RSSItem bean.
     */
    @Override
    protected Item parseItem(final Element rssRoot, final Element eItem, final Locale locale) {

        final Item item = super.parseItem(rssRoot, eItem, locale);

        final Element description = super.getChild(eItem, "description", getRSSNamespace());
        if (description != null) {
            item.setDescription(parseItemDescription(rssRoot, description));
        }

        final Element pubDate = super.getChild(eItem, "pubDate", getRSSNamespace());
        if (pubDate != null) {
            item.setPubDate(DateParser.parseDate(pubDate.getTextContent(), locale));
        }

        final Element encoded = super.getChild(eItem, "encoded", getContentNamespace());
        if (encoded != null) {
            final Content content = new Content();
            content.setType(Content.HTML);
            content.setValue(encoded.getTextContent());
            item.setContent(content);
        }

        return item;

    }

    protected Description parseItemDescription(final Element rssRoot, final Element eDesc) {
        final Description desc = new Description();
        desc.setType("text/plain");
        desc.setValue(eDesc.getTextContent());
        return desc;
    }

}
