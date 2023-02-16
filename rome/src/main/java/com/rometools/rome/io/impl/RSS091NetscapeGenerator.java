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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import com.rometools.rome.io.FeedException;

/**
 * Feed Generator for RSS 0.91
 * 
 */
public class RSS091NetscapeGenerator extends RSS091UserlandGenerator {

    public RSS091NetscapeGenerator() {
        this("rss_0.91N", "0.91");
    }

    protected RSS091NetscapeGenerator(final String type, final String version) {
        super(type, version);
    }

    protected Document createDocument(final Element root) throws FeedException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
    		dbf.setNamespaceAware(true);
    		dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    		final Document doc = dbf.newDocumentBuilder().newDocument();
			DOMImplementation domImpl = doc.getImplementation();
	        DocumentType doctype = domImpl.createDocumentType(RSS091NetscapeParser.ELEMENT_NAME,
	        		RSS091NetscapeParser.PUBLIC_ID,
	        		RSS091NetscapeParser.SYSTEM_ID);
	        doc.appendChild(doctype);
	        doc.appendChild(root);
	        return doc;
		} catch (ParserConfigurationException e) {
			throw new FeedException("Document builder failed", e);
		}
        
    }

    @Override
    protected String getTextInputLabel() {
        return "textinput";
    }

    /**
     * To be overriden by RSS 0.91 Netscape and RSS 0.94
     */
    @Override
    protected boolean isHourFormat24() {
        return false;
    }

}
