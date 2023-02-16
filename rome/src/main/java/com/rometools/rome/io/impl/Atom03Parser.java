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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.stream.events.Namespace;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.WireFeedForeignMarkup;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Generator;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.FeedException;
import com.rometools.utils.Lists;

/**
 * Atom 0.3 Parser
 */
public class Atom03Parser extends BaseWireFeedParser {

    private static final String ATOM_03_URI = "http://purl.org/atom/ns#";
    private static final Namespace ATOM_03_NS = BaseWireFeedParser.createNamespace(ATOM_03_URI);

    /**
     * Public constructor.
     */
    public Atom03Parser() {
        this("atom_0.3", ATOM_03_NS);
    }

    protected Atom03Parser(final String type, final Namespace ns) {
        super(type, ns);
    }

    protected Namespace getAtomNamespace() {
        return ATOM_03_NS;
    }

    @Override
    public boolean isMyType(final Document document) {
    	final Element rssRoot = document.getDocumentElement();
        final Namespace defaultNS = BaseWireFeedParser.createNamespace(rssRoot.getNamespaceURI());
        return defaultNS != null && defaultNS.getNamespaceURI() != null && getAtomNamespace().getNamespaceURI().equals(defaultNS.getNamespaceURI());
    }

    @Override
    public WireFeed parse(final Document document, final boolean validate, final Locale locale) throws IllegalArgumentException, FeedException {

        if (validate) {
            validateFeed(document);
        }

        final Element rssRoot = document.getDocumentElement();

        return parseFeed(rssRoot, locale);

    }

    protected void validateFeed(final Document document) throws FeedException {
        // TODO here we have to validate the Feed against a schema or whatever not sure how to do it
        // one posibility would be to produce an ouput and attempt to parse it again with validation
        // turned on. otherwise will have to check the document elements by hand.
    }

    protected WireFeed parseFeed(final Element eFeed, final Locale locale) throws FeedException {

        final String type = getType();
        final Document document = eFeed.getOwnerDocument();
        final String styleSheet = getStyleSheet(document);

        final Feed feed = new Feed(type);
        feed.setStyleSheet(styleSheet);

        final Element title = super.getChild(eFeed, "title", getAtomNamespace());
        if (title != null) {
            feed.setTitleEx(parseContent(title));
        }

        final List<Element> links = super.getChildren(eFeed, "link", getAtomNamespace());
        feed.setAlternateLinks(parseAlternateLinks(links));
        feed.setOtherLinks(parseOtherLinks(links));

        final Element author = super.getChild(eFeed, "author", getAtomNamespace());
        if (author != null) {
            final List<SyndPerson> authors = new ArrayList<SyndPerson>();
            authors.add(parsePerson(author));
            feed.setAuthors(authors);
        }

        final List<Element> contributors = super.getChildren(eFeed, "contributor", getAtomNamespace());
        if (null != contributors) {
            feed.setContributors(parsePersons(contributors));
        }

        final Element tagline = super.getChild(eFeed, "tagline", getAtomNamespace());
        if (tagline != null) {
            feed.setTagline(parseContent(tagline));
        }

        final Element id = super.getChild(eFeed, "id", getAtomNamespace());
        if (id != null) {
            feed.setId(id.getTextContent());
        }

        final Element generator = super.getChild(eFeed, "generator", getAtomNamespace());
        if (generator != null) {
            final Generator gen = new Generator();
            gen.setValue(generator.getTextContent());
            String att = getAttributeValue(generator, "url");
            if (att != null) {
                gen.setUrl(att);
            }
            att = getAttributeValue(generator, "version");
            if (att != null) {
                gen.setVersion(att);
            }
            feed.setGenerator(gen);
        }

        final Element copyright = super.getChild(eFeed, "copyright", getAtomNamespace());
        if (copyright != null) {
            feed.setCopyright(copyright.getTextContent());
        }

        final Element info = super.getChild(eFeed, "info", getAtomNamespace());
        if (info != null) {
            feed.setInfo(parseContent(info));
        }

        final Element modified = super.getChild(eFeed, "modified", getAtomNamespace());
        if (modified != null) {
            feed.setModified(DateParser.parseDate(modified.getTextContent(), locale));
        }

        feed.setModules(parseFeedModules(eFeed, locale));

        final List<Element> entries = super.getChildren(eFeed, "entry", getAtomNamespace());
        if (null != entries) {
            feed.setEntries(parseEntries(entries, locale));
        }

        final List<WireFeedForeignMarkup> foreignMarkup = extractForeignMarkup(eFeed, feed, getAtomNamespace());
        if (!foreignMarkup.isEmpty()) {
            feed.setForeignMarkup(foreignMarkup);
        }

        return feed;

    }

    private Link parseLink(final Element eLink) {

        final Link link = new Link();

        final String rel = getAttributeValue(eLink, "rel");
        if (rel != null) {
            link.setRel(rel);
        }

        final String type = getAttributeValue(eLink, "type");
        if (type != null) {
            link.setType(type);
        }

        final String href = getAttributeValue(eLink, "href");
        if (href != null) {
            link.setHref(href);
        }

        return link;

    }

    private List<Link> parseLinks(final List<Element> eLinks, final boolean alternate) {

        final List<Link> links = new ArrayList<Link>();

        if (null != eLinks) {
		    for (Element eLink : eLinks) {
		        final String rel = getAttributeValue(eLink, "rel");
		        if (alternate) {
		            if ("alternate".equals(rel)) {
		                links.add(parseLink(eLink));
		            }
		        } else {
		            if (!"alternate".equals(rel)) {
		                links.add(parseLink(eLink));
		            }
		        }
		    }
        }
        return Lists.emptyToNull(links);

    }

    // List<Element> -> List(Link)
    private List<Link> parseAlternateLinks(final List<Element> eLinks) {
        return parseLinks(eLinks, true);
    }

    // List<Element> -> List(Link)
    private List<Link> parseOtherLinks(final List<Element> eLinks) {
        return parseLinks(eLinks, false);
    }

    private Person parsePerson(final Element ePerson) {

        final Person person = new Person();

        final Element name = super.getChild(ePerson, "name", getAtomNamespace());

        if (name != null) {
            person.setName(name.getTextContent());
        }

        final Element url = super.getChild(ePerson, "url", getAtomNamespace());
        if (url != null) {
            person.setUrl(url.getTextContent());
        }

        final Element email = super.getChild(ePerson, "email", getAtomNamespace());
        if (email != null) {
            person.setEmail(email.getTextContent());
        }

        return person;

    }

    // List<Element> -> List(Persons)
    private List<SyndPerson> parsePersons(final List<Element> ePersons) {

        final List<SyndPerson> persons = new ArrayList<SyndPerson>();
        if (null != ePersons) {
		    for (Element p : ePersons) {
		        persons.add(parsePerson(p));
		    }
        }
        return Lists.emptyToNull(persons);

    }

    private Content parseContent(final Element e) throws FeedException {

        String value = null;

        String type = getAttributeValue(e, "type");
        if (type == null) {
            type = "text/plain";
        }

        String mode = getAttributeValue(e, "mode");
        if (mode == null) {
            mode = Content.XML; // default to xml content
        }

        if (mode.equals(Content.ESCAPED)) {

            // do nothing XML Parser took care of this
            value = e.getTextContent();

        } else if (mode.equals(Content.BASE64)) {

            value = new String(Base64.getDecoder().decode(e.getTextContent().getBytes()));

        } else if (mode.equals(Content.XML)) {
        	try {
        		TransformerFactory tf = TransformerFactory.newInstance();
    			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    			Transformer t = tf.newTransformer();
    			StringWriter buffer = new StringWriter();
    			t.setOutputProperty(OutputKeys.METHOD, "xml");
    	    	t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    	    	t.transform(new DOMSource(e.getFirstChild()),
    	    	      new StreamResult(buffer));
    	    	value = buffer.toString();
    		} catch (TransformerConfigurationException tce) {
    			throw new FeedException("Failed parse Element", tce);
    		} catch (TransformerException te) {
    			throw new FeedException("Failed parse Element", te);
    		}
        }

        final Content content = new Content();
        content.setType(type);
        content.setMode(mode);
        content.setValue(value);
        return content;
    }

    // List(Elements) -> List(Entries)
    private List<Entry> parseEntries(final List<Element> eEntries, final Locale locale) throws FeedException {

        final List<Entry> entries = new ArrayList<Entry>();
        if (null != eEntries) {
		    for (Element e : eEntries) {
		        entries.add(parseEntry(e, locale));
		    }
        }
        return Lists.emptyToNull(entries);

    }

    private Entry parseEntry(final Element eEntry, final Locale locale) throws FeedException {

        final Entry entry = new Entry();

        final Element title = super.getChild(eEntry, "title", getAtomNamespace());
        if (title != null) {
            entry.setTitleEx(parseContent(title));
        }

        final List<Element> links = super.getChildren(eEntry, "link", getAtomNamespace());
        entry.setAlternateLinks(parseAlternateLinks(links));
        entry.setOtherLinks(parseOtherLinks(links));

        final Element author = super.getChild(eEntry, "author", getAtomNamespace());
        if (author != null) {
            final List<SyndPerson> authors = new ArrayList<SyndPerson>();
            authors.add(parsePerson(author));
            entry.setAuthors(authors);
        }

        final List<Element> contributors = super.getChildren(eEntry, "contributor", getAtomNamespace());
        if (null != contributors) {
            entry.setContributors(parsePersons(contributors));
        }

        final Element id = super.getChild(eEntry, "id", getAtomNamespace());
        if (id != null) {
            entry.setId(id.getTextContent());
        }

        final Element modified = super.getChild(eEntry, "modified", getAtomNamespace());
        if (modified != null) {
            entry.setModified(DateParser.parseDate(modified.getTextContent(), locale));
        }

        final Element issued = super.getChild(eEntry, "issued", getAtomNamespace());
        if (issued != null) {
            entry.setIssued(DateParser.parseDate(issued.getTextContent(), locale));
        }

        final Element created = super.getChild(eEntry, "created", getAtomNamespace());
        if (created != null) {
            entry.setCreated(DateParser.parseDate(created.getTextContent(), locale));
        }

        final Element summary = super.getChild(eEntry, "summary", getAtomNamespace());
        if (summary != null) {
            entry.setSummary(parseContent(summary));
        }

        final List<Element> contents = super.getChildren(eEntry, "content", getAtomNamespace());
        if (null != contents) {
            final List<Content> content = new ArrayList<Content>();
            for (Element c : contents) {
                content.add(parseContent(c));
            }
            entry.setContents(content);
        }

        entry.setModules(parseItemModules(eEntry, locale));

        final List<WireFeedForeignMarkup> foreignMarkup = extractForeignMarkup(eEntry, entry, getAtomNamespace());
        if (!foreignMarkup.isEmpty()) {
            entry.setForeignMarkup(foreignMarkup);
        }

        return entry;

    }

}
