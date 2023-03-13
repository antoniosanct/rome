/*
 * Copyright 2006 Nathanial X. Freitas, openvision.tv
 *
 * This code is currently released under the Mozilla Public License.
 * http://www.mozilla.org/MPL/
 *
 * Alternately you may apply the terms of the Apache Software License
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.rometools.modules.mediarss.io;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.modules.georss.GMLGenerator;
import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.MediaModule;
import com.rometools.modules.mediarss.types.Category;
import com.rometools.modules.mediarss.types.Credit;
import com.rometools.modules.mediarss.types.Embed.Param;
import com.rometools.modules.mediarss.types.License;
import com.rometools.modules.mediarss.types.Location;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.MediaGroup;
import com.rometools.modules.mediarss.types.Metadata;
import com.rometools.modules.mediarss.types.PeerLink;
import com.rometools.modules.mediarss.types.PlayerReference;
import com.rometools.modules.mediarss.types.Price;
import com.rometools.modules.mediarss.types.Rating;
import com.rometools.modules.mediarss.types.Restriction;
import com.rometools.modules.mediarss.types.Scene;
import com.rometools.modules.mediarss.types.SubTitle;
import com.rometools.modules.mediarss.types.Tag;
import com.rometools.modules.mediarss.types.Text;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.modules.mediarss.types.UrlReference;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleGenerator;

/**
 * Generator for MediaRSS module.
 *
 */
public class MediaModuleGenerator extends ChildNavigator implements ModuleGenerator {

    private static final Namespace NS = ChildNavigator.createNamespace("media", MediaModule.URI);
    private static final Set<Namespace> NAMESPACES = new HashSet<Namespace>();

    static {
        NAMESPACES.add(NS);
        NAMESPACES.add(GeoRSSModule.SIMPLE_NS);
        NAMESPACES.add(GeoRSSModule.W3CGEO_NS);
        NAMESPACES.add(GeoRSSModule.GML_NS);
    }

    @Override
    public String getNamespaceUri() {
        return MediaModule.URI;
    }

    @Override
    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    @Override
    public void generate(final Module module, final Element element) {
        if (module instanceof MediaModule) {
            final MediaModule m = (MediaModule) module;
            generateMetadata(m.getMetadata(), element);
            generatePlayer(m.getPlayer(), element);
        }

        if (module instanceof MediaEntryModule) {
            final MediaEntryModule m = (MediaEntryModule) module;
            for (final MediaGroup element2 : m.getMediaGroups()) {
                generateGroup(element2, element);
            }
            for (final MediaContent element2 : m.getMediaContents()) {
                generateContent(element2, element);
            }
        }
    }

    public void generateContent(final MediaContent c, final Element e) {
        final Element mc = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "content");
        mc.setPrefix(NS.getPrefix());
        addNotNullAttribute(mc, "medium", c.getMedium());
        addNotNullAttribute(mc, "channels", c.getAudioChannels());
        addNotNullAttribute(mc, "bitrate", c.getBitrate());
        addNotNullAttribute(mc, "duration", c.getDuration());
        addNotNullAttribute(mc, "expression", c.getExpression());
        addNotNullAttribute(mc, "fileSize", c.getFileSize());
        addNotNullAttribute(mc, "framerate", c.getFramerate());
        addNotNullAttribute(mc, "height", c.getHeight());
        addNotNullAttribute(mc, "lang", c.getLanguage());
        addNotNullAttribute(mc, "samplingrate", c.getSamplingrate());
        addNotNullAttribute(mc, "type", c.getType());
        addNotNullAttribute(mc, "width", c.getWidth());

        if (c.isDefaultContent()) {
            addNotNullAttribute(mc, "isDefault", "true");
        }

        if (c.getReference() instanceof UrlReference) {
            addNotNullAttribute(mc, "url", c.getReference());
            generatePlayer(c.getPlayer(), mc);
        } else {
            generatePlayer(c.getPlayer(), mc);
        }

        generateMetadata(c.getMetadata(), mc);
        e.appendChild(mc);
    }

    public void generateGroup(final MediaGroup g, final Element e) {
        final Element t = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "group");
        final MediaContent[] c = g.getContents();

        for (final MediaContent element : c) {
            generateContent(element, t);
        }

        generateMetadata(g.getMetadata(), t);
        e.appendChild(t);
    }

    public void generateMetadata(final Metadata m, final Element e) {
        if (m == null) {
            return;
        }

        final Category[] cats = m.getCategories();

        for (final Category cat : cats) {
            final Element c = generateSimpleElement("category", cat.getValue(), e.getOwnerDocument());
            c.setPrefix(NS.getPrefix());
            addNotNullAttribute(c, "scheme", cat.getScheme());
            addNotNullAttribute(c, "label", cat.getLabel());
            e.appendChild(c);
        }

        final Element copyright = addNotNullElement(e, "copyright", m.getCopyright());
        addNotNullAttribute(copyright, "url", m.getCopyrightUrl());

        final Credit[] creds = m.getCredits();

        for (final Credit cred : creds) {
            final Element c = generateSimpleElement("credit", cred.getName(), e.getOwnerDocument());
            c.setPrefix(NS.getPrefix());
            addNotNullAttribute(c, "role", cred.getRole());
            addNotNullAttribute(c, "scheme", cred.getScheme());
            e.appendChild(c);
        }

        final Element desc = addNotNullElement(e, "description", m.getDescription());
        addNotNullAttribute(desc, "type", m.getDescriptionType());

        if (m.getHash() != null) {
            final Element hash = addNotNullElement(e, "hash", m.getHash().getValue());
            addNotNullAttribute(hash, "algo", m.getHash().getAlgorithm());
        }

        final String[] keywords = m.getKeywords();

        if (keywords.length > 0) {
            String keyword = keywords[0];

            for (int i = 1; i < keywords.length; i++) {
                keyword += ", " + keywords[i];
            }

            addNotNullElement(e, "keywords", keyword);
        }

        final Rating[] rats = m.getRatings();

        for (final Rating rat2 : rats) {
            final Element rat = addNotNullElement(e, "rating", rat2.getValue());
            addNotNullAttribute(rat, "scheme", rat2.getScheme());

            if (rat2.equals(Rating.ADULT)) {
                addNotNullElement(e, "adult", "true");
            } else if (rat2.equals(Rating.NONADULT)) {
                addNotNullElement(e, "adult", "false");
            }
        }

        final Text[] text = m.getText();

        for (final Text element : text) {
            final Element t = addNotNullElement(e, "text", element.getValue());
            addNotNullAttribute(t, "type", element.getType());
            addNotNullAttribute(t, "start", element.getStart());
            addNotNullAttribute(t, "end", element.getEnd());
        }

        final Element title = addNotNullElement(e, "title", m.getTitle());
        addNotNullAttribute(title, "type", m.getTitleType());

        generateBackLinks(m, e);
        generateComments(m, e);
        generateCommunity(m, e);
        generateEmbed(m, e);
        generateLicenses(m, e);
        generateLocations(m, e);
        generatePeerLinks(m, e);
        generatePrices(m, e);
        generateResponses(m, e);
        final Restriction[] r = m.getRestrictions();
        for (final Restriction element : r) {
            final Element res = addNotNullElement(e, "restriction", element.getValue());
            addNotNullAttribute(res, "type", element.getType());
            addNotNullAttribute(res, "relationship", element.getRelationship());
        }
        if (m.getRights() != null) {
            final Element rights = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "rights");
            rights.setPrefix(NS.getPrefix());
            rights.setAttribute("status", m.getRights().name());
            e.appendChild(rights);
        }
        generateScenes(m, e);
        generateStatus(m, e);
        generateSubTitles(m, e);
        generateThumbails(m, e);
    }

    /**
     * Generation of thumbnail tags.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateThumbails(final Metadata m, final Element e) {
        for (final Thumbnail thumb : m.getThumbnail()) {
            final Element t = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "thumbnail");
            t.setPrefix(NS.getPrefix());
            addNotNullAttribute(t, "url", thumb.getUrl());
            addNotNullAttribute(t, "width", thumb.getWidth());
            addNotNullAttribute(t, "height", thumb.getHeight());
            addNotNullAttribute(t, "time", thumb.getTime());
            e.appendChild(t);
        }
    }

    /**
     * Generation of backLinks tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateBackLinks(final Metadata m, final Element e) {
        final Element backLinksElements = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "backLinks");
        backLinksElements.setPrefix(NS.getPrefix());
        for (final URL backLink : m.getBackLinks()) {
            addNotNullElement(backLinksElements, "backLink", backLink);
        }
        if (!super.getChildren(backLinksElements).isEmpty()) {
            e.appendChild(backLinksElements);
        }
    }

    /**
     * Generation of comments tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateComments(final Metadata m, final Element e) {
        final Element commentsElements = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "comments");
        commentsElements.setPrefix(NS.getPrefix());
        for (final String comment : m.getComments()) {
            addNotNullElement(commentsElements, "comment", comment);
        }
        if (!super.getChildren(commentsElements).isEmpty()) {
            e.appendChild(commentsElements);
        }
    }

    /**
     * Generation of community tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateCommunity(final Metadata m, final Element e) {
        if (m.getCommunity() == null) {
            return;
        }
        final Element communityElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "community");
        communityElement.setPrefix(NS.getPrefix());
        if (m.getCommunity().getStarRating() != null) {
            final Element starRatingElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "starRating");
            starRatingElement.setPrefix(NS.getPrefix());
            addNotNullAttribute(starRatingElement, "average", m.getCommunity().getStarRating().getAverage());
            addNotNullAttribute(starRatingElement, "count", m.getCommunity().getStarRating().getCount());
            addNotNullAttribute(starRatingElement, "min", m.getCommunity().getStarRating().getMin());
            addNotNullAttribute(starRatingElement, "max", m.getCommunity().getStarRating().getMax());
            if (starRatingElement.hasAttributes()) {
            	communityElement.appendChild(starRatingElement);
            }
        }
        if (m.getCommunity().getStatistics() != null) {
            final Element statisticsElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "statistics");
            statisticsElement.setPrefix(NS.getPrefix());
            addNotNullAttribute(statisticsElement, "views", m.getCommunity().getStatistics().getViews());
            addNotNullAttribute(statisticsElement, "favorites", m.getCommunity().getStatistics().getFavorites());
            if (statisticsElement.hasAttributes()) {
            	communityElement.appendChild(statisticsElement);
            }
        }
        if (m.getCommunity().getTags() != null && !m.getCommunity().getTags().isEmpty()) {
            final Element tagsElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "tags");
            tagsElement.setPrefix(NS.getPrefix());
            StringBuffer sb = new StringBuffer("");
            for (final Tag tag : m.getCommunity().getTags()) {
                if (!"".equals(sb.toString())) {
                	sb.append(", ");
                }
                if (tag.getWeight() == null) {
                	sb.append(tag.getName());
                } else {
                	sb.append(tag.getName());
                	sb.append(": ");
                    sb.append(String.valueOf(tag.getWeight()));
                }
            }
            tagsElement.setTextContent(sb.toString());
            if (!tagsElement.getTextContent().isEmpty()) {
            	communityElement.appendChild(tagsElement);
            }
        }
        if (!super.getChildren(communityElement).isEmpty()) {
            e.appendChild(communityElement);
        }
    }

    /**
     * Generation of embed tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateEmbed(final Metadata m, final Element e) {
        if (m.getEmbed() == null) {
            return;
        }
        final Element embedElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "embed");
        embedElement.setPrefix(NS.getPrefix());
        addNotNullAttribute(embedElement, "url", m.getEmbed().getUrl());
        addNotNullAttribute(embedElement, "width", m.getEmbed().getWidth());
        addNotNullAttribute(embedElement, "height", m.getEmbed().getHeight());
        for (final Param param : m.getEmbed().getParams()) {
            final Element paramElement = addNotNullElement(embedElement, "param", param.getValue());
            if (paramElement != null) {
                addNotNullAttribute(paramElement, "name", param.getName());
            }
        }
        if (embedElement.hasAttributes() || !super.getChildren(embedElement).isEmpty()) {
            e.appendChild(embedElement);
        }
    }

    /**
     * Generation of scenes tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateScenes(final Metadata m, final Element e) {
        final Element scenesElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "scenes");
        scenesElement.setPrefix(NS.getPrefix());
        for (final Scene scene : m.getScenes()) {
            final Element sceneElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "scene");
            sceneElement.setPrefix(NS.getPrefix());
            addNotNullElement(sceneElement, "sceneTitle", scene.getTitle());
            addNotNullElement(sceneElement, "sceneDescription", scene.getDescription());
            addNotNullElement(sceneElement, "sceneStartTime", scene.getStartTime());
            addNotNullElement(sceneElement, "sceneEndTime", scene.getEndTime());
            if (!super.getChildren(sceneElement).isEmpty()) {
            	scenesElement.appendChild(sceneElement);
            }
        }
        if (!super.getChildren(scenesElement).isEmpty()) {
            e.appendChild(scenesElement);
        }
    }

    /**
     * Generation of location tags.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateLocations(final Metadata m, final Element e) {
        final GMLGenerator geoRssGenerator = new GMLGenerator();
        for (final Location location : m.getLocations()) {
            final Element locationElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "location");
            locationElement.setPrefix(NS.getPrefix());
            addNotNullAttribute(locationElement, "description", location.getDescription());
            addNotNullAttribute(locationElement, "start", location.getStart());
            addNotNullAttribute(locationElement, "end", location.getEnd());
            if (location.getGeoRss() != null) {
                geoRssGenerator.generate(location.getGeoRss(), locationElement);
            }
            if (locationElement.hasAttributes() || !super.getChildren(locationElement).isEmpty()) {
                e.appendChild(locationElement);
            }
        }
    }

    /**
     * Generation of peerLink tags.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generatePeerLinks(final Metadata m, final Element e) {
        for (final PeerLink peerLink : m.getPeerLinks()) {
            final Element peerLinkElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "peerLink");
            peerLinkElement.setPrefix(NS.getPrefix());
            addNotNullAttribute(peerLinkElement, "type", peerLink.getType());
            addNotNullAttribute(peerLinkElement, "href", peerLink.getHref());
            if (peerLinkElement.hasAttributes()) {
                e.appendChild(peerLinkElement);
            }
        }
    }

    /**
     * Generation of subTitle tags.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateSubTitles(final Metadata m, final Element e) {
        for (final SubTitle subTitle : m.getSubTitles()) {
            final Element subTitleElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "subTitle");
            subTitleElement.setPrefix(NS.getPrefix());
            addNotNullAttribute(subTitleElement, "type", subTitle.getType());
            addNotNullAttribute(subTitleElement, "lang", subTitle.getLang());
            addNotNullAttribute(subTitleElement, "href", subTitle.getHref());
            if (subTitleElement.hasAttributes()) {
                e.appendChild(subTitleElement);
            }
        }
    }

    /**
     * Generation of license tags.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateLicenses(final Metadata m, final Element e) {
        for (final License license : m.getLicenses()) {
            final Element licenseElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "license");
            licenseElement.setPrefix(NS.getPrefix());
            addNotNullAttribute(licenseElement, "type", license.getType());
            addNotNullAttribute(licenseElement, "href", license.getHref());
            if (license.getValue() != null) {
            	licenseElement.setTextContent(license.getValue());
            }
            if (licenseElement.hasAttributes() || !licenseElement.getTextContent().isEmpty()) {
                e.appendChild(licenseElement);
            }
        }
    }

    /**
     * Generation of backLinks tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generatePrices(final Metadata m, final Element e) {
        for (final Price price : m.getPrices()) {
            if (price == null) {
                continue;
            }
            final Element priceElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "price");
            priceElement.setPrefix(NS.getPrefix());
            if (price.getType() != null) {
                priceElement.setAttribute("type", price.getType().name().toLowerCase());
            }
            addNotNullAttribute(priceElement, "info", price.getInfo());
            addNotNullAttribute(priceElement, "price", price.getPrice());
            addNotNullAttribute(priceElement, "currency", price.getCurrency());
            if (priceElement.hasAttributes()) {
                e.appendChild(priceElement);
            }
        }
    }

    /**
     * Generation of responses tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateResponses(final Metadata m, final Element e) {
        if (m.getResponses() == null || m.getResponses().length == 0) {
            return;
        }
        final Element responsesElements = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "responses");
        responsesElements.setPrefix(NS.getPrefix());
        for (final String response : m.getResponses()) {
            addNotNullElement(responsesElements, "response", response);
        }
        e.appendChild(responsesElements);
    }

    /**
     * Generation of status tag.
     * 
     * @param m source
     * @param e element to attach new element to
     */
    private void generateStatus(final Metadata m, final Element e) {
        if (m.getStatus() == null) {
            return;
        }
        final Element statusElement = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "status");
        statusElement.setPrefix(NS.getPrefix());
        if (m.getStatus().getState() != null) {
            statusElement.setAttribute("state", m.getStatus().getState().name());
        }
        addNotNullAttribute(statusElement, "reason", m.getStatus().getReason());
        if (statusElement.hasAttributes()) {
            e.appendChild(statusElement);
        }
    }

    public void generatePlayer(final PlayerReference p, final Element e) {
        if (p == null) {
            return;
        }

        final Element t = e.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "player");
        t.setPrefix(NS.getPrefix());
        addNotNullAttribute(t, "url", p.getUrl());
        addNotNullAttribute(t, "width", p.getWidth());
        addNotNullAttribute(t, "height", p.getHeight());
        e.appendChild(t);
    }

    protected void addNotNullAttribute(final Element target, final String name, final Object value) {
        if (target == null || value == null || "".equals(value)) {
            return;
        } else {
            target.setAttribute(name, value.toString());
        }
    }

    protected Element addNotNullElement(final Element target, final String name, final Object value) {
        if (value == null) {
            return null;
        } else {
            final Element e = generateSimpleElement(name, value.toString(), target.getOwnerDocument());
            target.appendChild(e);

            return e;
        }
    }

    protected Element generateSimpleElement(final String name, final String value, final Document parent) {
        final Element element = parent.createElementNS(NS.getNamespaceURI(), name);
        element.setPrefix(NS.getPrefix());
        element.setTextContent(value);

        return element;
    }
}

