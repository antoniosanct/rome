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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rometools.rome.feed.module.DCModule;
import com.rometools.rome.feed.module.DCSubject;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

/**
 * Feed Generator for DublinCore Module.
 * 
 */
public class DCModuleGenerator implements ModuleGenerator {

    private static final String DC_URI = "http://purl.org/dc/elements/1.1/";
    private static final String TAXO_URI = "http://purl.org/rss/1.0/modules/taxonomy/";
    private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private static final Namespace DC_NS = BaseWireFeedParser.createNamespace("dc", DC_URI);
    private static final Namespace TAXO_NS = BaseWireFeedParser.createNamespace("taxo", TAXO_URI);
    private static final Namespace RDF_NS = BaseWireFeedParser.createNamespace("rdf", RDF_URI);

    private static final Set<Namespace> NAMESPACES;

    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(DC_NS);
        nss.add(TAXO_NS);
        nss.add(RDF_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    @Override
    public final String getNamespaceUri() {
        return DC_URI;
    }

    private final Namespace getDCNamespace() {
        return DC_NS;
    }

    private final Namespace getRDFNamespace() {
        return RDF_NS;
    }

    private final Namespace getTaxonomyNamespace() {
        return TAXO_NS;
    }

    /**
     * Returns a set with all the URIs (JDOM Namespace elements) this module generator uses.
     * 
     * It is used by the the feed generators to add their namespace definition in the root element
     * of the generated document (forward-missing of Java 5.0 Generics).
     * 
     *
     * @return a set with all the URIs this module generator uses.
     */
    @Override
    public final Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    /**
     * Populate an element tree with elements for a module.

     *
     * @param module the module to populate from.
     * @param element the root element to attach child elements to.
     * @throws ParserConfigurationException 
     * @throws DOMException 
     */
    @Override
    public final void generate(final Module module, final Element element) throws DOMException {

        final DCModule dcModule = (DCModule) module;

        final String title = dcModule.getTitle();
        if (title != null) {
            element.appendChild(generateSimpleElementList("title", dcModule.getTitles(), element));
        }

        final String creator = dcModule.getCreator();
        if (creator != null) {
            element.appendChild(generateSimpleElementList("creator", dcModule.getCreators(), element));
        }

        final List<DCSubject> subjects = dcModule.getSubjects();
        for (final DCSubject dcSubject : subjects) {
            element.appendChild(generateSubjectElement(dcSubject, element));
        }

        final String description = dcModule.getDescription();
        if (description != null) {
            element.appendChild(generateSimpleElementList("description", dcModule.getDescriptions(), element));
        }

        final String publisher = dcModule.getPublisher();
        if (publisher != null) {
            element.appendChild(generateSimpleElementList("publisher", dcModule.getPublishers(), element));
        }

        final List<String> contributors = dcModule.getContributors();
        if (null != contributors && !contributors.isEmpty()) {
            element.appendChild(generateSimpleElementList("contributor", contributors, element));
        }

        final Date dcDate = dcModule.getDate();
        if (dcDate != null) {
            for (final Date date : dcModule.getDates()) {
                element.appendChild(generateSimpleElement("date", DateParser.formatW3CDateTime(date, Locale.US), element));
            }
        }

        final String type = dcModule.getType();
        if (type != null) {
            element.appendChild(generateSimpleElementList("type", dcModule.getTypes(), element));
        }

        final String format = dcModule.getFormat();
        if (format != null) {
            element.appendChild(generateSimpleElementList("format", dcModule.getFormats(), element));
        }

        final String identifier = dcModule.getIdentifier();
        if (identifier != null) {
            element.appendChild(generateSimpleElementList("identifier", dcModule.getIdentifiers(), element));
        }

        final String source = dcModule.getSource();
        if (source != null) {
            element.appendChild(generateSimpleElementList("source", dcModule.getSources(), element));
        }

        final String language = dcModule.getLanguage();
        if (language != null) {
            element.appendChild(generateSimpleElementList("language", dcModule.getLanguages(), element));
        }

        final String relation = dcModule.getRelation();
        if (relation != null) {
            element.appendChild(generateSimpleElementList("relation", dcModule.getRelations(), element));
        }

        final String coverage = dcModule.getCoverage();
        if (coverage != null) {
            element.appendChild(generateSimpleElementList("coverage", dcModule.getCoverages(), element));
        }

        final String rights = dcModule.getRights();
        if (rights != null) {
            element.appendChild(generateSimpleElementList("rights", dcModule.getRightsList(), element));
        }

    }

    /**
     * Utility method to generate an element for a subject.

     *
     * @param subject the subject to generate an element for.
     * @return the element for the subject.
     */
    protected final Element generateSubjectElement(final DCSubject subject, final Element element) {
        final Element subjectElement = element.getOwnerDocument().createElementNS(getDCNamespace().getNamespaceURI(), "subject");
        subjectElement.setPrefix(getDCNamespace().getPrefix());
        
        final String taxonomyUri = subject.getTaxonomyUri();
        final String value = subject.getValue();

        if (taxonomyUri != null) {

            final Element topicElement = element.getOwnerDocument().createElementNS(getTaxonomyNamespace().getNamespaceURI(), "topic");
            topicElement.setPrefix(getTaxonomyNamespace().getPrefix());
            topicElement.setAttributeNS(getRDFNamespace().getValue(), "resource", taxonomyUri);
            final Element descriptionElement = element.getOwnerDocument().createElementNS(getRDFNamespace().getNamespaceURI(), "Description");
            descriptionElement.setPrefix(getRDFNamespace().getPrefix());
            descriptionElement.appendChild(topicElement);
            
            if (value != null) {
                final Element valueElement = element.getOwnerDocument().createElementNS(getRDFNamespace().getNamespaceURI(), "value");
                valueElement.setPrefix(getRDFNamespace().getPrefix());
                valueElement.setTextContent(value);
                descriptionElement.appendChild(valueElement);
            }

            subjectElement.appendChild(descriptionElement);

        } else {
            subjectElement.setTextContent(value);
        }

        return subjectElement;
    }

    /**
     * Utility method to generate a single element containing a string.

     *
     * @param name the name of the elment to generate.
     * @param value the value of the text in the element.
     * @return the element generated.
     * @throws ParserConfigurationException 
     */
    protected final Node generateSimpleElement(final String name, final String value, final Element element) {
        final Node child = element.getOwnerDocument().createElementNS(getDCNamespace().getValue(), name);
        child.setPrefix(getDCNamespace().getPrefix());
        child.setTextContent(value);
        return child;
        
    }

    /**
     * Utility method to generate a list of simple elements.

     *
     * @param name the name of the element list to generate.
     * @param values the list of values for the elements.
     * @return a list of Elements created.
     * @throws ParserConfigurationException 
     * @throws DOMException 
     */
    protected final Node generateSimpleElementList(final String name, final List<String> values, final Element element) throws DOMException {
        final Element child = element.getOwnerDocument().createElement(name);
        for (final String value : values) {
            child.appendChild(generateSimpleElement(name, value, element));
        }
        return child;
    }
}
