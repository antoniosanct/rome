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

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.modules.feedburner.FeedBurner;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

/**
 * ModuleGenerator implementation for the FeedBurner RSS extension.
 */
public class FeedBurnerModuleGenerator implements ModuleGenerator {
    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace("feedburner", FeedBurner.URI);

    @Override
    public String getNamespaceUri() {
        return FeedBurner.URI;
    }

    @Override
    public Set<Namespace> getNamespaces() {
        final HashSet<Namespace> set = new HashSet<Namespace>();
        set.add(FeedBurnerModuleGenerator.NS);
        return set;
    }

    @Override
    public void generate(final Module module, final Element element) {
        if (!(module instanceof FeedBurner)) {
            return;
        }

        final FeedBurner feedBurner = (FeedBurner) module;

        if (feedBurner.getAwareness() != null) {
            element.appendChild(generateSimpleElement("awareness", feedBurner.getAwareness(), element.getOwnerDocument()));
        }

        if (feedBurner.getOrigLink() != null) {
            element.appendChild(generateSimpleElement("origLink", feedBurner.getOrigLink(), element.getOwnerDocument()));
        }

        if (feedBurner.getOrigEnclosureLink() != null) {
            element.appendChild(generateSimpleElement("origEnclosureLink", feedBurner.getOrigEnclosureLink(), element.getOwnerDocument()));
        }
    }

    protected Element generateSimpleElement(final String name, final String value, final Document doc) {
        final Element element = doc.createElementNS(FeedBurnerModuleGenerator.NS.getNamespaceURI(), name);
        element.setPrefix(FeedBurnerModuleGenerator.NS.getPrefix());
        element.setTextContent(value);

        return element;
    }
}
