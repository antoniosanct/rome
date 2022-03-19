/*
 * Copyright 2005 Robert Cooper, Temple of the Screaming Penguin
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
package com.rometools.modules.itunes.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.rometools.modules.itunes.AbstractITunesObject;
import com.rometools.modules.itunes.EntryInformationImpl;
import com.rometools.modules.itunes.FeedInformationImpl;
import com.rometools.modules.itunes.types.Category;
import com.rometools.modules.itunes.types.Duration;
import com.rometools.modules.itunes.types.Subcategory;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;
import com.rometools.rome.io.WireFeedParser;

public class ITunesParser extends ChildNavigator implements ModuleParser {

    private static final Logger LOG = LoggerFactory.getLogger(ITunesParser.class);

    private static final List<String> EXPLICIT_TRUE = Arrays.asList("yes", "explicit", "true");
    private static final List<String> EXPLICIT_FALSE = Arrays.asList("clean", "no", "false");

    Namespace ns = XMLEventFactory.newDefaultFactory().createNamespace(AbstractITunesObject.URI);

    public ITunesParser() {
    }

    public void setParser(final WireFeedParser feedParser) {
    }

    @Override
    public String getNamespaceUri() {
        return AbstractITunesObject.URI;
    }

    @Override
    public com.rometools.rome.feed.module.Module parse(final Element element, final Locale locale) {
        AbstractITunesObject module = null;

        if (element.getLocalName().equals("channel")) {
            final FeedInformationImpl feedInfo = new FeedInformationImpl();
            module = feedInfo;

            // Now I am going to get the channel specific tags
            final Element owner = super.getChild(element, "owner", ns);

            if (owner != null) {
                final Element name = super.getChild(owner, "name", ns);

                if (name != null) {
                    feedInfo.setOwnerName(name.getTextContent());
                }

                final Element email = super.getChild(owner, "email", ns);

                if (email != null) {
                    feedInfo.setOwnerEmailAddress(email.getTextContent());
                }
            }

            final List<Element> categories = super.getChildren(element, "category", ns);
            for (final Element element2 : categories) {
                final Element category = element2;
                if (category != null && category.getAttribute("text") != null) {
                    final Category cat = new Category();
                    cat.setName(category.getAttribute("text").trim());

                    final List<Element> subCategories = super.getChildren(category, "category", ns);

                    for (Element subCategory : subCategories) {
                        if (subCategory.getAttribute("text") != null) {
                            final Subcategory subcat = new Subcategory();
                            subcat.setName(subCategory.getAttribute("text"));
                            cat.addSubcategory(subcat);
                        }
                    }


                    feedInfo.getCategories().add(cat);
                }
            }

            final Element complete = super.getChild(element, "complete", ns);
            if (complete != null) {
                feedInfo.setComplete("yes".equals(complete.getTextContent().toLowerCase()));
            }

            final Element newFeedUrl = super.getChild(element, "new-feed-url", ns);
            if (newFeedUrl != null) {
                feedInfo.setNewFeedUrl(newFeedUrl.getTextContent());
            }

            final Element type = super.getChild(element, "type", ns);
            if (type != null) {
                feedInfo.setType(type.getTextContent());
            }

        } else if (element.getLocalName().equals("item")) {
            final EntryInformationImpl entryInfo = new EntryInformationImpl();
            module = entryInfo;

            // Now I am going to get the item specific tags

            final Element duration = super.getChild(element, "duration", ns);

            if (duration != null && duration.getTextContent() != null) {
                try {
                    final Duration dur = new Duration(duration.getTextContent().trim());
                    entryInfo.setDuration(dur);
                } catch (Exception e) {
                    LOG.warn("Failed to parse duration: {}", duration.getTextContent());
                }
            }

            final Element closedCaptioned = super.getChild(element, "isClosedCaptioned", ns);

            if (closedCaptioned != null && closedCaptioned.getTextContent() != null && closedCaptioned.getTextContent().trim().equalsIgnoreCase("yes")) {
                entryInfo.setClosedCaptioned(true);
            }

            final Element order = super.getChild(element, "order", ns);

            if (order != null && order.getTextContent() != null) {
                try {
                    entryInfo.setOrder(Integer.valueOf(order.getTextContent().trim()));
                } catch (NumberFormatException e) {
                    LOG.warn("Failed to parse order: {}", order.getTextContent());
                }
            }

            final Element season = super.getChild(element, "season", ns);

            if (season != null && season.getTextContent() != null) {
                try {
                    entryInfo.setSeason(Integer.valueOf(season.getTextContent().trim()));
                } catch (NumberFormatException e) {
                    LOG.warn("Failed to parse season: {}", season.getTextContent());
                }
            }

            final Element episode = super.getChild(element, "episode", ns);

            if (episode != null && episode.getTextContent() != null) {
                try {
                    entryInfo.setEpisode(Integer.valueOf(episode.getTextContent().trim()));
                } catch (NumberFormatException e) {
                    LOG.warn("Failed to parse episode: {}", episode.getTextContent());
                }
            }

            final Element episodeType = super.getChild(element, "episodeType", ns);

            if (episodeType != null && episodeType.getTextContent() != null) {
                entryInfo.setEpisodeType(episodeType.getTextContent());
            }

            final Element title = super.getChild(element, "title", ns);

            if (title != null && title.getTextContent() != null) {
                entryInfo.setTitle(title.getTextContent().trim());
            }
        }
        if (module != null) {
            // All these are common to both Channel and Item
            final Element author = super.getChild(element, "author", ns);

            if (author != null && author.getTextContent() != null) {
                module.setAuthor(author.getTextContent());
            }

            final Element block = super.getChild(element, "block", ns);

            // Ignore case of the value, assuming that any kind of "yes" clearly shows the intent.
            if (block != null
                    && block.getTextContent() != null
                    && block.getTextContent().trim().equalsIgnoreCase("Yes")) {
                module.setBlock(true);
            }

            final Element explicit = super.getChild(element, "explicit", ns);

            if (explicit != null && explicit.getTextContent() != null) {
                String explicitValue = explicit.getTextContent().trim().toLowerCase();

                if (EXPLICIT_TRUE.contains(explicitValue)) {
                    module.setExplicit(true);
                }

                if (EXPLICIT_FALSE.contains(explicitValue)) {
                    module.setExplicit(false);
                }
            }

            final Element keywords = super.getChild(element, "keywords", ns);

            if (null != keywords && null != keywords.getTextContent()) {
                final StringTokenizer tok = new StringTokenizer(keywords.getTextContent().trim(), ",");
                final String[] keywordsArray = new String[tok.countTokens()];

                for (int i = 0; tok.hasMoreTokens(); i++) {
                    keywordsArray[i] = tok.nextToken();
                }

                module.setKeywords(keywordsArray);
            }

            final Element subtitle = super.getChild(element, "subtitle", ns);

            if (subtitle != null) {
                module.setSubtitle(subtitle.getTextContent());
            }

            final Element summary = super.getChild(element, "summary", ns);

            if (summary != null) {
                module.setSummary(summary.getTextContent());
            }

            final Element image = super.getChild(element, "image", ns);

            if (image != null && image.getAttribute("href") != null) {
                try {
                    final URL imageURL = new URL(image.getAttribute("href").trim());
                    module.setImage(imageURL);
                } catch (final MalformedURLException e) {
                    LOG.warn("Malformed URL Exception reading itunes:image tag: {}", image.getAttribute("href"));
                }

                module.setImageUri(image.getAttribute("href").trim());
            }
        }

        return module;
    }

}
