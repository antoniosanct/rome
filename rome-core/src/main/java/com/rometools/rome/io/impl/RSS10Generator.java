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

import java.util.List;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.io.FeedException;

/**
 * Feed Generator for RSS 1.0
 * 
 */

public class RSS10Generator extends RSS090Generator {

    private static final String RSS_URI = "http://purl.org/rss/1.0/";
    private static final Namespace RSS_NS = BaseWireFeedParser.createNamespace(RSS_URI);

    public RSS10Generator() {
        super("rss_1.0");
    }

    protected RSS10Generator(final String feedType) {
        super(feedType);
    }

    @Override
    protected Namespace getFeedNamespace() {
        return RSS_NS;
    }

    @Override
    protected void populateChannel(final Channel channel, final Element eChannel) {

        super.populateChannel(channel, eChannel);

        final String channelUri = channel.getUri();
        if (channelUri != null) {
            eChannel.setAttributeNS(getRDFNamespace().getNamespaceURI(), "about", channelUri);
        }

        final List<Item> items = channel.getItems();
        if (!items.isEmpty()) {
            final Element eItems = eChannel.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "items");
            final Element eSeq = eChannel.getOwnerDocument().createElementNS(getRDFNamespace().getNamespaceURI(), "Seq");
            for (final Item item : items) {
                final Element lis = eChannel.getOwnerDocument().createElementNS(getRDFNamespace().getNamespaceURI(), "li");
                final String uri = item.getUri();
                if (uri != null) {
                    lis.setAttributeNS(getRDFNamespace().getNamespaceURI(), "resource", uri);
                }
                eSeq.appendChild(lis);
            }
            eItems.appendChild(eSeq);
            eChannel.appendChild(eItems);
        }
    }

    @Override
    protected void populateItem(final Item item, final Element eItem, final int index, final Element parent) {

        super.populateItem(item, eItem, index, parent);

        final String link = item.getLink();
        final String uri = item.getUri();
        if (uri != null) {
            eItem.setAttributeNS(getRDFNamespace().getNamespaceURI(), "about", uri);
        } else if (link != null) {
            eItem.setAttributeNS(getRDFNamespace().getNamespaceURI(), "about", link);
        }

        final Description description = item.getDescription();
        if (description != null) {
            eItem.appendChild(generateSimpleElement("description", description.getValue(), eItem));
        }

        if (item.getModule(getContentNamespace().getNamespaceURI()) == null && item.getContent() != null) {
            final Element elem = eItem.getOwnerDocument().createElementNS(getContentNamespace().getNamespaceURI(), "encoded");
            elem.setTextContent(item.getContent().getValue());
            eItem.appendChild(elem);
        }

    }

    @Override
    protected void checkChannelConstraints(final Element eChannel) throws FeedException {
        checkNotNullAndLength(eChannel, "title", 0, -1);
        checkNotNullAndLength(eChannel, "description", 0, -1);
        checkNotNullAndLength(eChannel, "link", 0, -1);
    }

    @Override
    protected void checkImageConstraints(final Element eImage) throws FeedException {
        checkNotNullAndLength(eImage, "title", 0, -1);
        checkNotNullAndLength(eImage, "url", 0, -1);
        checkNotNullAndLength(eImage, "link", 0, -1);
    }

    @Override
    protected void checkTextInputConstraints(final Element eTextInput) throws FeedException {
        checkNotNullAndLength(eTextInput, "title", 0, -1);
        checkNotNullAndLength(eTextInput, "description", 0, -1);
        checkNotNullAndLength(eTextInput, "name", 0, -1);
        checkNotNullAndLength(eTextInput, "link", 0, -1);
    }

    @Override
    protected void checkItemsConstraints(final Element parent) throws FeedException {
    }

    @Override
    protected void checkItemConstraints(final Element eItem) throws FeedException {
        checkNotNullAndLength(eItem, "title", 0, -1);
        checkNotNullAndLength(eItem, "link", 0, -1);
    }

}
