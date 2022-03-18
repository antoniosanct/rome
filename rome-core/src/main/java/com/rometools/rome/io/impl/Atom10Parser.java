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
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.WireFeedForeignMarkup;
import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Generator;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedInput;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.utils.Lists;

/**
 * Parser for Atom 1.0
 */
public class Atom10Parser extends BaseWireFeedParser {

    private static final String ATOM_10_URI = "http://www.w3.org/2005/Atom";
    private static final Namespace ATOM_10_NS = BaseWireFeedParser.createNamespace(ATOM_10_URI);

    private static boolean resolveURIs = false;

    /**
     * 
     * @param resolveURIs flag to resolve URIs
     */
    public static void setResolveURIs(final boolean resolveURIs) {
        Atom10Parser.resolveURIs = resolveURIs;
    }

    /**
     * 
     * @return the resolveURIs value
     */
    public static boolean getResolveURIs() {
        return resolveURIs;
    }

    /**
     * Public constructor.
     */
    public Atom10Parser() {
        this("atom_1.0");
    }

    protected Atom10Parser(final String type) {
        super(type, ATOM_10_NS);
    }

    protected Namespace getAtomNamespace() {
        return ATOM_10_NS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMyType(final Document document) {
        final Element rssRoot = document.getDocumentElement();
        final Namespace defaultNS = BaseWireFeedParser.createNamespace(rssRoot.getNamespaceURI());
        return defaultNS != null && defaultNS.getNamespaceURI() != null && getAtomNamespace().getNamespaceURI().equals(defaultNS.getNamespaceURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WireFeed parse(final Document document, final boolean validate, final Locale locale) throws IllegalArgumentException, FeedException {
        if (validate) {
            validateFeed(document);
        }
        final Element rssRoot = document.getDocumentElement();
        return parseFeed(rssRoot, locale);
    }

    protected void validateFeed(final Document document) throws FeedException {
        // TBD here we have to validate the Feed against a schema or whatever not sure how to do it
        // one posibility would be to produce an ouput and attempt to parse it again with validation
        // turned on. otherwise will have to check the document elements by hand.
    }

    protected WireFeed parseFeed(final Element eFeed, final Locale locale) throws FeedException {

        String baseURI = null;
        try {
            baseURI = findBaseURI(eFeed);
        } catch (final Exception e) {
            throw new FeedException("ERROR while finding base URI of feed", e);
        }

        final Feed feed = parseFeedMetadata(baseURI, eFeed, locale);
        feed.setStyleSheet(getStyleSheet(eFeed.getOwnerDocument()));

        final String xmlBase = eFeed.getAttributeNS(XMLConstants.XML_NS_URI, "base");
        if (xmlBase != null) {
            feed.setXmlBase(xmlBase);
        }

        feed.setModules(parseFeedModules(eFeed, locale));

        final List<Element> eList = super.getChildren(eFeed, "entry");
        if (null != eList) {
            feed.setEntries(parseEntries(feed, baseURI, eList, locale));
        }

        final List<WireFeedForeignMarkup> foreignMarkup = extractForeignMarkup(eFeed, feed, getAtomNamespace());
        if (!foreignMarkup.isEmpty()) {
            feed.setForeignMarkup(foreignMarkup);
        }
        return feed;
    }

    private Feed parseFeedMetadata(final String baseURI, final Element eFeed, final Locale locale) throws FeedException {

        final Feed feed = new Feed(getType());

        final Element title = super.getChild(eFeed, "title");
        if (title != null) {
            final Content c = new Content();
            c.setValue(parseTextConstructToString(title));
            c.setType(title.getAttribute("type"));
            feed.setTitleEx(c);
        }

        final List<Element> links = super.getChildren(eFeed, "link");
        if (null != links) {
        	feed.setAlternateLinks(parseAlternateLinks(feed, null, baseURI, links));
        	feed.setOtherLinks(parseOtherLinks(feed, null, baseURI, links));
        }

        final List<Element> categories = super.getChildren(eFeed, "category");
        if (null != categories) {
        	feed.setCategories(parseCategories(baseURI, categories));
        }

        final List<Element> authors = super.getChildren(eFeed, "author");
        if (null != authors) {
            feed.setAuthors(parsePersons(baseURI, authors, locale));
        }

        final List<Element> contributors = super.getChildren(eFeed, "contributor");
        if (null != contributors) {
            feed.setContributors(parsePersons(baseURI, contributors, locale));
        }

        final Element subtitle = super.getChild(eFeed, "subtitle");
        if (subtitle != null) {
            final Content content = new Content();
            content.setValue(parseTextConstructToString(subtitle));
            content.setType(subtitle.getAttribute("type"));
            feed.setSubtitle(content);
        }

        final Element id = super.getChild(eFeed, "id");
        if (id != null) {
            feed.setId(id.getTextContent());
        }

        final Element generator = super.getChild(eFeed, "generator");
        if (generator != null) {
            final Generator gen = new Generator();
            gen.setValue(generator.getTextContent());

            final String uri = generator.getAttribute("uri");
            if (uri != null) {
                gen.setUrl(uri);
            }

            final String version = generator.getAttribute("version");
            if (version != null) {
                gen.setVersion(version);
            }

            feed.setGenerator(gen);

        }

        final Element rights = super.getChild(eFeed, "rights");
        if (rights != null) {
            feed.setRights(parseTextConstructToString(rights));
        }

        final Element icon = super.getChild(eFeed, "icon");
        if (icon != null) {
            feed.setIcon(icon.getTextContent());
        }

        final Element logo = super.getChild(eFeed, "logo");
        if (logo != null) {
            feed.setLogo(logo.getTextContent());
        }

        final Element updated = super.getChild(eFeed, "updated");
        if (updated != null) {
            feed.setUpdated(DateParser.parseDate(updated.getTextContent(), locale));
        }

        return feed;

    }

    private Link parseLink(final Feed feed, final Entry entry, final String baseURI, final Element eLink) {

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
            if (isRelativeURI(href)) {
                link.setHrefResolved(resolveURI(baseURI, eLink, href));
            }
        }

        final String title = getAttributeValue(eLink, "title");
        if (title != null) {
            link.setTitle(title);
        }

        final String hrefLang = getAttributeValue(eLink, "hreflang");
        if (hrefLang != null) {
            link.setHreflang(hrefLang);
        }

        final String length = getAttributeValue(eLink, "length");
        if (length != null) {
            final Long val = NumberParser.parseLong(length);
            if (val != null) {
                link.setLength(val.longValue());
            }
        }

        return link;

    }

    // List(Elements) -> List(Link)
    private List<Link> parseAlternateLinks(final Feed feed, final Entry entry, final String baseURI, final List<Element> eLinks) {

        final List<Link> links = new ArrayList<Link>();
        for (Element l : eLinks) {
            final Link link = parseLink(feed, entry, baseURI, l);
            if (link.getRel() == null || "".equals(link.getRel().trim()) || "alternate".equals(link.getRel())) {
                links.add(link);
            }
        }

        return Lists.emptyToNull(links);

    }

    private List<Link> parseOtherLinks(final Feed feed, final Entry entry, final String baseURI, final List<Element> eLinks) {

        final List<Link> links = new ArrayList<Link>();
        for (Element l : eLinks) {
            final Link link = parseLink(feed, entry, baseURI, l);
            if (!"alternate".equals(link.getRel())) {
                links.add(link);
            }
        }

        return Lists.emptyToNull(links);

    }

    private Person parsePerson(final String baseURI, final Element ePerson, final Locale locale) {

        final Person person = new Person();

        final Element name = super.getChild(ePerson, "name");
        if (name != null) {
            person.setName(name.getTextContent());
        }

        final Element uri = super.getChild(ePerson, "uri");
        if (uri != null) {
            person.setUri(uri.getTextContent());
            if (isRelativeURI(uri.getTextContent())) {
                person.setUriResolved(resolveURI(baseURI, ePerson, uri.getTextContent()));
            }
        }

        final Element email = super.getChild(ePerson, "email");
        if (email != null) {
            person.setEmail(email.getTextContent());
        }

        person.setModules(parsePersonModules(ePerson, locale));

        return person;
    }

    // List(Elements) -> List(Persons)
    private List<SyndPerson> parsePersons(final String baseURI, final List<Element> ePersons, final Locale locale) {

        final List<SyndPerson> persons = new ArrayList<SyndPerson>();
        for (Element p : ePersons) {
            persons.add(parsePerson(baseURI, p, locale));
        }

        return Lists.emptyToNull(persons);

    }

    private Content parseContent(final Element e) throws FeedException {

        final String value = parseTextConstructToString(e);
        final String src = getAttributeValue(e, "src");
        final String type = getAttributeValue(e, "type");

        final Content content = new Content();
        content.setSrc(src);
        content.setType(type);
        content.setValue(value);
        return content;

    }

    private String parseTextConstructToString(final Element e) throws FeedException {
    	String type = e.getAttribute("type");
    	if (null == type) {
    		type = Content.TEXT;
    	}
    	
    	StringBuffer value = new StringBuffer("");
    	if (Content.XHTML.equals(type) || type.indexOf("/xml") != -1 || type.indexOf("+xml") != -1) {
			try {
				TransformerFactory tf = TransformerFactory.newInstance();
				tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
				Transformer t = tf.newTransformer();
				t.setOutputProperty(OutputKeys.METHOD, "xml");
		    	t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    	for (int i = 0; i < e.getChildNodes().getLength(); i++) {
		    		StringWriter buffer = new StringWriter();
		    		t.transform(new DOMSource(e.getChildNodes().item(i)),
		    			new StreamResult(buffer));
		    		value.append(buffer.toString());
		    	}
			} catch (TransformerConfigurationException tce) {
				throw new FeedException("Failed parse Element", tce);
			} catch (TransformerException te) {
				throw new FeedException("Failed parse Element", te);
			}
    	} else {
    		value.append(e.getTextContent());
    	}
    	
    	return value.toString();
    }

    // List(Elements) -> List(Entries)
    protected List<Entry> parseEntries(final Feed feed, final String baseURI, final List<Element> eEntries, final Locale locale) throws FeedException {

        final List<Entry> entries = new ArrayList<Entry>();
        for (Element e : eEntries) {
            entries.add(this.parseEntry(feed, e, baseURI, locale));
        }

        return Lists.emptyToNull(entries);

    }

    protected Entry parseEntry(final Feed feed, final Element eEntry, final String baseURI, final Locale locale) throws FeedException {

        final Entry entry = new Entry();

        final String xmlBase = eEntry.getAttributeNS(XMLConstants.XML_NS_URI, "base");
        if (xmlBase != null) {
            entry.setXmlBase(xmlBase);
        }

        final Element title = super.getChild(eEntry, "title");
        if (title != null) {
            final Content c = new Content();
            c.setValue(parseTextConstructToString(title));
            c.setType(title.getAttribute("type"));
            entry.setTitleEx(c);
        }

        final List<Element> links = super.getChildren(eEntry, "link");
        if (links != null) {
        	entry.setAlternateLinks(parseAlternateLinks(feed, entry, baseURI, links));
        	entry.setOtherLinks(parseOtherLinks(feed, entry, baseURI, links));
        }

        final List<Element> authors = super.getChildren(eEntry, "author");
        if (authors != null) {
            entry.setAuthors(parsePersons(baseURI, authors, locale));
        }

        final List<Element> contributors = super.getChildren(eEntry, "contributor");
        if (contributors != null) {
            entry.setContributors(parsePersons(baseURI, contributors, locale));
        }

        final Element id = super.getChild(eEntry, "id");
        if (id != null) {
            entry.setId(id.getTextContent());
        }

        final Element updated = super.getChild(eEntry, "updated");
        if (updated != null) {
            entry.setUpdated(DateParser.parseDate(updated.getTextContent(), locale));
        }

        final Element published = super.getChild(eEntry, "published");
        if (published != null) {
            entry.setPublished(DateParser.parseDate(published.getTextContent(), locale));
        }

        final Element summary = super.getChild(eEntry, "summary");
        if (summary != null) {
            entry.setSummary(parseContent(summary));
        }

        final Element content = super.getChild(eEntry, "content");
        if (content != null) {
            final List<Content> contents = new ArrayList<Content>();
            contents.add(parseContent(content));
            entry.setContents(contents);
        }

        final Element rights = super.getChild(eEntry, "rights");
        if (rights != null) {
            entry.setRights(rights.getTextContent());
        }

        final List<Element> categories = super.getChildren(eEntry, "category");
        if (categories != null) {
        	entry.setCategories(parseCategories(baseURI, categories));
        }

        final Element source = super.getChild(eEntry, "source");
        if (source != null) {
            entry.setSource(parseFeedMetadata(baseURI, source, locale));
        }

        entry.setModules(parseItemModules(eEntry, locale));

        final List<WireFeedForeignMarkup> foreignMarkup = extractForeignMarkup(eEntry, entry, getAtomNamespace());
        if (!foreignMarkup.isEmpty()) {
            entry.setForeignMarkup(foreignMarkup);
        }

        return entry;
    }

    private List<Category> parseCategories(final String baseURI, final List<Element> eCategories) {

        final List<Category> cats = new ArrayList<Category>();
        for (Element c : eCategories) {
            cats.add(parseCategory(baseURI, c));
        }

        return Lists.emptyToNull(cats);

    }

    private Category parseCategory(final String baseURI, final Element eCategory) {

        final Category category = new Category();

        final String term = getAttributeValue(eCategory, "term");
        if (term != null) {
            category.setTerm(term);
        }

        final String scheme = getAttributeValue(eCategory, "scheme");
        if (scheme != null) {
            category.setScheme(scheme);
            if (isRelativeURI(scheme)) {
                category.setSchemeResolved(resolveURI(baseURI, eCategory, scheme));
            }
        }

        final String label = getAttributeValue(eCategory, "label");
        if (label != null) {
            category.setLabel(label);
        }

        return category;

    }

    // Once following relative URI methods are made public in the ROME
    // Atom10Parser, then use them instead and delete these.

    // Fix for issue #34 "valid IRI href attributes are stripped for atom:link"
    // URI's that didn't start with http were being treated as relative URIs.
    // So now consider an absolute URI to be any alpha-numeric string followed
    // by a colon, followed by anything -- specified by this regex:
    static Pattern absoluteURIPattern = Pattern.compile("^[a-z0-9]*:.*$");

    /**
     * 
     * @param uri the uri parameter to confirm as absolute.
     * @return the matcher result
     */
    public static boolean isAbsoluteURI(final String uri) {
        return absoluteURIPattern.matcher(uri).find();
    }

    /**
     * Returns true if URI is relative. 
     * @param uri the URI to evaluate
     * @return The evaluation result.
     */
    public static boolean isRelativeURI(final String uri) {
        return !isAbsoluteURI(uri);
    }

    /**
     * Resolve URI via base URL and parent element. Resolve URI based considering xml:base and
     * baseURI.
     *
     * @param baseURI Base URI used to fetch the XML document
     * @param parent Parent element from which to consider xml:base
     * @param url URL to be resolved
     * @return The resolve URI
     */
    public static String resolveURI(final String baseURI, final Node parent, String url) {

        if (!resolveURIs) {
            return url;
        }

        if (isRelativeURI(url)) {

            if (".".equals(url) || "./".equals(url)) {
                url = "";
            }

            if (url.startsWith("/") && baseURI != null) {
                String base = null;
                final int slashslash = baseURI.indexOf("//");
                final int nextslash = baseURI.indexOf("/", slashslash + 2);
                if (nextslash != -1) {
                    base = baseURI.substring(0, nextslash);
                } else {
                	base = "";
                }
                return formURI(base, url);
            }

            // Relative URI with parent
            if (parent != null && parent instanceof Element) {

                // Do we have an xml:base?
                String xmlbase = ((Element) parent).getAttributeNS(XMLConstants.XML_NS_URI, "base");
                if (xmlbase != null && xmlbase.trim().length() > 0) {
                    if (isAbsoluteURI(xmlbase)) {
                        // Absolute xml:base, so form URI right now
                        if (url.startsWith("/")) {
                            // Host relative URI
                            final int slashslash = xmlbase.indexOf("//");
                            final int nextslash = xmlbase.indexOf("/", slashslash + 2);
                            if (nextslash != -1) {
                                xmlbase = xmlbase.substring(0, nextslash);
                            }
                            return formURI(xmlbase, url);
                        }
                        if (!xmlbase.endsWith("/")) {
                            // Base URI is filename, strip it off
                            xmlbase = xmlbase.substring(0, xmlbase.lastIndexOf("/"));
                        }
                        return formURI(xmlbase, url);
                    } else {
                        // Relative xml:base, so walk up tree
                        return resolveURI(baseURI, parent.getParentNode(), stripTrailingSlash(xmlbase) + "/" + stripStartingSlash(url));
                    }
                }
                // No xml:base so walk up tree
                return resolveURI(baseURI, parent.getParentNode(), url);

                // Relative URI with no parent (i.e. top of tree), so form URI
                // right now
            } else if (null != baseURI && (parent == null || parent instanceof Document)) {
                return formURI(baseURI, url);
            }
        }

        return url;

    }

    /**
     * Find base URI of feed considering relative URIs.
     *
     * @param root Root element of feed.
     */
    private String findBaseURI(final Element root) throws MalformedURLException {
        String ret = null;
        if (findAtomLink(root, "self") != null) {
            ret = findAtomLink(root, "self");
            if (".".equals(ret) || "./".equals(ret)) {
                ret = "";
            }
            if (null != ret && ret.indexOf("/") != -1) {
                ret = ret.substring(0, ret.lastIndexOf("/"));
            } else if (null != ret) {
            	ret = resolveURI(null, root, ret);
            }
        }
        return ret;
    }

    /**
     * Return URL string of Atom link element under parent element. Link with no rel attribute is
     * considered to be rel="alternate"
     *
     * @param parent Consider only children of this parent element
     * @param rel Consider only links with this relationship
     */
    private String findAtomLink(final Element parent, final String rel) {
        String ret = null;
        final List<Element> linksList = super.getChildren(parent, "link");
        if (linksList != null) {
            for (Element link : linksList) {
                final Attr relAtt = getAttribute(link, "rel");
                final Attr hrefAtt = getAttribute(link, "href");
                if (relAtt == null && "alternate".equals(rel) || relAtt != null && relAtt.getValue().equals(rel)) {
                    ret = hrefAtt.getValue();
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Form URI by combining base with append portion and giving special consideration to append
     * portions that begin with ".."
     *
     * @param base Base of URI, may end with trailing slash
     * @param append String to append, may begin with slash or ".."
     */
    private static String formURI(String base, String append) {
        base = stripTrailingSlash(base);
        append = stripStartingSlash(append);
        if (null != append && append.startsWith("..")) {
            final String[] parts = append.split("/");
            for (final String part : parts) {
                if (null != base && "..".equals(part)) {
                    final int last = base.lastIndexOf("/");
                    if (last != -1) {
                        base = base.substring(0, last);
                        append = append.substring(3, append.length());
                    } else {
                        break;
                    }
                }
            }
        }
        return base + "/" + append;
    }

    /**
     * Strip starting slash from beginning of string.
     */
    private static String stripStartingSlash(String s) {
        if (s != null && s.startsWith("/")) {
            s = s.substring(1, s.length());
        }
        return s;
    }

    /**
     * Strip trailing slash from end of string.
     */
    private static String stripTrailingSlash(String s) {
        if (s != null && s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Parse entry from reader.
     * @param rd the reader from the entry origins
     * @param baseURI the base URI of entry
     * @param locale the Locale used for entry
     * @return The Entry object
     * @throws IOException Any I/O exception
     * @throws IllegalArgumentException Any illegal argument exception
     * @throws FeedException Any feed exception
     */
    public static Entry parseEntry(final Reader rd, final String baseURI, final Locale locale) throws IOException, IllegalArgumentException,
            FeedException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	    	DocumentBuilder db = dbf.newDocumentBuilder();
			final Document d = db.parse(new InputSource(rd));
			final Element fetchedEntryElement = d.getDocumentElement();

	        final Feed feed = new Feed();
	        feed.setFeedType("atom_1.0");
	        final WireFeedOutput wireFeedOutput = new WireFeedOutput();
	        final Document feedDoc = wireFeedOutput.outputDom(feed);
	        Node newN = feedDoc.adoptNode(fetchedEntryElement.cloneNode(true));
	        feedDoc.getFirstChild().appendChild(newN);
	        
	        if (baseURI != null) {
	            feedDoc.getDocumentElement().setAttributeNS(XMLConstants.XML_NS_URI, "base", baseURI);
	        }

	        final WireFeedInput input = new WireFeedInput(false, locale);
	        final Feed parsedFeed = (Feed) input.build(feedDoc);
	        return parsedFeed.getEntries().get(0);
		} catch (ParserConfigurationException | SAXException e) {
			throw new FeedException("Invalid XML", e);
		}
        
        
    }

}
