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
 *
 */
package com.rometools.modules.itunes.io;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.modules.itunes.AbstractITunesObject;
import com.rometools.modules.itunes.EntryInformationImpl;
import com.rometools.modules.itunes.FeedInformationImpl;
import com.rometools.modules.itunes.types.Category;
import com.rometools.modules.itunes.types.Subcategory;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleGenerator;

public class ITunesGenerator implements ModuleGenerator {

    private static final HashSet<Namespace> NAMESPACES = new HashSet<Namespace>();
    private static final Namespace NAMESPACE = ChildNavigator.createNamespace(AbstractITunesObject.PREFIX, AbstractITunesObject.URI);

    static {
        NAMESPACES.add(NAMESPACE);
    }

    public ITunesGenerator() {
    }

    @Override
    public void generate(final Module module, final Element element) {
        Element root = element;

        while (root.getParentNode() != null && root.getParentNode() instanceof Element) {
            root = (Element) root.getParentNode();
        }

//        root.addNamespaceDeclaration(NAMESPACE);
        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + NAMESPACE.getPrefix(), NAMESPACE.getNamespaceURI());

        if (!(module instanceof AbstractITunesObject)) {
            return;
        }

        final AbstractITunesObject itunes = (AbstractITunesObject) module;

        if (itunes instanceof FeedInformationImpl) {
            // Do Channel Specific Stuff.
            final FeedInformationImpl info = (FeedInformationImpl) itunes;
            final Element owner = generateSimpleElement("owner", "", element.getOwnerDocument());
            final Element email = generateSimpleElement("email", info.getOwnerEmailAddress(), element.getOwnerDocument());
            owner.appendChild(email);

            final Element name = generateSimpleElement("name", info.getOwnerName(), element.getOwnerDocument());
            owner.appendChild(name);
            element.appendChild(owner);

            final List<Category> categories = info.getCategories();
            for (final Category cat : categories) {

                final Element category = generateSimpleElement("category", "", element.getOwnerDocument());
                category.setAttribute("text", cat.getName());

                for (Subcategory subcategory : cat.getSubcategories()) {
                    final Element subcat = generateSimpleElement("category", "", element.getOwnerDocument());
                    subcat.setAttribute("text", subcategory.getName());
                    category.appendChild(subcat);
                }

                element.appendChild(category);
            }

            if (info.getType() != null) {
                element.appendChild(generateSimpleElement("type",info.getType(), element.getOwnerDocument()));
            }

            if (info.getComplete()) {
                element.appendChild(generateSimpleElement("complete", "yes", element.getOwnerDocument()));
            }

            if (info.getNewFeedUrl() != null) {
                element.appendChild(generateSimpleElement("new-feed-url", info.getNewFeedUrl(), element.getOwnerDocument()));
            }

        } else if (itunes instanceof EntryInformationImpl) {
            final EntryInformationImpl info = (EntryInformationImpl) itunes;

            if (info.getDuration() != null) {
                element.appendChild(generateSimpleElement("duration", info.getDuration().toString(), element.getOwnerDocument()));
            }
            if (info.getClosedCaptioned()) {
                element.appendChild(generateSimpleElement("isClosedCaptioned", "yes", element.getOwnerDocument()));
            }
            if (info.getOrder() != null) {
                element.appendChild(generateSimpleElement("order", info.getOrder().toString(), element.getOwnerDocument()));
            }
            if (info.getEpisodeType() != null) {
                element.appendChild(generateSimpleElement("episodeType", info.getEpisodeType(), element.getOwnerDocument()));
            }
            if (info.getSeason() != null && info.getSeason() > 0) {
                element.appendChild(generateSimpleElement("season", info.getSeason().toString(), element.getOwnerDocument()));
            }
            if (info.getEpisode() != null && info.getEpisode() > 0) {
                element.appendChild(generateSimpleElement("episode", info.getEpisode().toString(), element.getOwnerDocument()));
            }
            if (info.getTitle() != null) {
                element.appendChild(generateSimpleElement("title", info.getTitle(), element.getOwnerDocument()));
            }
        }

        if (itunes.getAuthor() != null) {
            element.appendChild(generateSimpleElement("author", itunes.getAuthor(), element.getOwnerDocument()));
        }

        if (itunes.getBlock()) {
            element.appendChild(generateSimpleElement("block", "Yes", element.getOwnerDocument()));
        }

        if (itunes.getExplicitNullable() != null) {
            if (itunes.getExplicitNullable()) {
                element.appendChild(generateSimpleElement("explicit", "yes", element.getOwnerDocument()));
            } else {
                element.appendChild(generateSimpleElement("explicit", "no", element.getOwnerDocument()));
            }
        }

        if (itunes.getImage() != null) {
            final Element image = generateSimpleElement("image", "", element.getOwnerDocument());
            image.setAttribute("href", itunes.getImage().toString());
            element.appendChild(image);
        } else if (itunes.getImageUri() != null) {
            final Element image = generateSimpleElement("image", "", element.getOwnerDocument());
            image.setAttribute("href", itunes.getImageUri().toString());
            element.appendChild(image);
        }

        if (itunes.getKeywords() != null) {
            final StringBuffer sb = new StringBuffer();

            for (int i = 0; i < itunes.getKeywords().length; i++) {
                if (i != 0) {
                    sb.append(", ");
                }

                sb.append(itunes.getKeywords()[i]);
            }
            if (!"".equals(sb.toString())) {
            	element.appendChild(generateSimpleElement("keywords", sb.toString(), element.getOwnerDocument()));
            }
        }

        if (itunes.getSubtitle() != null) {
            element.appendChild(generateSimpleElement("subtitle", itunes.getSubtitle(), element.getOwnerDocument()));
        }

        if (itunes.getSummary() != null) {
            element.appendChild(generateSimpleElement("summary", itunes.getSummary(), element.getOwnerDocument()));
        }
    }

    /**
     * Returns the list of namespaces this module uses.
     *
     * @return set of Namespace objects.
     */
    @Override
    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    /**
     * Returns the namespace URI this module handles.
     *
     * @return Returns the namespace URI this module handles.
     */
    @Override
    public String getNamespaceUri() {
        return AbstractITunesObject.URI;
    }

    protected Element generateSimpleElement(final String name, final String value, final Document doc) {
        final Element element = doc.createElementNS(NAMESPACE.getNamespaceURI(), name);
        element.setPrefix(NAMESPACE.getPrefix());
        element.setTextContent(value);

        return element;
    }
}
