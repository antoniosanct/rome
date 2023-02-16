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

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Generator;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.utils.Lists;

/**
 * Feed Generator for Atom
 * 
 */
public class Atom10Generator extends BaseWireFeedGenerator {

    private static final String ATOM_10_URI = "http://www.w3.org/2005/Atom";
    
    private static final Namespace ATOM_NS = BaseWireFeedParser.createNamespace(ATOM_10_URI);

    private final String version;

    /**
     * Public constructor.
     */
    public Atom10Generator() {
        this("atom_1.0", "1.0");
    }

    protected Atom10Generator(final String type, final String version) {
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

        final String xmlBase = feed.getXmlBase();
        if (xmlBase != null) {
            root.setAttributeNS(XMLConstants.XML_NS_URI, "base", xmlBase);
        }

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
        generateForeignMarkup(eFeed, feed.getForeignMarkup(), parent);
        checkFeedHeaderConstraints(eFeed);
        generateFeedModules(feed.getModules(), eFeed);
    }

    protected void addEntries(final Feed feed, final Element parent, final Document doc) throws FeedException {
        final List<Entry> items = feed.getEntries();
        for (final Entry entry : items) {
            addEntry(entry, parent, doc);
        }
        checkEntriesConstraints(parent);
    }

    protected void addEntry(final Entry entry, final Element parent, final Document doc) throws FeedException {

        final Element eEntry = parent.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "entry");

        final String xmlBase = entry.getXmlBase();
        if (xmlBase != null) {
            eEntry.setAttributeNS(XMLConstants.XML_NS_URI, "base", xmlBase);
        }

        populateEntry(entry, eEntry, doc);
        generateForeignMarkup(eEntry, entry.getForeignMarkup(), parent);
        checkEntryConstraints(eEntry);
        generateItemModules(entry.getModules(), eEntry);
        parent.appendChild(eEntry);

    }

    protected void populateFeedHeader(final Feed feed, final Element eFeed, final Document doc) throws FeedException {

        final Content titleEx = feed.getTitleEx();
        if (titleEx != null) {
            final Element titleElement = eFeed.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "title");
            fillContentElement(titleElement, titleEx, doc);
            eFeed.appendChild(titleElement);
        }

        final List<Link> alternateLinks = feed.getAlternateLinks();
        if (alternateLinks != null) {
            for (final Link link : alternateLinks) {
                eFeed.appendChild(generateLinkElement(link, eFeed));
            }
        }

        final List<Link> otherLinks = feed.getOtherLinks();
        if (otherLinks != null) {
            for (final Link link : otherLinks) {
                eFeed.appendChild(generateLinkElement(link, eFeed));
            }
        }

        final List<Category> cats = feed.getCategories();
        if (cats != null) {
            for (final Category category : cats) {
                eFeed.appendChild(generateCategoryElement(category, eFeed));
            }
        }

        final List<SyndPerson> authors = feed.getAuthors();
        if (Lists.isNotEmpty(authors)) {
            for (final SyndPerson author : authors) {
                final Element authorElement = eFeed.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "author");
                fillPersonElement(authorElement, author);
                eFeed.appendChild(authorElement);
            }
        }

        final List<SyndPerson> contributors = feed.getContributors();
        if (Lists.isNotEmpty(contributors)) {
            for (final SyndPerson contributor : contributors) {
                final Element contributorElement = eFeed.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "contributor");
                fillPersonElement(contributorElement, contributor);
                eFeed.appendChild(contributorElement);
            }
        }

        final Content subtitle = feed.getSubtitle();
        if (subtitle != null) {
            final Element subtitleElement = eFeed.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "subtitle");
            fillContentElement(subtitleElement, subtitle, doc);
            eFeed.appendChild(subtitleElement);
        }

        final String id = feed.getId();
        if (id != null) {
            eFeed.appendChild(generateSimpleElement("id", id, eFeed));
        }

        final Generator generator = feed.getGenerator();
        if (generator != null) {
            eFeed.appendChild(generateGeneratorElement(generator, eFeed));
        }

        final String rights = feed.getRights();
        if (rights != null) {
            eFeed.appendChild(generateSimpleElement("rights", rights, eFeed));
        }

        final String icon = feed.getIcon();
        if (icon != null) {
            eFeed.appendChild(generateSimpleElement("icon", icon, eFeed));
        }

        final String logo = feed.getLogo();
        if (logo != null) {
            eFeed.appendChild(generateSimpleElement("logo", logo, eFeed));
        }

        final Date updated = feed.getUpdated();
        if (updated != null) {
            final Element updatedElement = eFeed.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "updated");
            updatedElement.setTextContent(DateParser.formatW3CDateTime(updated, Locale.US));
            eFeed.appendChild(updatedElement);
        }

    }

    protected void populateEntry(final Entry entry, final Element eEntry, final Document doc) throws FeedException {

        final Content titleEx = entry.getTitleEx();
        if (titleEx != null) {
            final Element titleElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "title");
            fillContentElement(titleElement, titleEx, doc);
            eEntry.appendChild(titleElement);
        }

        final List<Link> alternateLinks = entry.getAlternateLinks();
        if (alternateLinks != null) {
            for (final Link link : alternateLinks) {
                eEntry.appendChild(generateLinkElement(link, eEntry));
            }
        }

        final List<Link> otherLinks = entry.getOtherLinks();
        if (otherLinks != null) {
            for (final Link link : otherLinks) {
                eEntry.appendChild(generateLinkElement(link, eEntry));
            }
        }

        final List<Category> cats = entry.getCategories();
        if (cats != null) {
            for (final Category category : cats) {
                eEntry.appendChild(generateCategoryElement(category, eEntry));
            }
        }

        final List<SyndPerson> authors = entry.getAuthors();
        if (Lists.isNotEmpty(authors)) {
            for (final SyndPerson author : authors) {
                final Element authorElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "author");
                fillPersonElement(authorElement, author);
                eEntry.appendChild(authorElement);
            }
        }

        final List<SyndPerson> contributors = entry.getContributors();
        if (Lists.isNotEmpty(contributors)) {
            for (final SyndPerson contributor : contributors) {
                final Element contributorElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "contributor");
                fillPersonElement(contributorElement, contributor);
                eEntry.appendChild(contributorElement);
            }
        }

        final String id = entry.getId();
        if (id != null) {
            eEntry.appendChild(generateSimpleElement("id", id, eEntry));
        }

        final Date updated = entry.getUpdated();
        if (updated != null) {
            final Element updatedElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "updated");
            updatedElement.setTextContent(DateParser.formatW3CDateTime(updated, Locale.US));
            eEntry.appendChild(updatedElement);
        }

        final Date published = entry.getPublished();
        if (published != null) {
            final Element publishedElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "published");
            publishedElement.setTextContent(DateParser.formatW3CDateTime(published, Locale.US));
            eEntry.appendChild(publishedElement);
        }

        final List<Content> contents = entry.getContents();
        if (Lists.isNotEmpty(contents)) {
            final Element contentElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "content");
            final Content content = contents.get(0);
            fillContentElement(contentElement, content, doc);
            eEntry.appendChild(contentElement);
        }

        final Content summary = entry.getSummary();
        if (summary != null) {
            final Element summaryElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "summary");
            fillContentElement(summaryElement, summary, doc);
            eEntry.appendChild(summaryElement);
        }

        final Feed source = entry.getSource();
        if (source != null) {
            final Element sourceElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "source");
            populateFeedHeader(source, sourceElement, doc);
            eEntry.appendChild(sourceElement);
        }

        final String rights = entry.getRights();
        if (rights != null) {
            eEntry.appendChild(generateSimpleElement("rights", rights, eEntry));
        }

    }

    protected void checkFeedHeaderConstraints(final Element eFeed) throws FeedException {
    }

    protected void checkEntriesConstraints(final Element parent) throws FeedException {
    }

    protected void checkEntryConstraints(final Element eEntry) throws FeedException {
    }

    protected Element generateCategoryElement(final Category cat, final Element eEntry) {

        final Namespace namespace = getFeedNamespace();
        final Element catElement = eEntry.getOwnerDocument().createElementNS(namespace.getNamespaceURI(), "category");

        final String term = cat.getTerm();
        if (term != null) {
            catElement.setAttribute("term", term);
        }

        final String label = cat.getLabel();
        if (label != null) {
            catElement.setAttribute("label", label);
        }

        final String scheme = cat.getScheme();
        if (scheme != null) {
            catElement.setAttribute("scheme", scheme);
        }

        return catElement;

    }

    protected Element generateLinkElement(final Link link, final Element eEntry) {

        final Namespace namespace = getFeedNamespace();
        final Element linkElement = eEntry.getOwnerDocument().createElementNS(namespace.getNamespaceURI(), "link");

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

        final String hreflang = link.getHreflang();
        if (hreflang != null) {
            linkElement.setAttribute("hreflang", hreflang);
        }

        final String linkTitle = link.getTitle();
        if (linkTitle != null) {
            linkElement.setAttribute("title", linkTitle);
        }

        if (link.getLength() != 0) {
            linkElement.setAttribute("length", Long.toString(link.getLength()));
        }

        return linkElement;

    }

    protected void fillPersonElement(final Element element, final SyndPerson person) {

        final String name = person.getName();
        if (name != null) {
            element.appendChild(generateSimpleElement("name", name, element));
        }

        final String uri = person.getUri();
        if (uri != null) {
            element.appendChild(generateSimpleElement("uri", uri, element));
        }

        final String email = person.getEmail();
        if (email != null) {
            element.appendChild(generateSimpleElement("email", email, element));
        }

        generatePersonModules(person.getModules(), element);

    }

    protected Element generateTagLineElement(final Content tagline, final Element eEntry) {

        final Element taglineElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "subtitle");

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

        String atomType = type;

        if (type != null) {
            // Fix for issue #39 "Atom 1.0 Text Types Not Set Correctly"
            // we're not sure who set this value, so ensure Atom types are used
            if ("text/plain".equals(type)) {
                atomType = Content.TEXT;
            } else if ("text/html".equals(type)) {
                atomType = Content.HTML;
            } else if ("application/xhtml+xml".equals(type)) {
                atomType = Content.XHTML;
            }

            contentElement.setAttribute("type", atomType);
        }

        final String href = content.getSrc();
        if (href != null) {
            contentElement.setAttribute("src", href);
        }

        final String value = content.getValue();
        if (value != null) {

            if (atomType != null && (atomType.equals(Content.XHTML) || atomType.indexOf("/xml") != -1 || atomType.indexOf("+xml") != -1)) {

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
            } else {

                // must be type html, text or some other non-XML format
                // JDOM will escape property for XML
                contentElement.setTextContent(value);

            }

        }
    }

    protected Element generateGeneratorElement(final Generator generator, final Element eEntry) {

        final Element generatorElement = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "generator");

        final String url = generator.getUrl();
        if (url != null) {
            generatorElement.setAttribute("uri", url);
        }

        final String version2 = generator.getVersion();
        if (version2 != null) {
            generatorElement.setAttribute("version", version2);
        }

        final String value = generator.getValue();
        if (value != null) {
            generatorElement.setTextContent(value);
        }

        return generatorElement;

    }

    protected Element generateSimpleElement(final String name, final String value, final Element eEntry) {
        final Element element = eEntry.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), name);
        element.setTextContent(value);
        return element;
    }

    /**
     * Utility method to serialize an entry to writer.
     * @param entry the entry to serialize
     * @param writer the writer to commit entry
     * @throws IllegalArgumentException any illegal argument exception
     * @throws FeedException any feed exception
     * @throws IOException any I/O exception
     */
    public static void serializeEntry(final Entry entry, final Writer writer) throws IllegalArgumentException, FeedException, IOException {

        // Build a feed containing only the entry
        final List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        final Feed feed1 = new Feed();
        feed1.setFeedType("atom_1.0");
        feed1.setEntries(entries);

        // Get Rome to output feed as a JDOM document
        final WireFeedOutput wireFeedOutput = new WireFeedOutput();
        final Document feedDoc = wireFeedOutput.outputDom(feed1);

        // Grab entry element from feed and get JDOM to serialize it
        final Element entryElement = (Element) feedDoc.getDocumentElement().getChildNodes().item(0);

		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        StreamResult result = new StreamResult(writer);
	        DOMSource source = new DOMSource(entryElement);
	        t.transform(source, result);
		} catch (TransformerException | TransformerFactoryConfigurationError e) {
			throw new FeedException("Error outputting feed", e);
		}
    }

}
