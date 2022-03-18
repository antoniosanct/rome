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

import java.io.StringReader;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Generator;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.ModuleGenerator;
import com.rometools.utils.Lists;

/**
 * Feed Generator for Atom
 * 
 */

public class Atom03Generator extends BaseWireFeedGenerator {

    private static final String ATOM_03_URI = "http://purl.org/atom/ns#";
    private static final Namespace ATOM_NS = BaseWireFeedParser.createNamespace(ATOM_03_URI);

    private final String version;

    /**
     * Public constructor.
     */
    public Atom03Generator() {
        this("atom_0.3", "0.3");
    }

    protected Atom03Generator(final String type, final String version) {
        super(type);
        this.version = version;
    }

    protected String getVersion() {
        return version;
    }

    protected Namespace getFeedNamespace() {
        return ATOM_NS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document generate(final WireFeed wFeed) throws FeedException {
    	try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document doc = dbf.newDocumentBuilder().newDocument();
			final Feed feed = (Feed) wFeed;
	        final Element root = createRootElement(feed, doc);
	        populateFeed(feed, root, doc);
	        purgeUnusedNamespaceDeclarations(root);
	        doc.appendChild(root);
	        return doc;
		} catch (ParserConfigurationException e) {
			throw new FeedException("Failed creating document", e);
		}
    }

    protected Element createRootElement(final Feed feed, final Document doc) {
        final Element root = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "feed");
//        root.addNamespaceDeclaration(getFeedNamespace());
//        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + getFeedNamespace().getPrefix(), getFeedNamespace().getNamespaceURI());
        root.setAttribute("version", getVersion());
        generateModuleNamespaceDefs(root);
        return root;
    }

    protected void populateFeed(final Feed feed, final Element parent, final Document doc) throws FeedException {
        addFeed(feed, parent, doc);
        addEntries(feed, parent, doc);
    }

    protected void addFeed(final Feed feed, final Element parent, final Document doc) throws FeedException {
        final Element eFeed = parent;
        populateFeedHeader(feed, eFeed, doc);
        checkFeedHeaderConstraints(eFeed);
        generateFeedModules(feed.getModules(), eFeed);
        generateForeignMarkup(eFeed, feed.getForeignMarkup(), parent);
    }

    protected void addEntries(final Feed feed, final Element parent, final Document doc) throws FeedException {
        final List<Entry> entries = feed.getEntries();
        for (final Entry entry : entries) {
            addEntry(entry, parent, doc);
        }
        checkEntriesConstraints(parent);
    }

    protected void addEntry(final Entry entry, final Element parent, final Document doc) throws FeedException {
        final Element eEntry = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "entry");
        populateEntry(entry, eEntry, parent, doc);
        checkEntryConstraints(eEntry);
        generateItemModules(entry.getModules(), eEntry);
        parent.appendChild(eEntry);
    }

    protected void populateFeedHeader(final Feed feed, final Element eFeed, final Document doc) throws FeedException {

        final Content titleEx = feed.getTitleEx();
        if (titleEx != null) {
            final Element titleElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "title");
            fillContentElement(titleElement, titleEx, doc);
            eFeed.appendChild(titleElement);
        }

        List<Link> links = feed.getAlternateLinks();
        for (final Link link : links) {
            eFeed.appendChild(generateLinkElement(link, eFeed, doc));
        }

        links = feed.getOtherLinks();
        for (final Link link : links) {
            eFeed.appendChild(generateLinkElement(link, eFeed, doc));
        }

        final List<SyndPerson> authors = feed.getAuthors();
        if (Lists.isNotEmpty(authors)) {
            final Element authorElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "author");
            fillPersonElement(authorElement, authors.get(0), doc);
            eFeed.appendChild(authorElement);
        }

        final List<SyndPerson> contributors = feed.getContributors();
        for (final SyndPerson contributor : contributors) {
            final Element contributorElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "contributor");
            fillPersonElement(contributorElement, contributor, doc);
            eFeed.appendChild(contributorElement);
        }

        final Content tagline = feed.getTagline();
        if (tagline != null) {
            final Element taglineElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "tagline");
            fillContentElement(taglineElement, tagline, doc);
            eFeed.appendChild(taglineElement);
        }

        final String id = feed.getId();
        if (id != null) {
            eFeed.appendChild(generateSimpleElement("id", id, eFeed, doc));
        }

        final Generator generator = feed.getGenerator();
        if (generator != null) {
            eFeed.appendChild(generateGeneratorElement(generator, eFeed, doc));
        }

        final String copyright = feed.getCopyright();
        if (copyright != null) {
            eFeed.appendChild(generateSimpleElement("copyright", copyright, eFeed, doc));
        }

        final Content info = feed.getInfo();
        if (info != null) {
            final Element infoElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "info");
            fillContentElement(infoElement, info, doc);
            eFeed.appendChild(infoElement);
        }

        final Date modified = feed.getModified();
        if (modified != null) {
            final Element modifiedElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "modified");
            modifiedElement.setTextContent(DateParser.formatW3CDateTime(modified, Locale.US));
            eFeed.appendChild(modifiedElement);
        }

    }

    protected void populateEntry(final Entry entry, final Element eEntry, final Element eFeed, final Document doc) throws FeedException {

        final Content titleEx = entry.getTitleEx();
        if (titleEx != null) {
            final Element titleElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "title");
            fillContentElement(titleElement, titleEx, doc);
            eEntry.appendChild(titleElement);
        }

        final List<Link> alternateLinks = entry.getAlternateLinks();
        for (final Link link : alternateLinks) {
            eEntry.appendChild(generateLinkElement(link, eFeed, doc));
        }

        final List<Link> otherLinks = entry.getOtherLinks();
        for (final Link link : otherLinks) {
            eEntry.appendChild(generateLinkElement(link, eFeed, doc));
        }

        final List<SyndPerson> authors = entry.getAuthors();
        if (Lists.isNotEmpty(authors)) {
            final Element authorElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "author");
            fillPersonElement(authorElement, authors.get(0), doc);
            eEntry.appendChild(authorElement);
        }

        final List<SyndPerson> contributors = entry.getContributors();
        for (final SyndPerson contributor : contributors) {
            final Element contributorElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "contributor");
            fillPersonElement(contributorElement, contributor, doc);
            eEntry.appendChild(contributorElement);
        }

        final String id = entry.getId();
        if (id != null) {
            eEntry.appendChild(generateSimpleElement("id", id, eEntry, doc));
        }

        final Date modified = entry.getModified();
        if (modified != null) {
            final Element modifiedElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "modified");
            modifiedElement.setTextContent(DateParser.formatW3CDateTime(modified, Locale.US));
            eEntry.appendChild(modifiedElement);
        }

        final Date issued = entry.getIssued();
        if (issued != null) {
            final Element issuedElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "issued");
            issuedElement.setTextContent(DateParser.formatW3CDateTime(issued, Locale.US));
            eEntry.appendChild(issuedElement);
        }

        final Date created = entry.getCreated();
        if (created != null) {
            final Element createdElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "created");
            createdElement.setTextContent(DateParser.formatW3CDateTime(created, Locale.US));
            eEntry.appendChild(createdElement);
        }

        final Content summary = entry.getSummary();
        if (summary != null) {
            final Element summaryElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "summary");
            fillContentElement(summaryElement, summary, doc);
            eEntry.appendChild(summaryElement);
        }

        final List<Content> contents = entry.getContents();
        for (final Content content : contents) {
            final Element contentElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "content");
            fillContentElement(contentElement, content, doc);
            eEntry.appendChild(contentElement);
        }

        generateForeignMarkup(eEntry, entry.getForeignMarkup(), eFeed);

    }

    protected void checkFeedHeaderConstraints(final Element eFeed) throws FeedException {
    }

    protected void checkEntriesConstraints(final Element parent) throws FeedException {
    }

    protected void checkEntryConstraints(final Element eEntry) throws FeedException {
    }

    protected Element generateLinkElement(final Link link, final Element eFeed, final Document doc) {

        final Element linkElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "link");

        final String rel = link.getRel();
        if (rel != null) {
            linkElement.setAttribute("rel", rel);
        }

        final String type = link.getType();
        if (type != null) {
            linkElement.setAttribute("type", type);
        }

        final String href = link.getHref();
        if (href != null) {
            linkElement.setAttribute("href", href);
        }

        return linkElement;

    }

    protected void fillPersonElement(final Element element, final SyndPerson person, final Document doc) {

        final String name = person.getName();
        if (name != null) {
            element.appendChild(generateSimpleElement("name", name, element, doc));
        }

        final String uri = person.getUri();
        if (uri != null) {
            element.appendChild(generateSimpleElement("url", uri, element, doc));
        }

        final String email = person.getEmail();
        if (email != null) {
            element.appendChild(generateSimpleElement("email", email, element, doc));
        }

    }

    protected Element generateTagLineElement(final Content tagline, final Element eFeed, final Document doc) {

        final Element taglineElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "tagline");

        final String type = tagline.getType();
        if (type != null) {
            taglineElement.setAttribute("type", type);
        }

        final String value = tagline.getValue();
        if (value != null) {
            taglineElement.setTextContent(value);
        }

        return taglineElement;

    }

    protected void fillContentElement(final Element contentElement, final Content content, final Document doc) throws FeedException {

        final String type = content.getType();
        if (type != null) {
            contentElement.setAttribute("type", type);
        }

        final String mode = content.getMode();
        if (mode != null) {
            contentElement.setAttribute("mode", mode);
        }

        final String value = content.getValue();
        if (value != null) {

            if (mode == null || mode.equals(Content.ESCAPED)) {

                contentElement.setTextContent(value);

            } else if (mode.equals(Content.BASE64)) {

                contentElement.setTextContent(Base64.getEncoder().encodeToString(value.getBytes()));

            } else if (mode.equals(Content.XML)) {

                final StringBuffer tmpDocString = new StringBuffer("<tmpdoc>");
                tmpDocString.append(value);
                tmpDocString.append("</tmpdoc>");
                final StringReader tmpDocReader = new StringReader(tmpDocString.toString());
                Document tmpDoc;

                try {
                	DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
                	dbf.setNamespaceAware(true);
                	dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        			DocumentBuilder db = dbf.newDocumentBuilder();
                    tmpDoc = db.parse(new InputSource(tmpDocReader));
                } catch (final Exception ex) {
                    throw new FeedException("Invalid XML", ex);
                }
                Node newN = doc.importNode(tmpDoc.getDocumentElement(), true);
                contentElement.appendChild(newN.getFirstChild());
            }

        }
    }

    protected Element generateGeneratorElement(final Generator generator, final Element eFeed, final Document doc) {

        final Element generatorElement = doc.createElementNS(getFeedNamespace().getNamespaceURI(), "generator");

        final String url = generator.getUrl();
        if (url != null) {
            generatorElement.setAttribute("url", url);
        }

        final String version = generator.getVersion();
        if (version != null) {
            generatorElement.setAttribute("version", version);
        }

        final String value = generator.getValue();
        if (value != null) {
            generatorElement.setTextContent(value);
        }

        return generatorElement;

    }

    protected Element generateSimpleElement(final String name, final String value, final Element eFeed, final Document doc) {
        final Element element = doc.createElementNS(getFeedNamespace().getNamespaceURI(), name);
        element.setTextContent(value);
        return element;
    }

}
