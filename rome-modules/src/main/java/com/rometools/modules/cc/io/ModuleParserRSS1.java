/*
 * Copyright 2005 Robert Cooper, Temple of the Screaming Penguin
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

package com.rometools.modules.cc.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.cc.CreativeCommonsImpl;
import com.rometools.modules.cc.types.License;
import com.rometools.modules.cc.types.License.Behaviour;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

public class ModuleParserRSS1 extends ChildNavigator implements ModuleParser {

    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace(CreativeCommonsImpl.RSS1_URI);
    static final Namespace RDF = XMLEventFactory.newDefaultFactory().createNamespace(CreativeCommonsImpl.RDF_URI);

    public ModuleParserRSS1() {
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        final CreativeCommonsImpl module = new CreativeCommonsImpl();
        {
            // Parsing Channel level.
            Element root = element;
//            while (root.getParentNode() != null) {
//                root = (Element) root.getParentNode();
//            }
            final List<Element> licenseList = super.getChildren(root, "License", NS);
            final List<License> licenses = new ArrayList<>(1);
            final Iterator<Element> it = licenseList.iterator();
            while (it.hasNext()) {
                final Element licenseTag = it.next();
                final String licenseURI = licenseTag.getAttributeNS(RDF.getNamespaceURI(), "about");
                if (licenseURI == null) {
                    continue;
                }
                License license = License.findByValue(licenseURI);
                {
                    final ArrayList<Behaviour> permitsValues = new ArrayList<Behaviour>();
                    final ArrayList<Behaviour> requiresValues = new ArrayList<Behaviour>();
                    final List<Element> permitsTags = super.getChildren(licenseTag, "permits", NS);
                    Iterator<Element> sit = permitsTags.iterator();
                    while (sit.hasNext()) {
                        final Element permitTag = sit.next();
                        permitsValues.add(License.Behaviour.findByValue(permitTag.getAttributeNS(RDF.getNamespaceURI(), "resource")));
                    }
                    final List<Element> requiresTags = super.getChildren(licenseTag, "requires", NS);
                    sit = requiresTags.iterator();
                    while (sit.hasNext()) {
                        final Element requireTag = sit.next();
                        requiresValues.add(License.Behaviour.findByValue(requireTag.getAttributeNS(RDF.getNamespaceURI(), "resource")));
                    }
                    license = new License(licenseURI, requiresValues.toArray(new License.Behaviour[requiresValues.size()]),
                            permitsValues.toArray(new License.Behaviour[permitsValues.size()]));

                }

                licenses.add(license);
            }
            module.setAllLicenses(licenses.toArray(new License[0]));
        }
        final List<License> licenses = new ArrayList<>(1);
        final List<Element> licenseTags = super.getChildren(element, "license", NS);
        final Iterator<Element> lit = licenseTags.iterator();
        while (lit.hasNext()) {
            final Element licenseTag = lit.next();
            licenses.add(License.findByValue(licenseTag.getAttributeNS(RDF.getNamespaceURI(), "resource")));
        }

        if (!licenses.isEmpty()) {
            module.setLicenses(licenses.toArray(new License[licenses.size()]));
        }

        if (module.getLicenses() != null || module.getAllLicenses() != null) {
            return module;
        } else {
            return null;
        }
    }

    @Override
    public String getNamespaceUri() {
        return CreativeCommonsImpl.RSS1_URI;
    }

}
