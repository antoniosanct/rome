/*
 * ContentModuleImplTest.java
 * JUnit based test
 *
 * Created on February 2, 2005, 2:58 PM
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

package com.rometools.modules.content;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.modules.AbstractTestCase;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * Test class for ContentModuleImpl class 
 *
 */
public class ContentModuleImplTest extends AbstractTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ContentModuleImplTest.class);

    private final ContentModuleImpl module = new ContentModuleImpl();
    
    /**
     * List of content items.
     */
    public static List<ContentItem> contentItems = new ArrayList<ContentItem>();

    static {
        ContentItem item = new ContentItem();
        item.setContentFormat("http://www.w3.org/1999/xhtml");
        item.setContentEncoding("http://www.w3.org/TR/REC-xml#dt-wellformed");
        // item.setContentValueNamespaces("http://www.w3.org/1999/xhtml");
        item.setContentValue("<em>This is <strong>very</strong></em> <strong>cool</strong>.");
        item.setContentValueParseType("Literal");

        contentItems.add(item);

        item = new ContentItem();
        item.setContentFormat("http://www.w3.org/TR/html4/");
        item.setContentValue("<em>This is<strong>very</em> cool</strong>.");

        contentItems.add(item);

        item = new ContentItem();
        item.setContentAbout("http://example.org/item/content-here.txt");
        item.setContentFormat("http://www.isi.edu/in-notes/iana/assignments/media-types/text/plain");
        item.setContentValue("This is &gt;very cool&lt;.");

        contentItems.add(item);

        item = new ContentItem();
        item.setContentAbout("http://example.org/item/content.svg");
        item.setContentResource("http://www.w3.org/2000/svg");

        contentItems.add(item);
    }

    /**
     * Public constructor
     * @param testName the test name.
     */
    public ContentModuleImplTest(final String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {

    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    /**
     * Generates a new test suite associated with this test class.
     * @return A test suite associated with this test class.
     */
    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ContentModuleImplTest.class);
    }

    /**
     * Test of getEncodeds method, of class com.totsp.xml.syndication.content.ContentModuleImpl.
     */
    public void testEncodeds() {
        final ArrayList<String> encodeds = new ArrayList<String>();
        encodeds.add("Foo");
        encodeds.add("Bar");
        encodeds.add("Baz");
        module.setEncodeds(encodeds);
        final List<String> check = module.getEncodeds();
        assertTrue(check.equals(encodeds));

    }

    /**
     * Test of getInterface method, of class com.totsp.xml.syndication.content.ContentModuleImpl.
     */
    public void testInterface() {
        LOG.debug("testInterface");
        assertTrue(module.getInterface().equals(ContentModule.class));
    }

    /**
     * Test of getContentItems method, of class com.totsp.xml.syndication.content.ContentModuleImpl.
     */
    public void testContentItems() {
        LOG.debug("testContentItems");
        module.setContentItems(contentItems);
        assertTrue(module.getContentItems().equals(contentItems));
    }

    /**
     * Test of getContents method, of class com.totsp.xml.syndication.content.ContentModuleImpl.
     */
    public void testContents() {
        LOG.debug("testContents");
        final ArrayList<String> contents = new ArrayList<String>();
        contents.add("Foo");
        contents.add("Bar");
        contents.add("Baz");
        module.setContents(contents);
        final List<String> check = module.getContents();
        assertTrue(check.equals(contents));
    }

    /**
     * Test of copyFrom method, of class com.totsp.xml.syndication.content.ContentModuleImpl.
     */
    public void testCopyFrom() {
        LOG.debug("testCopyFrom");
        final ContentModule test = new ContentModuleImpl();
        test.copyFrom(module);
        assertTrue(test.getContentItems().equals(module.getContentItems()) & test.getContents().equals(module.getContents())
                & test.getEncodeds().equals(module.getEncodeds()));
    }

    /**
     * Test for Issue 524
     * @throws IOException Any I/O exception
     * @throws FeedException Any feed exception
     * @throws MalformedURLException  Any malformed URI exception
     * @throws IllegalArgumentException  Any illegal argument exception.
     */
    public void testIssueEncodeds() throws IllegalArgumentException, MalformedURLException, FeedException, IOException {
    	final SyndFeedInput input = new SyndFeedInput();
        final SyndFeed feed = input.build(new XmlReader(new File(getTestFile("xml/test-rdf.xml")).toURI().toURL()));
        final SyndEntry entry = feed.getEntries().get(0);
    	final List<String> contents = new ArrayList<>();
    	contents.add("<h1>Welcome to Modules from the ROME Framework!!</h1>");
    	final ContentModule test = new ContentModuleImpl();
        test.copyFrom(module);
        test.setEncodeds(contents);
    	entry.getModules().add(module);
    }
}
