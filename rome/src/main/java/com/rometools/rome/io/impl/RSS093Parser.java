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

import java.util.Locale;

import org.w3c.dom.Element;

import com.rometools.rome.feed.rss.Item;

public class RSS093Parser extends RSS092Parser {

    public RSS093Parser() {
        this("rss_0.93");
    }

    protected RSS093Parser(final String type) {
        super(type);
    }

    @Override
    protected String getRSSVersion() {
        return "0.93";
    }

    @Override
    protected Item parseItem(final Element rssRoot, final Element eItem, final Locale locale) {

        final Item item = super.parseItem(rssRoot, eItem, locale);

        final Element pubDate = super.getChild(eItem, "pubDate");
        if (pubDate != null) {
            item.setPubDate(DateParser.parseDate(pubDate.getTextContent(), locale));
        }

        final Element expirationDate = super.getChild(eItem, "expirationDate");
        if (expirationDate != null) {
            item.setExpirationDate(DateParser.parseDate(expirationDate.getTextContent(), locale));
        }

        final Element description = super.getChild(eItem, "description");
        if (description != null) {
            final String type = ((Element) description).getAttribute("type");
            if (type != null) {
                item.getDescription().setType(type);
            }
        }

        return item;

    }

}
