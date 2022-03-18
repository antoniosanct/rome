/*
 * SSEParserTest.java
 * JUnit based test
 *
 * Created on August 2, 2005, 1:30 PM
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
package com.rometools.modules.sse;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.rometools.modules.AbstractTestCase;
import com.rometools.modules.sse.modules.Conflict;
import com.rometools.modules.sse.modules.History;
import com.rometools.modules.sse.modules.SSEModule;
import com.rometools.modules.sse.modules.Sync;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;
import com.rometools.rome.io.impl.DateParser;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test to verify correctness of SSE subproject.
 */
public class SSEParserTest extends AbstractTestCase {
	
	/**
	 * Public constructor
	 * @param testName the test name.
	 */
    public SSEParserTest(final String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * A standard test suite.
     * @return a test suite
     */
    public static Test suite() {
        return new TestSuite(SSEParserTest.class);
    }

    /**
     * Test of getNamespaceUri method, of class com.rometools.rome.feed.module.sse.SSE091
     */
    public void testGetNamespaceUri() {
        assertEquals("Namespace", SSEModule.SSE_SCHEMA_URI, new SSE091Generator().getNamespaceUri());
    }

    /**
     * A xtestParseGenerateV5 test
     * @throws Exception any exception
     */
    public void xtestParseGenerateV5() throws Exception {
        final URL feedURL = new File(getTestFile("xml/v/v5.xml")).toURI().toURL();
        // parse the document for comparison
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	dbf.setNamespaceAware(true);
    	dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	final Document directlyBuilt = db.parse(feedURL.openConnection().getInputStream());

        // generate the feed back into a document
        final SyndFeedInput input = new SyndFeedInput();
        final SyndFeed inputFeed = input.build(new XmlReader(feedURL));

        final SyndFeedOutput output = new SyndFeedOutput();
        final Document parsedAndGenerated = output.outputDom(inputFeed);

        // XMLOutputter outputter = new XMLOutputter();
        // outputter.setFormat(Format.getPrettyFormat());
        // outputter.output(directlyBuilt, new
        // FileOutputStream("c:\\cygwin\\tmp\\sync-direct.xml"));
        // outputter.output(parsedAndGenerated, new
        // FileOutputStream("c:\\cygwin\\tmp\\sync-pg.xml"));

        assertDocumentsEqual(directlyBuilt, parsedAndGenerated);
    }

    // TODO: probably should rip this out and use xunit instead
    private void assertDocumentsEqual(final Document one, final Document two) {
        assertEqualElements(one.getDocumentElement(), two.getDocumentElement());
    }

    private void assertEqualElements(final Element one, final Element two) {
        if (one == two || bothNull(one, two)) {
            return;
        }

        assertNullEqual("elements not null equal", one, two);
        assertEqualAttributes(one, two);
        asserEqualContent(one, two);
    }

    private void assertNullEqual(final String mesg, final Object one, final Object two) {
        assertTrue(mesg, nullEqual(one, two));
    }

    private boolean nullEqual(final Object one, final Object two) {
        return one == null && two == null || one != null && two != null;
    }

    private boolean bothNull(final Object one, final Object two) {
        return one == null && two == null;
    }

    private void assertEqualAttributes(final Element one, final Element two) {
        assertTrue(equalAttributes(one, two, true));
    }

    private boolean equalAttributes(final Element one, final Element two, final boolean doAssert) {
        final NamedNodeMap attrs1 = one.getAttributes();
        final NamedNodeMap attrs2 = two.getAttributes();

        boolean equal = nullEqual(attrs1, attrs2);
        if (doAssert) {
            assertTrue("not null equal", equal);
        }

        if (bothNull(attrs1, attrs2)) {
            return true;
        }

        if (equal) {
            for (int i = 0; i < attrs1.getLength(); i++) {
                // compare the attributes in an order insensitive way
                final Attr a1 = (Attr) attrs1.item(i);
                final Attr a2 = findAttribute(a1.getName(), attrs2);

                equal = a2 != null;
                if (!equal) {
                    if (doAssert) {
                        assertNotNull("no matching attribute for: " + one.getLocalName() + "." + a1.getName() + "=" + a1.getValue(), a2);
                    }
                    break;
                }

                Object av1 = a1.getValue();
                Object av2 = a2.getValue();

                equal = nullEqual(av1, av2);
                if (!equal && doAssert) {
                    assertNullEqual("attribute values not null equal: " + av1 + " != " + av2, av1, av2);
                }

                if (!bothNull(av1, av2)) {
                    final String a1Name = a1.getName();

                    // this test is brittle, but its comprehensive
                    if ("until".equals(a1Name) || "since".equals(a1Name) || "when".equals(a1Name)) {
                        av1 = DateParser.parseRFC822((String) av1, Locale.US);
                        av2 = DateParser.parseRFC822((String) av2, Locale.US);
                    }

                    assertTrue("unequal attributes:" + one.getLocalName() + "." + a1.getName() + ": " + av1 + " != " + av2, av1.equals(av2));
                }
            }
        }
        return equal;
    }

    private Attr findAttribute(final String name, final NamedNodeMap attrs) {
        for (int i = 0; i < attrs.getLength(); i++) {
        	Attr a = (Attr) attrs.item(i);
            if (a.getName().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }

    private void asserEqualContent(final Element one, final Element two) {
        final NodeList oneContent = one.getChildNodes();
        final NodeList twoContent = two.getChildNodes();
        if (bothNull(oneContent, twoContent)) {
            return;
        }

        assertNullEqual("missing compare content", oneContent, twoContent);
        assertEqualAttributes(one, two);

        // scan through the content to make sure each element is equal
        for (int i = 0; i < oneContent.getLength(); i++) {
        	Element content1 = (Element) oneContent.item(i);
            if (content1 instanceof Element) {
                final Element e1 = (Element) content1;

                boolean foundEqual = false;
                final ArrayList<String> messages = new ArrayList<String>();
                for (int j = 0; j < twoContent.getLength(); j++) {
                	Element o = (Element) twoContent.item(j);
                    if (o instanceof Element) {
                        final Element e2 = (Element) o;

                        try {
                            // have to check all elements to be order insensitive
                            if (e1.getLocalName().equals(e2.getLocalName()) && equalAttributes(e1, e2, false)) {
                                assertEqualElements(e1, e2);
                                foundEqual = true;
                                messages.clear();
                                break;
                            }
                        } catch (final Error e) {
                            messages.add(e.getMessage());
                        }
                    }
                }

                // look for the content in the other tree
                assertTrue("could not find matching element for: " + one.getLocalName(), foundEqual);
            }
        }
    }

    /**
     * Assure v5 file parsed correctly.
     *
     * @throws Exception any exception
     */
    public void xtestV5() throws Exception {
        final File feed = new File(getTestFile("xml/v/v5.xml"));
        final SyndFeedInput input = new SyndFeedInput();
        final SyndFeed syndfeed = input.build(new XmlReader(feed.toURI().toURL()));

        final List<SyndEntry> entries = syndfeed.getEntries();
        final Iterator<SyndEntry> it = entries.iterator();

        for (int id = 101; it.hasNext() && id <= 113; id++) {
            final SyndEntry entry = it.next();
            final Sync sync = (Sync) entry.getModule(SSEModule.SSE_SCHEMA_URI);
            assertEquals(String.valueOf(id), sync.getId());

            final History history = sync.getHistory();
            assertNotNull(history);

            final Date when = history.getWhen();
            assertNotNull(when);
            final Date testDate = DateParser.parseRFC822("Fri, 6 Jan 2006 19:24:09 GMT", Locale.US);
            assertEquals(testDate, when);
        }

        for (int ep = 1; ep <= 2; ep++) {
            for (int i = 100; i < 102; i++) {
                final SyndEntry entry = it.next();
                final Sync sync = (Sync) entry.getModule(SSEModule.SSE_SCHEMA_URI);
                final String id = sync.getId();
                assertEquals("ep" + ep + "." + i, id);

                if (id.equals("ep1.100")) {
                    final List<Conflict> conflicts = sync.getConflicts();
                    assertNotNull(conflicts);

                    final Conflict conflict = conflicts.get(0);
                    final Item conflictItem = conflict.getItem();

                    assertEquals(conflictItem.getTitle(), "Phish - Coventry Live (the last *good* concert)");
                    assertEquals(conflictItem.getDescription().getValue(), "All songs");
                }
            }
        }
    }
}
