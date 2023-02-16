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

import org.w3c.dom.Element;

import com.rometools.rome.feed.rss.Category;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Guid;
import com.rometools.rome.feed.rss.Item;

/**
 * Feed Generator for RSS 2.0
 * 
 */
public class RSS20Generator extends RSS094Generator {

    public RSS20Generator() {
        this("rss_2.0", "2.0");
    }

    protected RSS20Generator(final String feedType, final String version) {
        super(feedType, version);
    }

    @Override
    protected void populateChannel(final Channel channel, final Element eChannel) {

        super.populateChannel(channel, eChannel);

        final String generator = channel.getGenerator();
        if (generator != null) {
            eChannel.appendChild(generateSimpleElement("generator", generator, eChannel));
        }

        final int ttl = channel.getTtl();
        if (ttl > -1) {
            eChannel.appendChild(generateSimpleElement("ttl", String.valueOf(ttl), eChannel));
        }

        final List<Category> categories = channel.getCategories();
        for (final Category category : categories) {
            eChannel.appendChild(generateCategoryElement(category, eChannel));
        }

        generateForeignMarkup(eChannel, channel.getForeignMarkup(), eChannel.getOwnerDocument().getDocumentElement());

    }

    @Override
    public void populateItem(final Item item, final Element eItem, final int index, final Element parent) {

        super.populateItem(item, eItem, index, parent);

        final Element description = super.getChild(eItem, "description");
        if (null != description) {
        	description.removeAttribute("type");
        }

        final String author = item.getAuthor();
        if (author != null) {
            eItem.appendChild(generateSimpleElement("author", author, eItem));
        }

        final String comments = item.getComments();
        if (comments != null) {
            eItem.appendChild(generateSimpleElement("comments", comments, eItem));
        }

        final Guid guid = item.getGuid();
        if (guid != null) {
            final Element eGuid = generateSimpleElement("guid", guid.getValue(), eItem);
            if (!guid.isPermaLink()) {
                eGuid.setAttribute("isPermaLink", "false");
            }
            eItem.appendChild(eGuid);
        }
    }

}
