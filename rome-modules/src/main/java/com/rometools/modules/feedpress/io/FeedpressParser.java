/*
 * Copyright 2019 Maximilian Irro
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
 */
package com.rometools.modules.feedpress.io;

import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.feedpress.modules.FeedpressModule;
import com.rometools.modules.feedpress.modules.FeedpressModuleImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
import com.rometools.rome.io.impl.ChildNavigator;

/**
 * The ModuleParser implementation for the Feedpress module.
 */
public class FeedpressParser extends ChildNavigator implements ModuleParser {

    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace(FeedpressModule.URI);

    @Override
    public String getNamespaceUri() {
        return FeedpressModule.URI;
    }

    @Override
    public Module parse(Element element, Locale l) {
        FeedpressModule feedpress = null;
        if (element.getLocalName().equals("channel") || element.getLocalName().equals("feed")) {
            feedpress = new FeedpressModuleImpl();

            final Element newsletterId = super.getChild(element, FeedpressElement.NEWSLETTER_ID, NS);
            if (newsletterId != null && newsletterId.getTextContent() != null) {
                feedpress.setNewsletterId(newsletterId.getTextContent().trim());
            }

            final Element locale = super.getChild(element, FeedpressElement.LOCALE, NS);
            if (locale != null && locale.getTextContent() != null) {
                feedpress.setLocale(locale.getTextContent().trim());
            }

            final Element podcastId = super.getChild(element, FeedpressElement.PODCAST_ID, NS);
            if (podcastId != null && podcastId.getTextContent() != null) {
                feedpress.setPodcastId(podcastId.getTextContent().trim());
            }

            final Element cssFile = super.getChild(element, FeedpressElement.CSS_FILE, NS);
            if (cssFile != null && cssFile.getTextContent() != null) {
                feedpress.setCssFile(cssFile.getTextContent().trim());
            }
        }
        return feedpress;
    }

}