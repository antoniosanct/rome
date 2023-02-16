/*
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
package com.rometools.rome.unittest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.impl.ConfigurableClassLoader;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.WireFeedInput;

import junit.framework.TestCase;

/**
 * Abstract feed test class.
 */
public abstract class FeedTest extends TestCase {
    
    private final String feedFileName;
    private final boolean allowDoctypes;
    private Document w3cDoc = null;
    private WireFeed wireFeed = null;
    private SyndFeed syndFeed = null;

    private boolean preservingWireFeed = false;

    protected FeedTest(final String feedFileName) {
        this(feedFileName, false);
    }
    
    protected FeedTest(final String feedFileName, boolean allowDoctypes) {
        this.feedFileName = feedFileName;
        this.allowDoctypes = allowDoctypes;
    }

    protected String getFeedFileName() {
        return feedFileName;
    }

    protected Reader getFeedReader() throws Exception {
        final ClassLoader ClassLoader = ConfigurableClassLoader.INSTANCE.getClassLoader();
        final InputStream resource = ClassLoader.getResourceAsStream(getFeedFileName());
        assertNotNull("Could not find resource " + getFeedFileName(), resource);
        return new InputStreamReader(resource);
    }

    protected Document getW3CDoc() throws Exception {
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	dbf.setNamespaceAware(true);
    	dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	return db.parse(new InputSource(getFeedReader()));
    }

    protected WireFeed getWireFeed() throws Exception {
        final WireFeedInput in = new WireFeedInput();
        in.setAllowDoctypes(allowDoctypes);
        return in.build(getFeedReader());
    }

    protected SyndFeed getSyndFeed(final boolean preserveWireFeed) throws Exception {
        final SyndFeedInput in = new SyndFeedInput();
        in.setPreserveWireFeed(preserveWireFeed);
        in.setAllowDoctypes(allowDoctypes);
        return in.build(getFeedReader());
    }

    protected Document getCachedW3CDoc() throws Exception {
        if (w3cDoc == null) {
        	w3cDoc = getW3CDoc();
        }
        return w3cDoc;
    }

    protected WireFeed getCachedWireFeed() throws Exception {
        if (wireFeed == null) {
            wireFeed = getWireFeed();
        }
        return wireFeed;
    }

    protected SyndFeed getCachedSyndFeed(final boolean preserveWireFeed) throws Exception {

        if (syndFeed == null || preservingWireFeed != preserveWireFeed) {
            syndFeed = getSyndFeed(preserveWireFeed);
            preservingWireFeed = preserveWireFeed;
        }
        return syndFeed;

    }

    protected SyndFeed getCachedSyndFeed() throws Exception {
        return this.getCachedSyndFeed(false);
    }

}
