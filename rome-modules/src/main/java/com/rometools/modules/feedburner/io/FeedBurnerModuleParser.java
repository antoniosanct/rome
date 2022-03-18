/*
 * Copyright 2010 Scandio GmbH.
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
package com.rometools.modules.feedburner.io;

import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.feedburner.FeedBurner;
import com.rometools.modules.feedburner.FeedBurnerImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
import com.rometools.rome.io.impl.ChildNavigator;

/**
 * ModuleParser implementation for the FeedBurner RSS extension.
 */
public class FeedBurnerModuleParser extends ChildNavigator implements ModuleParser {
    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace(FeedBurner.URI);

    @Override
    public String getNamespaceUri() {
        return FeedBurner.URI;
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        final FeedBurnerImpl fbi = new FeedBurnerImpl();
        boolean returnObj = false;
        Element tag = super.getChild(element, "awareness", FeedBurnerModuleParser.NS);

        if (tag != null) {
            fbi.setAwareness(tag.getTextContent().trim());
            returnObj = true;
        }

        tag = super.getChild(element, "origLink", FeedBurnerModuleParser.NS);

        if (tag != null) {
            fbi.setOrigLink(tag.getTextContent().trim());
            returnObj = true;
        }

        tag = super.getChild(element, "origEnclosureLink", FeedBurnerModuleParser.NS);

        if (tag != null) {
            fbi.setOrigEnclosureLink(tag.getTextContent().trim());
            returnObj = true;
        }

        if (returnObj) {
            return fbi;
        }

        return null;
    }
}
