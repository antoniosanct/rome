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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.rometools.rome.feed.module.DCModule;
import com.rometools.rome.feed.module.DCModuleImpl;
import com.rometools.rome.feed.module.DCSubject;
import com.rometools.rome.feed.module.DCSubjectImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;

/**
 * Parser for the Dublin Core module.
 */
public class DCModuleParser extends ChildNavigator implements ModuleParser {

    private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String TAXO_URI = "http://purl.org/rss/1.0/modules/taxonomy/";

    private static final Namespace DC_NS = BaseWireFeedParser.createNamespace(DCModule.URI);
    private static final Namespace RDF_NS = BaseWireFeedParser.createNamespace(RDF_URI);
    private static final Namespace TAXO_NS = BaseWireFeedParser.createNamespace(TAXO_URI);

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getNamespaceUri() {
        return DCModule.URI;
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
     * Parse an element tree and return the module found in it.
     *
     * @param dcRoot the root element containing the module elements.
     * @param locale for date/time parsing
     * @return the module parsed from the element tree, <i>null</i> if none.
     */
    @Override
    public Module parse(final Element dcRoot, final Locale locale) {

        boolean foundSomething = false;
        final DCModule dcm = new DCModuleImpl();

        final List<Element> titles = getChildren(dcRoot, "title", getDCNamespace());
        if (null != titles) {
            foundSomething = true;
            dcm.setTitles(parseElementList(titles));
        }

        final List<Element> creators = getChildren(dcRoot, "creator", getDCNamespace());
        if (null != titles) {
            foundSomething = true;
            dcm.setCreators(parseElementList(creators));
        }

        final List<Element> subjects = getChildren(dcRoot, "subject", getDCNamespace());
        if (null != subjects) {
            foundSomething = true;
            dcm.setSubjects(parseSubjects(subjects));
        }

        final List<Element> descriptions = getChildren(dcRoot, "description", getDCNamespace());
        if (null != descriptions) {
            foundSomething = true;
            dcm.setDescriptions(parseElementList(descriptions));
        }

        final List<Element> publishers = getChildren(dcRoot, "publisher", getDCNamespace());
        if (null != publishers) {
            foundSomething = true;
            dcm.setPublishers(parseElementList(publishers));
        }

        final List<Element> contributors = getChildren(dcRoot, "contributor", getDCNamespace());
        if (null != contributors) {
            foundSomething = true;
            dcm.setContributors(parseElementList(contributors));
        }

        final List<Element> dates = getChildren(dcRoot, "date", getDCNamespace());
        if (null != dates) {
            foundSomething = true;
            dcm.setDates(parseElementListDate(dates, locale));
        }

        final List<Element> types = getChildren(dcRoot, "type", getDCNamespace());
        if (null != types) {
            foundSomething = true;
            dcm.setTypes(parseElementList(types));
        }

        final List<Element> formats = getChildren(dcRoot, "format", getDCNamespace());
        if (null != formats) {
            foundSomething = true;
            dcm.setFormats(parseElementList(formats));
        }

        final List<Element> identifiers = getChildren(dcRoot, "identifier", getDCNamespace());
        if (null != identifiers) {
            foundSomething = true;
            dcm.setIdentifiers(parseElementList(identifiers));
        }

        final List<Element> sources = getChildren(dcRoot, "source", getDCNamespace());
        if (null != sources) {
            foundSomething = true;
            dcm.setSources(parseElementList(sources));
        }

        final List<Element> languages = getChildren(dcRoot, "language", getDCNamespace());
        if (null != languages) {
            foundSomething = true;
            dcm.setLanguages(parseElementList(languages));
        }

        final List<Element> relations = getChildren(dcRoot, "relation", getDCNamespace());
        if (null != relations) {
            foundSomething = true;
            dcm.setRelations(parseElementList(relations));
        }

        final List<Element> coverages = getChildren(dcRoot, "coverage", getDCNamespace());
        if (null != coverages) {
            foundSomething = true;
            dcm.setCoverages(parseElementList(coverages));
        }

        final List<Element> rights = getChildren(dcRoot, "rights", getDCNamespace());
        if (null != rights) {
            foundSomething = true;
            dcm.setRightsList(parseElementList(rights));
        }

        if (foundSomething) {
            return dcm;
        } else {
            return null;
        }

    }

    /**
     * Utility method to parse a taxonomy from an element.
     *
     * @param desc the taxonomy description element.
     * @return the string contained in the resource of the element.
     */
    protected final String getTaxonomy(final Element desc) {
        String taxonomy = null;
        final Element topic = getChild(desc, "topic", getTaxonomyNamespace());
        if (topic != null) {
            final Attr resource = topic.getAttributeNodeNS(getRDFNamespace().getNamespaceURI(), "resource");
            if (resource != null) {
                taxonomy = resource.getValue();
            }
        }
        return taxonomy;
    }

    /**
     * Utility method to parse a list of subjects out of a list of elements.
     *
     * @param eList the element list to parse.
     * @return a list of subjects parsed from the elements.
     */
    protected final List<DCSubject> parseSubjects(final List<Element> eList) {

        final List<DCSubject> subjects = new ArrayList<DCSubject>();
        for (Element eSubject : eList) {
            final Element description = getChild(eSubject, "Description", getRDFNamespace());

            if (description != null) {

                final String taxonomy = getTaxonomy(description);

                final List<Element> values = getChildren(description, "value", getRDFNamespace());
                if (values != null) {
                	for (Element d : values) {
	                    final DCSubject subject = new DCSubjectImpl();
	                    subject.setTaxonomyUri(taxonomy);
	                    subject.setValue(d.getTextContent());
	                    subjects.add(subject);
	                }
                }
            } else {
                final DCSubject subject = new DCSubjectImpl();
                subject.setValue(eSubject.getTextContent());
                subjects.add(subject);
            }
        }

        return subjects;
    }

    /**
     * Utility method to parse a list of strings out of a list of elements.
     *
     * @param elements the list of elements to parse.
     * @return the list of strings
     */
    protected final List<String> parseElementList(final List<Element> elements) {
        final List<String> values = new ArrayList<String>();
        for (Element e : elements) {
            values.add(e.getTextContent());
        }
        return values;
    }

    /**
     * Utility method to parse a list of dates out of a list of elements.
     *
     * @param elements the list of elements to parse.
     * @param locale for date/time parsing
     * @return the list of dates.
     */
    protected final List<Date> parseElementListDate(final List<Element> elements, final Locale locale) {
        final List<Date> values = new ArrayList<Date>();
        for (Element e : elements) {
            values.add(DateParser.parseDate(e.getTextContent(), locale));
        }
        return values;
    }

    
}
