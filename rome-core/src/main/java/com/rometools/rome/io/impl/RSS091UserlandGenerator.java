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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Image;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.io.FeedException;

/**
 * Feed Generator for RSS 0.91
 * 
 */
public class RSS091UserlandGenerator extends RSS090Generator {

    private final String version;

    /**
     * Public constructor.
     */
    public RSS091UserlandGenerator() {
        this("rss_0.91U", "0.91");
    }

    protected RSS091UserlandGenerator(final String type, final String version) {
        super(type);
        this.version = version;
    }

    @Override
    protected Namespace getFeedNamespace() {
        return BaseWireFeedParser.createNamespace("");
    }

    /**
     * To be overriden by RSS 0.91 Netscape and RSS 0.94
     * @return the result of is hour format 24.
     */
    protected boolean isHourFormat24() {
        return true;
    }

    protected String getVersion() {
        return version;
    }

    @Override
    protected void addChannel(final Channel channel, final Element parent) throws FeedException {

        super.addChannel(channel, parent);

        final Element eChannel = super.getChild(parent, "channel");
        if (null != eChannel) {
        	addImage(channel, eChannel);
        	addTextInput(channel, eChannel);
        	addItems(channel, eChannel);
        }
    }

    @Override
    protected void checkChannelConstraints(final Element eChannel) throws FeedException {

        checkNotNullAndLength(eChannel, "title", 1, 100);
        checkNotNullAndLength(eChannel, "description", 1, 500);
        checkNotNullAndLength(eChannel, "link", 1, 500);
        checkNotNullAndLength(eChannel, "language", 2, 5);
        checkLength(eChannel, "rating", 20, 500);
        checkLength(eChannel, "copyright", 1, 100);
        checkLength(eChannel, "pubDate", 1, 100);
        checkLength(eChannel, "lastBuildDate", 1, 100);
        checkLength(eChannel, "docs", 1, 500);
        checkLength(eChannel, "managingEditor", 1, 100);
        checkLength(eChannel, "webMaster", 1, 100);

        final Element skipHours = super.getChild(eChannel, "skipHours");

        if (skipHours != null) {
        	
            final List<Element> hours = super.getChildren(skipHours);
            if (null != hours && !hours.isEmpty()) {
	            for (Element h : hours) {
	
	                final int value = Integer.parseInt(h.getTextContent().trim());
	
	                if (isHourFormat24()) {
	                    if (value < 1 || value > 24) {
	                        throw new FeedException("Invalid hour value " + value + ", it must be between 1 and 24");
	                    }
	                } else {
	                    if (value < 0 || value > 23) {
	                        throw new FeedException("Invalid hour value " + value + ", it must be between 0 and 23");
	                    }
	                }
	
	            }
            }

        }

    }

    @Override
    protected void checkImageConstraints(final Element eImage) throws FeedException {
        checkNotNullAndLength(eImage, "title", 1, 100);
        checkNotNullAndLength(eImage, "url", 1, 500);
        checkLength(eImage, "link", 1, 500);
        checkLength(eImage, "width", 1, 3);
        checkLength(eImage, "width", 1, 3);
        checkLength(eImage, "description", 1, 100);
    }

    @Override
    protected void checkItemConstraints(final Element eItem) throws FeedException {
        checkNotNullAndLength(eItem, "title", 1, 100);
        checkNotNullAndLength(eItem, "link", 1, 500);
        checkLength(eItem, "description", 1, 500);
    }

    @Override
    protected void checkTextInputConstraints(final Element eTextInput) throws FeedException {
        checkNotNullAndLength(eTextInput, "title", 1, 100);
        checkNotNullAndLength(eTextInput, "description", 1, 500);
        checkNotNullAndLength(eTextInput, "name", 1, 20);
        checkNotNullAndLength(eTextInput, "link", 1, 500);
    }

    @Override
    protected Element createRootElement(final Channel channel, final Document doc) {
        final Element root = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "rss");
        root.setAttribute("version", getVersion());
        generateModuleNamespaceDefs(root);
        return root;
    }

    protected Element generateSkipDaysElement(final List<String> days, final Element elem) {
        final Element skipDaysElement = elem.getOwnerDocument().createElement("skipDays");
        for (final String day : days) {
            skipDaysElement.appendChild(generateSimpleElement("day", day.toString(), skipDaysElement));
        }
        return skipDaysElement;
    }

    protected Element generateSkipHoursElement(final List<Integer> hours, final Element elem) {
        final Element skipHoursElement = elem.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "skipHours");
        for (final Integer hour : hours) {
            skipHoursElement.appendChild(generateSimpleElement("hour", hour.toString(), skipHoursElement));
        }
        return skipHoursElement;
    }

    @Override
    protected void populateChannel(final Channel channel, final Element eChannel) {

        super.populateChannel(channel, eChannel);

        final String language = channel.getLanguage();
        if (language != null) {
            eChannel.appendChild(generateSimpleElement("language", language, eChannel));
        }

        final String rating = channel.getRating();
        if (rating != null) {
            eChannel.appendChild(generateSimpleElement("rating", rating, eChannel));
        }

        final String copyright = channel.getCopyright();
        if (copyright != null) {
            eChannel.appendChild(generateSimpleElement("copyright", copyright, eChannel));
        }

        final Date pubDate = channel.getPubDate();
        if (pubDate != null) {
            eChannel.appendChild(generateSimpleElement("pubDate", DateParser.formatRFC822(pubDate, Locale.US), eChannel));
        }

        final Date lastBuildDate = channel.getLastBuildDate();
        if (lastBuildDate != null) {
            eChannel.appendChild(generateSimpleElement("lastBuildDate", DateParser.formatRFC822(lastBuildDate, Locale.US), eChannel));
        }

        final String docs = channel.getDocs();
        if (docs != null) {
            eChannel.appendChild(generateSimpleElement("docs", docs, eChannel));
        }

        final String managingEditor = channel.getManagingEditor();
        if (managingEditor != null) {
            eChannel.appendChild(generateSimpleElement("managingEditor", managingEditor, eChannel));
        }

        final String webMaster = channel.getWebMaster();
        if (webMaster != null) {
            eChannel.appendChild(generateSimpleElement("webMaster", webMaster, eChannel));
        }

        final List<Integer> skipHours = channel.getSkipHours();
        if (skipHours != null && !skipHours.isEmpty()) {
            eChannel.appendChild(generateSkipHoursElement(skipHours, eChannel));
        }

        final List<String> skipDays = channel.getSkipDays();
        if (skipDays != null && !skipDays.isEmpty()) {
            eChannel.appendChild(generateSkipDaysElement(skipDays, eChannel));
        }

    }

    @Override
    protected void populateFeed(final Channel channel, final Element parent) throws FeedException {
        addChannel(channel, parent);
    }

    @Override
    protected void populateImage(final Image image, final Element eImage) {

        super.populateImage(image, eImage);

        final Integer width = image.getWidth();
        if (width != null) {
            eImage.appendChild(generateSimpleElement("width", String.valueOf(width), eImage));
        }

        final Integer height = image.getHeight();
        if (height != null) {
            eImage.appendChild(generateSimpleElement("height", String.valueOf(height), eImage));
        }

        final String description = image.getDescription();
        if (description != null) {
            eImage.appendChild(generateSimpleElement("description", description, eImage));
        }

    }

    @Override
    protected void populateItem(final Item item, final Element eItem, final int index, final Element parent) {

        super.populateItem(item, eItem, index, parent);

        final Description description = item.getDescription();
        if (description != null) {
            eItem.appendChild(generateSimpleElement("description", description.getValue(), eItem));
        }

        final Namespace contentNamespace = getContentNamespace();
        final Content content = item.getContent();
        if (item.getModule(contentNamespace.getNamespaceURI()) == null && content != null) {
            final Element elem = eItem.getOwnerDocument().createElementNS(contentNamespace.getNamespaceURI(), "encoded");
            elem.setPrefix(contentNamespace.getPrefix());
            elem.setTextContent(content.getValue());
            eItem.appendChild(elem);
        }

    }

}
