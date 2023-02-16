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
package com.rometools.rome.io.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.rome.io.WireFeedParser;

public class RSS20ParserTest {

    private WireFeedParser parser;
    private Document document;

    @Before
    public void setUp() throws Exception {
        parser = new RSS20Parser();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        document = dbf.newDocumentBuilder().newDocument();
    }

    @Test
    public void testIsMyType() {
    	Element e = document.createElement("rss");
    	e.setAttribute("version", "2.0");
    	document.appendChild(e);
        assertTrue(parser.isMyType(document));
    }

    @Test
    public void testIsMyTypeNotMyType() {
    	Element e = document.createElement("rss");
    	e.setAttribute("version", "1.0");
    	document.appendChild(e);
        assertFalse(parser.isMyType(document));
    }

    @Test
    public void testIsMyTypeVersionWithSpaces() {
    	Element e = document.createElement("rss");
    	e.setAttribute("version", " 2.0 ");
    	document.appendChild(e);
        assertTrue(parser.isMyType(document));
    }

    @Test
    public void testIsMyTypeVersionWithTrailingText() {
    	Element e = document.createElement("rss");
    	e.setAttribute("version", "2.0test");
    	document.appendChild(e);
        assertTrue(parser.isMyType(document));
    }

    @Test
    public void testIsMyTypeVersionAbsent() {
    	Element e = document.createElement("rss");
    	document.appendChild(e);
        assertTrue(parser.isMyType(document));
    }
}
