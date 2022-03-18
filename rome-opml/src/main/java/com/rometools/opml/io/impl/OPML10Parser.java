/*
 * Opml10Parser.java
 *
 * Created on April 24, 2006, 11:34 PM
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
package com.rometools.opml.io.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.rometools.opml.feed.opml.Attribute;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedParser;
import com.rometools.rome.io.impl.BaseWireFeedParser;
import com.rometools.rome.io.impl.DateParser;

/**
 * OPML 1.0 Parser class
 *
 */
public class OPML10Parser extends BaseWireFeedParser implements WireFeedParser {

    /**
     * Public constructor.
     */
    public OPML10Parser() {
        super("opml_1.0", null);
    }

    /**
     * Public constructor.
     * @param type the OPML 1.0 type (opml_1.0 by default)
     */
    public OPML10Parser(final String type) {
        super(type, null);
    }

    /**
     * Inspects an XML Document (W3C) to check if it can parse it.
     * <p>
     * It checks if the given document if the type of feeds the parser understands.
     * <p>
     *
     * @param document XML Document (W3C) to check if it can be parsed by this parser.
     * @return <b>true</b> if the parser know how to parser this feed, <b>false</b> otherwise.
     */
    @Override
    public boolean isMyType(final Document document) {
        final Element e = document.getDocumentElement();

        if (e.getNodeName().equals("opml") && (super.getChild(e, "head") == null || super.getChild(super.getChild(e, "head"), "docs") == null)
                && (e.getAttribute("version") == null || e.getAttribute("version").equals("1.0"))) {
            return true;
        }

        return false;
    }

    /**
     * Parses an XML document (W3C Document) into a feed bean.
     * <p>
     *
     * @param document XML document (W3C) to parse.
     * @param validate indicates if the feed should be strictly validated (NOT YET IMPLEMENTED).
     * @return the resulting feed bean.
     * @throws IllegalArgumentException thrown if the parser cannot handle the given feed type.
     * @throws FeedException thrown if a feed bean cannot be created out of the XML document (W3C).
     */
    @Override
    public WireFeed parse(final Document document, final boolean validate, final Locale locale) throws IllegalArgumentException, FeedException {
        final Opml opml = new Opml();
        opml.setFeedType("opml_1.0");

        final Element root = document.getDocumentElement();
        final Element head = super.getChild(root, "head");

        if (head != null) {
            opml.setTitle(super.getChild(head, "title").getTextContent());

            if (super.getChild(head, "dateCreated") != null) {
                opml.setCreated(DateParser.parseRFC822(super.getChild(head, "dateCreated").getTextContent(), Locale.US));
            }

            if (super.getChild(head, "dateModified") != null) {
                opml.setModified(DateParser.parseRFC822(super.getChild(head, "dateModified").getTextContent(), Locale.US));
            }

            if (super.getChild(head, "ownerName") != null) {
            	opml.setOwnerName(super.getChild(head, "ownerName").getTextContent().trim());
            }
            if (super.getChild(head, "ownerEmail") != null) {
            	opml.setOwnerEmail(super.getChild(head, "ownerEmail").getTextContent().trim());
            }
            if (super.getChild(head, "vertScrollState") != null) {
            	opml.setVerticalScrollState(readInteger(super.getChild(head, "vertScrollState").getTextContent()));
            }
            if (super.getChild(head, "windowBottom") != null) {
		        try {
		            opml.setWindowBottom(readInteger(super.getChild(head, "windowBottom").getTextContent()));
		        } catch (final NumberFormatException nfe) {
		            if (validate) {
		                throw new FeedException("Unable to parse windowBottom", nfe);
		            }
		        }
            }
            if (super.getChild(head, "windowLeft") != null) {
	            try {
	                opml.setWindowLeft(readInteger(super.getChild(head, "windowLeft").getTextContent()));
	            } catch (final NumberFormatException nfe) {
	                if (validate) {
	                    throw new FeedException("Unable to parse windowLeft", nfe);
	                }
	            }
            }
            if (super.getChild(head, "windowRight") != null) {
	            try {
	                opml.setWindowRight(readInteger(super.getChild(head, "windowRight").getTextContent()));
	            } catch (final NumberFormatException nfe) {
	                if (validate) {
	                    throw new FeedException("Unable to parse windowRight", nfe);
	                }
	            }
            }
            if (super.getChild(head, "windowRight") != null) {
	            try {
	                opml.setWindowTop(readInteger(super.getChild(head, "windowTop").getTextContent()));
	            } catch (final NumberFormatException nfe) {
	                if (validate) {
	                    throw new FeedException("Unable to parse windowTop", nfe);
	                }
	            }
            }
            if (super.getChild(head, "expansionState") != null) {
	            try {
	                opml.setExpansionState(readIntArray(super.getChild(head, "expansionState").getTextContent()));
	            } catch (final NumberFormatException nfe) {
	                if (validate) {
	                    throw new FeedException("Unable to parse expansionState", nfe);
	                }
	            }
            }
        }
        final Element body = super.getChild(root, "body");
        opml.setOutlines(parseOutlines(super.getChildren(body, "outline"), validate, locale));
        opml.setModules(parseFeedModules(root, locale));

        return opml;
    }

    protected Outline parseOutline(final Element e, final boolean validate, final Locale locale) throws FeedException {
        if (!e.getLocalName().equals("outline")) {
            throw new RuntimeException("Not an outline element.");
        }

        final Outline outline = new Outline();
        outline.setText(super.getAttributeNotBlank("text", e));
        outline.setType(super.getAttributeNotBlank("type", e));
        outline.setTitle(super.getAttributeNotBlank("title", e));

        final NamedNodeMap jAttributes = e.getAttributes();
        final List<Attribute> attributes = new ArrayList<>(1);

        for (int i = 0; i < jAttributes.getLength(); i++) {
            final Attr a = (Attr) jAttributes.item(i);

            if (!a.getName().equals("isBreakpoint") && !a.getName().equals("isComment") && !a.getName().equals("title") && !a.getName().equals("text")
                    && !a.getName().equals("type")) {
                attributes.add(new Attribute(a.getName(), a.getValue()));
            }
        }

        outline.setAttributes(attributes);

        try {
            outline.setBreakpoint(readBoolean(e.getAttribute("isBreakpoint")));
        } catch (final Exception ex) {
            if (validate) {
                throw new FeedException("Unable to parse isBreakpoint value", ex);
            }
        }

        try {
            outline.setComment(readBoolean(e.getAttribute("isComment")));
        } catch (final Exception ex) {
            if (validate) {
                throw new FeedException("Unable to parse isComment value", ex);
            }
        }

        final List<Element> children = super.getChildren(e, "outline");
        outline.setModules(parseItemModules(e, locale));
        outline.setChildren(parseOutlines(children, validate, locale));

        return outline;
    }

    protected List<Outline> parseOutlines(final List<Element> elements, final boolean validate, final Locale locale) throws FeedException {
        final ArrayList<Outline> results = new ArrayList<Outline>();
        for (Element e : elements) {
            results.add(parseOutline(e, validate, locale));
        }
        return results;
    }

    protected boolean readBoolean(final String value) {
        if (value == null) {
            return false;
        } else {
            return Boolean.getBoolean(value.trim());
        }
    }

    protected int[] readIntArray(final String value) {
        if (value == null) {
            return null;
        } else {
            final StringTokenizer tok = new StringTokenizer(value, ",");
            final int[] result = new int[tok.countTokens()];
            int count = 0;

            while (tok.hasMoreElements()) {
                result[count] = Integer.parseInt(tok.nextToken().trim());
                count++;
            }

            return result;
        }
    }

    protected Integer readInteger(final String value) {
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }
}
