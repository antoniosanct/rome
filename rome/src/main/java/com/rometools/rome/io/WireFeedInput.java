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
package com.rometools.rome.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.impl.ConfigurableClassLoader;
import com.rometools.rome.io.impl.FeedParsers;
import com.rometools.rome.io.impl.XmlFixerReader;

/**
 * Parses an XML document (File, InputStream, Reader, W3C SAX InputSource, W3C DOM Document or JDom
 * DOcument) into an WireFeed (RSS/Atom).

 * It accepts all flavors of RSS (0.90, 0.91, 0.92, 0.93, 0.94, 1.0 and 2.0) and Atom 0.3 feeds.
 * Parsers are plugable (they must implement the WireFeedParser interface).

 * The WireFeedInput useds liberal parsers.
 */
public class WireFeedInput {

    private static Map<ClassLoader, FeedParsers> clMap = new WeakHashMap<ClassLoader, FeedParsers>();

    private final boolean validate;
    private final Locale locale;

    private boolean xmlHealerOn;
    private boolean allowDoctypes = false;

    private static FeedParsers getFeedParsers() {
        synchronized (WireFeedInput.class) {
            final ClassLoader classLoader = ConfigurableClassLoader.INSTANCE.getClassLoader();
            FeedParsers parsers = clMap.get(classLoader);
            if (parsers == null) {
                parsers = new FeedParsers();
                clMap.put(classLoader, parsers);
            }
            return parsers;
        }
    }

    /**
     * Returns the list of supported input feed types.

     *
     * @see WireFeed for details on the format of these strings.

     * @return a list of String elements with the supported input feed types.
     *
     */
    public static List<String> getSupportedFeedTypes() {
        return getFeedParsers().getSupportedFeedTypes();
    }

    /**
     * Creates a WireFeedInput instance with input validation turned off.

     *
     */
    public WireFeedInput() {
        this(false, Locale.US);
    }

    /**
     * Creates a WireFeedInput instance.
     *
     * @param validate indicates if the input should be validated. NOT IMPLEMENTED YET (validation
     *            does not happen)
     * @param locale the Locale instance.
     *
     */
    public WireFeedInput(final boolean validate, final Locale locale) {
        this.validate = false; // FIXME FIX THIS THINGY
        xmlHealerOn = true;
        this.locale = locale;
    }

    /**
     * Enables XML healing in the WiredFeedInput instance.

     * Healing trims leading chars from the stream (empty spaces and comments) until the XML prolog.

     * Healing resolves HTML entities (from literal to code number) in the reader.

     * The healing is done only with the build(File) and build(Reader) signatures.

     * By default is TRUE.

     *
     * @param heals TRUE enables stream healing, FALSE disables it.
     *
     */
    public void setXmlHealerOn(final boolean heals) {
        xmlHealerOn = heals;
    }

    /**
     * Indicates if the WiredFeedInput instance will XML heal (if necessary) the character stream.

     * Healing trims leading chars from the stream (empty spaces and comments) until the XML prolog.

     * Healing resolves HTML entities (from literal to code number) in the reader.

     * The healing is done only with the build(File) and build(Reader) signatures.

     * By default is TRUE.

     *
     * @return TRUE if healing is enabled, FALSE if not.
     *
     */
    public boolean getXmlHealerOn() {
        return xmlHealerOn;
    }
    
    /**
     * Indicates whether Doctype declarations are allowed.
     *  
     * @return true when Doctype declarations are allowed, false otherwise
     */
    public boolean isAllowDoctypes() {
        return allowDoctypes;
    }

    /**
     * Since ROME 1.5.1 we fixed a security vulnerability by disallowing Doctype declarations by default. 
     * This change breaks the compatibility with at least RSS 0.91N because it requires a Doctype declaration. 
     * You are able to allow Doctype declarations again with this property. You should only activate it 
     * when the feeds that you process are absolutely trustful. 
     *  
     * @param allowDoctypes true when Doctype declarations should be allowed again, false otherwise
     */
    public void setAllowDoctypes(boolean allowDoctypes) {
        this.allowDoctypes = allowDoctypes;
    }

    /**
     * Builds an WireFeed (RSS or Atom) from a file.

     * NOTE: This method delages to the 'AsbtractFeed WireFeedInput#build(org.jdom2.Document)'.

     *
     * @param file file to read to create the WireFeed.
     * @return the WireFeed read from the file.
     * @throws FileNotFoundException thrown if the file could not be found.
     * @throws IOException thrown if there is problem reading the file.
     * @throws IllegalArgumentException thrown if feed type could not be understood by any of the
     *             underlying parsers.
     * @throws FeedException if the feed could not be parsed
     *
     */
    public WireFeed build(final File file) throws FileNotFoundException, IOException, IllegalArgumentException, FeedException {
        return this.build(new FileReader(file));
        
    }

    /**
     * Builds an WireFeed (RSS or Atom) from a reader.

     * NOTE: This method delages to the 'AsbtractFeed WireFeedInput#build(org.jdom2.Document)'.

     *
     * @param reader Java Reader object to create the WireFeed.
     * @return the WireFeed read from the Reader object.
     * @throws FileNotFoundException thrown if the Reader object could not be found.
     * @throws IOException thrown if there is problem reading the Reader object.
     * @throws IllegalArgumentException thrown if feed type could not be understood by any of the
     *             underlying parsers.
     * @throws FeedException if the feed could not be parsed
     *
     */
    public WireFeed build(final Reader reader) throws FileNotFoundException, IOException, IllegalArgumentException, FeedException {
    	WireFeed feed;
    	Reader newReader = reader;
        try {
            if (xmlHealerOn) {
            	newReader = new XmlFixerReader(reader);
            }
            feed = this.build(new InputSource(newReader));
        } finally {
        	newReader.close();
        	reader.close();
        }
        return feed;
    }
    
    /**
     * Builds an WireFeed (RSS or Atom) from an W3C SAX InputSource.

     * NOTE: This method delages to the 'AsbtractFeed WireFeedInput#build(org.jdom2.Document)'.

     *
     * @param is W3C SAX InputSource to read to create the WireFeed.
     * @return the WireFeed read from the W3C SAX InputSource.
     * @throws IllegalArgumentException thrown if feed type could not be understood by any of the
     *             underlying parsers.
     * @throws FeedException if the feed could not be parsed
     *
     */
    WireFeed build(final InputSource is) throws IllegalArgumentException, FeedException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new DefaultHandler());
            return this.build(db.parse(is));
        } catch (final Exception ex) {
            throw new FeedException("Invalid XML", ex);
        }
    }


    /**
     * Builds an WireFeed (RSS or Atom) from an JDOM document.

     * NOTE: All other build methods delegate to this method.

     *
     * @param document JDOM document to read to create the WireFeed.
     * @return the WireFeed read from the JDOM document.
     * @throws IllegalArgumentException thrown if feed type could not be understood by any of the
     *             underlying parsers.
     * @throws FeedException if the feed could not be parsed
     *
     */
    public WireFeed build(final Document document) throws IllegalArgumentException, FeedException {
        final WireFeedParser parser = getFeedParsers().getParserFor(document);
        if (parser == null) {
            throw new IllegalArgumentException("Invalid document");
        }
        return parser.parse(document, validate, locale);
    }


}
