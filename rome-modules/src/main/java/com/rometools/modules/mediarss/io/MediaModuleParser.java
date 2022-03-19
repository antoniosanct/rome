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

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.SimpleParser;
import com.rometools.modules.mediarss.MediaEntryModuleImpl;
import com.rometools.modules.mediarss.MediaModule;
import com.rometools.modules.mediarss.MediaModuleImpl;
import com.rometools.modules.mediarss.types.Category;
import com.rometools.modules.mediarss.types.Community;
import com.rometools.modules.mediarss.types.Credit;
import com.rometools.modules.mediarss.types.Embed;
import com.rometools.modules.mediarss.types.Embed.Param;
import com.rometools.modules.mediarss.types.Expression;
import com.rometools.modules.mediarss.types.Hash;
import com.rometools.modules.mediarss.types.License;
import com.rometools.modules.mediarss.types.Location;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.MediaGroup;
import com.rometools.modules.mediarss.types.Metadata;
import com.rometools.modules.mediarss.types.Metadata.RightsStatus;
import com.rometools.modules.mediarss.types.PeerLink;
import com.rometools.modules.mediarss.types.PlayerReference;
import com.rometools.modules.mediarss.types.Price;
import com.rometools.modules.mediarss.types.Rating;
import com.rometools.modules.mediarss.types.Restriction;
import com.rometools.modules.mediarss.types.Scene;
import com.rometools.modules.mediarss.types.StarRating;
import com.rometools.modules.mediarss.types.Statistics;
import com.rometools.modules.mediarss.types.Status;
import com.rometools.modules.mediarss.types.SubTitle;
import com.rometools.modules.mediarss.types.Tag;
import com.rometools.modules.mediarss.types.Text;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.modules.mediarss.types.Time;
import com.rometools.modules.mediarss.types.UrlReference;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;
import com.rometools.utils.Doubles;
import com.rometools.utils.Integers;
import com.rometools.utils.Longs;
import com.rometools.utils.Strings;
import com.rometools.utils.URIs;

/**
 * @author Nathanial X. Freitas
 * 
 */
public class MediaModuleParser extends ChildNavigator implements ModuleParser {

    private static final Logger LOG = LoggerFactory.getLogger(MediaModuleParser.class);

    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace(MediaModule.URI);

    private static final Pattern FILESIZE_WITH_UNIT_PATTERN = Pattern.compile("([\\d,.]+)([TGMK])?B", Pattern.CASE_INSENSITIVE);

    @Override
    public String getNamespaceUri() {
        return MediaModule.URI;
    }

    @Override
    public Module parse(final Element mmRoot, final Locale locale) {
        MediaModuleImpl mod = null;

        if (mmRoot.getLocalName().equals("channel") || mmRoot.getLocalName().equals("feed")) {
            mod = new MediaModuleImpl();
        } else {
            mod = new MediaEntryModuleImpl();
        }

        mod.setMetadata(parseMetadata(mmRoot, locale));
        mod.setPlayer(parsePlayer(mmRoot));

        if (mod instanceof MediaEntryModuleImpl) {
            final MediaEntryModuleImpl m = (MediaEntryModuleImpl) mod;
            m.setMediaContents(parseContent(mmRoot, locale));
            m.setMediaGroups(parseGroup(mmRoot, locale));
        }

        return mod;
    }

    static long parseFileSize(String fileSizeAttrValue) {
        String nonWSFileSize = fileSizeAttrValue.replaceAll("\\s", "");

        if(nonWSFileSize.matches("\\d+")) {
            return Long.valueOf(nonWSFileSize);
        }

        Matcher sizeWithUnitMatcher = FILESIZE_WITH_UNIT_PATTERN.matcher(nonWSFileSize);
        if (sizeWithUnitMatcher.matches()) {
            BigDecimal number = new BigDecimal(sizeWithUnitMatcher.group(1).replace(',', '.'));
            BigDecimal multiplier = BigDecimal.valueOf(1);
            if (sizeWithUnitMatcher.group(2) != null) {
                char unit  = sizeWithUnitMatcher.group(2).toLowerCase().charAt(0);
                if (unit == 'k') {
                    multiplier = BigDecimal.valueOf(1000);
                } else if (unit == 'm') {
                    multiplier = BigDecimal.valueOf(1000).pow(2);
                } else if (unit == 'g') {
                    multiplier = BigDecimal.valueOf(1000).pow(3);
                } else if (unit == 't') {
                    multiplier = BigDecimal.valueOf(1000).pow(4);
                }
            }
            return number.multiply(multiplier).longValue();
        }

        throw new NumberFormatException("Invalid file size: " + fileSizeAttrValue);
    }

    /**
     * @param e element to parse
     * @param locale locale for parsing
     * @return array of media:content elements
     */
    private MediaContent[] parseContent(final Element e, final Locale locale) {

        final List<Element> contents = super.getChildren(e, "content", getNS());
        final ArrayList<MediaContent> values = new ArrayList<MediaContent>();

        try {
            for (int i = 0; contents != null && i < contents.size(); i++) {
                final Element content = contents.get(i);
                MediaContent mc = null;

                if (content.getAttribute("url") != null) {
                    try {
                        mc = new MediaContent(new UrlReference(URIs.parse(content.getAttribute("url"))));
                        mc.setPlayer(parsePlayer(content));
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }
                } else {
                    mc = new MediaContent(parsePlayer(content));
                }
                if (mc != null) {
                    values.add(mc);
                    try {
                        if (content.getAttribute("channels") != null) {
                            mc.setAudioChannels(Integer.valueOf(content.getAttribute("channels")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }
                    try {
                        if (content.getAttribute("bitrate") != null) {
                            mc.setBitrate(Float.valueOf(content.getAttribute("bitrate")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }
                    try {
                        if (content.getAttribute("duration") != null) {
                            mc.setDuration(Longs.parseDecimal(content.getAttribute("duration")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }

                    mc.setMedium(content.getAttribute("medium"));

                    final String expression = content.getAttribute("expression");

                    if (expression != null) {
                        if (expression.equalsIgnoreCase("full")) {
                            mc.setExpression(Expression.FULL);
                        } else if (expression.equalsIgnoreCase("sample")) {
                            mc.setExpression(Expression.SAMPLE);
                        } else if (expression.equalsIgnoreCase("nonstop")) {
                            mc.setExpression(Expression.NONSTOP);
                        }
                    }

                    try {
                        if (content.getAttribute("fileSize") != null) {
                            mc.setFileSize(parseFileSize(content.getAttribute("fileSize")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }
                    try {
                        if (content.getAttribute("framerate") != null) {
                            mc.setFramerate(Float.valueOf(content.getAttribute("framerate")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }
                    try {
                        if (content.getAttribute("height") != null) {
                            mc.setHeight(Integer.valueOf(content.getAttribute("height")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }

                    mc.setLanguage(content.getAttribute("lang"));
                    mc.setMetadata(parseMetadata(content, locale));
                    try {
                        if (content.getAttribute("samplingrate") != null) {
                            mc.setSamplingrate(Float.valueOf(content.getAttribute("samplingrate")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }

                    mc.setType(content.getAttribute("type"));
                    try {
                        if (content.getAttribute("width") != null) {
                            mc.setWidth(Integer.valueOf(content.getAttribute("width")));
                        }
                    } catch (final Exception ex) {
                        LOG.warn("Exception parsing content tag.", ex);
                    }

                    if (content.getAttribute("isDefault") != null) {
                        mc.setDefaultContent(Boolean.valueOf(content.getAttribute("isDefault")));
                    }
                } else {
                    LOG.warn("Could not find MediaContent.");
                }

            }
        } catch (final Exception ex) {
            LOG.warn("Exception parsing content tag.", ex);
        }

        return values.toArray(new MediaContent[values.size()]);
    }

    /**
     * @param e element to parse
     * @param locale locale for parsing
     * @return array of media:group elements
     */
    private MediaGroup[] parseGroup(final Element e, final Locale locale) {
        final List<Element> groups = super.getChildren(e, "group", getNS());
        final ArrayList<MediaGroup> values = new ArrayList<MediaGroup>();

        for (int i = 0; groups != null && i < groups.size(); i++) {
            final Element group = groups.get(i);
            final MediaGroup g = new MediaGroup(parseContent(group, locale));

            for (int j = 0; j < g.getContents().length; j++) {
                if (g.getContents()[j].isDefaultContent()) {
                    g.setDefaultContentIndex(Integer.valueOf(j));

                    break;
                }
            }

            g.setMetadata(parseMetadata(group, locale));
            values.add(g);
        }

        return values.toArray(new MediaGroup[values.size()]);
    }

    /**
     * @param e element to parse
     * @param locale locale for parsing
     * @return Metadata of media:group or media:content
     */
    private Metadata parseMetadata(final Element e, final Locale locale) {
        final Metadata md = new Metadata();
        parseCategories(e, md);
        parseCopyright(e, md);
        parseCredits(e, md);
        parseDescription(e, md);
        parseHash(e, md);
        parseKeywords(e, md);
        parseRatings(e, md);
        parseText(e, md);
        parseThumbnail(e, md);
        parseTitle(e, md);
        parseRestrictions(e, md);
        parseAdultMetadata(e, md);
        parseBackLinks(e, md);
        parseComments(e, md);
        parseCommunity(e, md);
        parsePrices(e, md);
        parseResponses(e, md);
        parseStatus(e, md);
        parseEmbed(e, md);
        parseLicenses(e, md);
        parseSubTitles(e, md);
        parsePeerLinks(e, md);
        parseRights(e, md);
        parseLocations(e, md, locale);
        parseScenes(e, md);
        return md;
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseScenes(final Element e, final Metadata md) {
        final Element scenesElement = super.getChild(e, "scenes", getNS());
        if (scenesElement != null) {
            final List<Element> sceneElements = super.getChildren(scenesElement, "scene", getNS());
            final Scene[] scenes = new Scene[sceneElements.size()];
            for (int i = 0; i < sceneElements.size(); i++) {
                scenes[i] = new Scene();
                scenes[i].setTitle(super.getChild(sceneElements.get(i), "sceneTitle", getNS()).getTextContent());
                scenes[i].setDescription(super.getChild(sceneElements.get(i), "sceneDescription", getNS()).getTextContent());
                final String sceneStartTime = super.getChild(sceneElements.get(i), "sceneStartTime", getNS()).getTextContent();
                if (sceneStartTime != null) {
                    scenes[i].setStartTime(new Time(sceneStartTime));
                }
                final String sceneEndTime = super.getChild(sceneElements.get(i), "sceneEndTime", getNS()).getTextContent();
                if (sceneEndTime != null) {
                    scenes[i].setEndTime(new Time(sceneEndTime));
                }
            }
            md.setScenes(scenes);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     * @param locale locale for parser
     */
    private void parseLocations(final Element e, final Metadata md, final Locale locale) {
        final List<Element> locationElements = super.getChildren(e, "location", getNS());
        final Location[] locations = new Location[locationElements.size()];
        final SimpleParser geoRssParser = new SimpleParser();
        for (int i = 0; i < locationElements.size(); i++) {
            locations[i] = new Location();
            locations[i].setDescription(locationElements.get(i).getAttribute("description"));
            if (locationElements.get(i).getAttribute("start") != null) {
                locations[i].setStart(new Time(locationElements.get(i).getAttribute("start")));
            }
            if (locationElements.get(i).getAttribute("end") != null) {
                locations[i].setEnd(new Time(locationElements.get(i).getAttribute("end")));
            }
            final Module geoRssModule = geoRssParser.parse(locationElements.get(i), locale);
            if (geoRssModule != null && geoRssModule instanceof GeoRSSModule) {
                locations[i].setGeoRss((GeoRSSModule) geoRssModule);
            }
        }
        md.setLocations(locations);
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseRights(final Element e, final Metadata md) {
        final Element rightsElement = super.getChild(e, "rights", getNS());
        if (rightsElement != null && rightsElement.getAttribute("status") != null) {
            md.setRights(RightsStatus.valueOf(rightsElement.getAttribute("status")));
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parsePeerLinks(final Element e, final Metadata md) {
        final List<Element> peerLinkElements = super.getChildren(e, "peerLink", getNS());
        final PeerLink[] peerLinks = new PeerLink[peerLinkElements.size()];
        for (int i = 0; i < peerLinkElements.size(); i++) {
            peerLinks[i] = new PeerLink();
            peerLinks[i].setType(peerLinkElements.get(i).getAttribute("type"));
            if (peerLinkElements.get(i).getAttribute("href") != null) {
                try {
                    peerLinks[i].setHref(new URL(peerLinkElements.get(i).getAttribute("href")));
                } catch (MalformedURLException ex) {
                    LOG.warn("Exception parsing peerLink href attribute.", ex);
                }
            }
        }
        md.setPeerLinks(peerLinks);
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseSubTitles(final Element e, final Metadata md) {
        final List<Element> subTitleElements = super.getChildren(e, "subTitle", getNS());
        final SubTitle[] subtitles = new SubTitle[subTitleElements.size()];
        for (int i = 0; i < subTitleElements.size(); i++) {
            subtitles[i] = new SubTitle();
            subtitles[i].setType(subTitleElements.get(i).getAttribute("type"));
            subtitles[i].setLang(subTitleElements.get(i).getAttribute("lang"));
            if (subTitleElements.get(i).getAttribute("href") != null) {
                try {
                    subtitles[i].setHref(new URL(subTitleElements.get(i).getAttribute("href")));
                } catch (MalformedURLException ex) {
                    LOG.warn("Exception parsing subTitle href attribute.", ex);
                }
            }
        }
        md.setSubTitles(subtitles);
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseLicenses(final Element e, final Metadata md) {
        final List<Element> licenseElements = super.getChildren(e, "license", getNS());
        final License[] licenses = new License[licenseElements.size()];
        for (int i = 0; i < licenseElements.size(); i++) {
            licenses[i] = new License();
            licenses[i].setType(licenseElements.get(i).getAttribute("type"));
            licenses[i].setValue(licenseElements.get(i).getTextContent().trim());
            if (licenseElements.get(i).getAttribute("href") != null) {
                try {
                    licenses[i].setHref(new URL(licenseElements.get(i).getAttribute("href")));
                } catch (MalformedURLException ex) {
                    LOG.warn("Exception parsing license href attribute.", ex);
                }
            }
        }
        md.setLicenses(licenses);
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parsePrices(final Element e, final Metadata md) {
        final List<Element> priceElements = super.getChildren(e, "price", getNS());
        final Price[] prices = new Price[priceElements.size()];
        for (int i = 0; i < priceElements.size(); i++) {
            
            final Element priceElement = priceElements.get(i);
            prices[i] = new Price();
            
            final String currency = priceElement.getAttribute("currency");
            if (currency != null) {
                try {
                    prices[i].setCurrency(Currency.getInstance(currency));
                } catch (IllegalArgumentException ex) {
                    LOG.warn("Invalid currency", ex);
                }
            }

            final String price = priceElement.getAttribute("price");
            if (price != null) {
                try {
                    prices[i].setPrice(new BigDecimal(price));
                } catch (NumberFormatException ex) {
                    LOG.warn("Invalid price", ex);
                }
            }
            
            if (priceElement.getAttribute("type") != null) {
                prices[i].setType(Price.Type.valueOf(priceElement.getAttribute("type").toUpperCase()));
            }
            if (priceElement.getAttribute("info") != null) {
                try {
                    prices[i].setInfo(new URL(priceElement.getAttribute("info")));
                } catch (MalformedURLException ex) {
                    LOG.warn("Exception parsing price info attribute.", ex);
                }
            }
        }
        md.setPrices(prices);
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseStatus(final Element e, final Metadata md) {
        final Element statusElement = super.getChild(e, "status", getNS());
        if (statusElement != null) {
            final Status status = new Status();
            status.setState(Status.State.valueOf(statusElement.getAttribute("state")));
            status.setReason(statusElement.getAttribute("reason"));
            md.setStatus(status);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseBackLinks(final Element e, final Metadata md) {
        final Element backLinksElement = super.getChild(e, "backLinks", getNS());
        if (backLinksElement != null) {
            final List<Element> backLinkElements = super.getChildren(backLinksElement, "backLink", getNS());
            final URL[] backLinks = new URL[backLinkElements.size()];
            for (int i = 0; i < backLinkElements.size(); i++) {
                try {
                    backLinks[i] = new URL(backLinkElements.get(i).getTextContent().trim());
                } catch (MalformedURLException ex) {
                    LOG.warn("Exception parsing backLink tag.", ex);
                }
            }
            md.setBackLinks(backLinks);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseComments(final Element e, final Metadata md) {
        final Element commentsElement = super.getChild(e, "comments", getNS());
        if (commentsElement != null) {
            final List<Element> commentElements = super.getChildren(commentsElement, "comment", getNS());
            final String[] comments = new String[commentElements.size()];
            for (int i = 0; i < commentElements.size(); i++) {
                comments[i] = commentElements.get(i).getTextContent().trim();
            }
            md.setComments(comments);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseResponses(final Element e, final Metadata md) {
        final Element responsesElement = super.getChild(e, "responses", getNS());
        if (responsesElement != null) {
            final List<Element> responseElements = super.getChildren(responsesElement, "response", getNS());
            final String[] responses = new String[responseElements.size()];
            for (int i = 0; i < responseElements.size(); i++) {
                responses[i] = responseElements.get(i).getTextContent().trim();
            }
            md.setResponses(responses);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseCommunity(final Element e, final Metadata md) {
        final Element communityElement = super.getChild(e, "community", getNS());
        if (communityElement != null) {
            final Community community = new Community();
            final Element starRatingElement = super.getChild(communityElement, "starRating", getNS());
            if (starRatingElement != null) {
                final StarRating starRating = new StarRating();
                starRating.setAverage(Doubles.parse(starRatingElement.getAttribute("average")));
                starRating.setCount(Integers.parse(starRatingElement.getAttribute("count")));
                starRating.setMax(Integers.parse(starRatingElement.getAttribute("max")));
                starRating.setMin(Integers.parse(starRatingElement.getAttribute("min")));
                community.setStarRating(starRating);
            }
            final Element statisticsElement = super.getChild(communityElement, "statistics", getNS());
            if (statisticsElement != null) {
                final Statistics statistics = new Statistics();
                statistics.setFavorites(Integers.parse(statisticsElement.getAttribute("favorites")));
                statistics.setViews(Integers.parse(statisticsElement.getAttribute("views")));
                community.setStatistics(statistics);
            }
            final Element tagsElement = super.getChild(communityElement, "tags", getNS());
            if (tagsElement != null) {
                final String tagsText = tagsElement.getTextContent().trim();
                final String[] tags = tagsText.split(",");
                for (String tagText : tags) {
                    final String[] tagParts = tagText.trim().split(":");
                    final Tag tag = new Tag(tagParts[0].trim());
                    if (tagParts.length > 1) {
                        tag.setWeight(Integers.parse(tagParts[1].trim()));
                    }
                    community.getTags().add(tag);
                }
            }
            md.setCommunity(community);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseCategories(final Element e, final Metadata md) {
        final List<Element> categories = super.getChildren(e, "category", getNS());
        final ArrayList<Category> values = new ArrayList<Category>();

        for (int i = 0; categories != null && i < categories.size(); i++) {
            try {
                final Element cat = categories.get(i);
                values.add(new Category(cat.getAttribute("scheme"), cat.getAttribute("label"), cat.getTextContent()));
            } catch (final Exception ex) {
                LOG.warn("Exception parsing category tag.", ex);
            }
        }

        md.setCategories(values.toArray(new Category[values.size()]));
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseCopyright(final Element e, final Metadata md) {
        try {
            final Element copy = super.getChild(e, "copyright", getNS());

            if (copy != null) {
                md.setCopyright(copy.getTextContent());
                if (copy.getAttribute("url") != null) {
                    md.setCopyrightUrl(new URI(copy.getAttribute("url")));
                }
            }
        } catch (final Exception ex) {
            LOG.warn("Exception parsing copyright tag.", ex);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseCredits(final Element e, final Metadata md) {
        final List<Element> credits = super.getChildren(e, "credit", getNS());
        final ArrayList<Credit> values = new ArrayList<Credit>();

        for (int i = 0; credits != null && i < credits.size(); i++) {
            try {
                final Element cred = credits.get(i);
                values.add(new Credit(cred.getAttribute("scheme"), cred.getAttribute("role"), cred.getTextContent()));
                md.setCredits(values.toArray(new Credit[values.size()]));
            } catch (final Exception ex) {
                LOG.warn("Exception parsing credit tag.", ex);
            }
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseDescription(final Element e, final Metadata md) {
        try {
            final Element description = super.getChild(e, "description", getNS());

            if (description != null) {
                md.setDescription(description.getTextContent());
                md.setDescriptionType(description.getAttribute("type"));
            }
        } catch (final Exception ex) {
            LOG.warn("Exception parsing description tag.", ex);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseEmbed(final Element e, final Metadata md) {
        final Element embedElement = super.getChild(e, "embed", getNS());
        if (embedElement != null) {
            final Embed embed = new Embed();
            embed.setWidth(Integers.parse(embedElement.getAttribute("width")));
            embed.setHeight(Integers.parse(embedElement.getAttribute("height")));
            if (embedElement.getAttribute("url") != null) {
                try {
                    embed.setUrl(new URL(embedElement.getAttribute("url")));
                } catch (MalformedURLException ex) {
                    LOG.warn("Exception parsing embed tag.", ex);
                }
            }
            final List<Element> paramElements = super.getChildren(embedElement, "param", getNS());
            embed.setParams(new Param[paramElements.size()]);
            for (int i = 0; i < paramElements.size(); i++) {
                embed.getParams()[i] = new Param(paramElements.get(i).getAttribute("name"), paramElements.get(i).getTextContent().trim());
            }
            md.setEmbed(embed);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseHash(final Element e, final Metadata md) {
        try {
            final Element hash = super.getChild(e, "hash", getNS());

            if (hash != null) {
                md.setHash(new Hash(hash.getAttribute("algo"), hash.getTextContent()));
            }
        } catch (final Exception ex) {
            LOG.warn("Exception parsing hash tag.", ex);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseKeywords(final Element e, final Metadata md) {
        final Element keywords = super.getChild(e, "keywords", getNS());

        if (keywords != null) {
            final StringTokenizer tok = new StringTokenizer(keywords.getTextContent(), ",");
            final String[] value = new String[tok.countTokens()];

            for (int i = 0; tok.hasMoreTokens(); i++) {
                value[i] = tok.nextToken().trim();
            }

            md.setKeywords(value);
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseRatings(final Element e, final Metadata md) {
        final ArrayList<Rating> values = new ArrayList<Rating>();

        final List<Element> ratings = super.getChildren(e, "rating", getNS());
        for (final Element ratingElement : ratings) {
            try {
                final String ratingText = ratingElement.getTextContent();
                String ratingScheme = Strings.trimToNull(ratingElement.getAttribute("scheme"));
                if (ratingScheme == null) {
                    ratingScheme = "urn:simple";
                }
                final Rating rating = new Rating(ratingScheme, ratingText);
                values.add(rating);
            } catch (final Exception ex) {
                LOG.warn("Exception parsing rating tag.", ex);
            }
        }

        md.setRatings(values.toArray(new Rating[values.size()]));

    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseText(final Element e, final Metadata md) {
        final List<Element> texts = super.getChildren(e, "text", getNS());
        final ArrayList<Text> values = new ArrayList<Text>();

        for (int i = 0; texts != null && i < texts.size(); i++) {
            try {
                final Element text = texts.get(i);
                Time start = null;
                if (text.getAttribute("start") != null) {
                    start = new Time(text.getAttribute("start"));
                }
                Time end = null;
                if (text.getAttribute("end") != null) {
                    end = new Time(text.getAttribute("end"));
                }
                values.add(new Text(text.getAttribute("type"), text.getTextContent().trim(), start, end));
            } catch (final Exception ex) {
                LOG.warn("Exception parsing text tag.", ex);
            }
        }

        md.setText(values.toArray(new Text[values.size()]));
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseThumbnail(final Element e, final Metadata md) {
        final ArrayList<Thumbnail> values = new ArrayList<Thumbnail>();

        final List<Element> thumbnails = super.getChildren(e, "thumbnail", getNS());
        for (final Element thumb : thumbnails) {
            try {

                final String timeAttr = Strings.trimToNull(thumb.getAttribute("time"));
                Time time = null;
                if (timeAttr != null) {
                    time = new Time(timeAttr);
                }

                final String widthAttr = thumb.getAttribute("width");
                final Integer width = Integers.parse(widthAttr);

                final String heightAttr = thumb.getAttribute("height");
                final Integer height = Integers.parse(heightAttr);

                final String url = thumb.getAttribute("url");
                final URI uri = new URI(url);
                final Thumbnail thumbnail = new Thumbnail(uri, width, height, time);

                values.add(thumbnail);

            } catch (final Exception ex) {
                LOG.warn("Exception parsing thumbnail tag.", ex);
            }
        }

        md.setThumbnail(values.toArray(new Thumbnail[values.size()]));
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseTitle(final Element e, final Metadata md) {
        final Element title = super.getChild(e, "title", getNS());

        if (title != null) {
            md.setTitle(title.getTextContent());
            md.setTitleType(title.getAttribute("type"));
        }
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseRestrictions(final Element e, final Metadata md) {
        final List<Element> restrictions = super.getChildren(e, "restriction", getNS());
        final ArrayList<Restriction> values = new ArrayList<Restriction>();

        for (int i = 0; i < restrictions.size(); i++) {
            final Element r = restrictions.get(i);

            Restriction.Type type = null;
            String restrictionType = r.getAttribute("type");
            if (restrictionType != null) {
                if (restrictionType.equalsIgnoreCase(Restriction.Type.URI.toString())){
                    type = Restriction.Type.URI;
                } else if (restrictionType.equalsIgnoreCase(Restriction.Type.COUNTRY.toString())) {
                    type = Restriction.Type.COUNTRY;
                } else if (restrictionType.equalsIgnoreCase(Restriction.Type.SHARING.toString())) {
                    type = Restriction.Type.SHARING;
                }
            }

            Restriction.Relationship relationship = null;

            if (r.getAttribute("relationship").equalsIgnoreCase("allow")) {
                relationship = Restriction.Relationship.ALLOW;
            } else if (r.getAttribute("relationship").equalsIgnoreCase("deny")) {
                relationship = Restriction.Relationship.DENY;
            }

            final Restriction value = new Restriction(relationship, type, r.getTextContent().trim());
            values.add(value);
        }

        md.setRestrictions(values.toArray(new Restriction[values.size()]));
    }

    /**
     * @param e element to parse
     * @param md metadata to fill in
     */
    private void parseAdultMetadata(final Element e, final Metadata md) {
        final Element adult = super.getChild(e, "adult", getNS());

        if (adult != null && md.getRatings().length == 0) {
            final Rating[] r = new Rating[1];

            if (adult.getTextContent().trim().equals("true")) {
                r[0] = new Rating("urn:simple", "adult");
            } else {
                r[0] = new Rating("urn:simple", "nonadult");
            }

            md.setRatings(r);
        }
    }

    /**
     * @param e element to parse
     * @return PlayerReference element
     */
    private PlayerReference parsePlayer(final Element e) {
        final Element player = super.getChild(e, "player", getNS());
        PlayerReference p = null;

        if (player != null) {
            try {
                Integer width = null;
                if (player.getAttribute("width") != null) {
                    width = Integer.valueOf(player.getAttribute("width"));
                }
                Integer height = null;
                if (player.getAttribute("height") != null) {
                    height = Integer.valueOf(player.getAttribute("height"));
                }
                p = new PlayerReference(new URI(player.getAttribute("url")), width, height);
            } catch (final Exception ex) {
                LOG.warn("Exception parsing player tag.", ex);
            }
        }

        return p;
    }

    public Namespace getNS() {
        return NS;
    }

}
