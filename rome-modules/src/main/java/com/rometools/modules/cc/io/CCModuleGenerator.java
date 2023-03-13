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

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.cc.CreativeCommons;
import com.rometools.modules.cc.CreativeCommonsImpl;
import com.rometools.modules.cc.types.License;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleGenerator;

public class CCModuleGenerator implements ModuleGenerator {

    private static final Namespace RSS1 = ChildNavigator.createNamespace("cc", CreativeCommonsImpl.RSS1_URI);
    private static final Namespace RSS2 = ChildNavigator.createNamespace("creativeCommons", CreativeCommonsImpl.RSS2_URI);
    private static final Namespace RSS = ChildNavigator.createNamespace("http://purl.org/rss/1.0/");
    private static final Namespace RDF = ChildNavigator.createNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static final HashSet<Namespace> NAMESPACES = new HashSet<Namespace>();
    static {
        NAMESPACES.add(RSS1);
        NAMESPACES.add(RSS2);
        NAMESPACES.add(RDF);
    }

    public CCModuleGenerator() {
        super();
    }

    @Override
    public void generate(final Module module, final Element element) {
        Element root = element;
        while (root.getParentNode() != null) {
            root = (Element) root.getParentNode();
        }
        boolean found = false;
        for (int i = 0; !found && i < root.getAttributes().getLength(); i++) {
        	final String namespaceValue = root.getAttributes().item(i).getTextContent();
        	if (namespaceValue.equals(RDF.getNamespaceURI()) || namespaceValue.equals(RSS.getNamespaceURI())) {
        		found = true;
        	}
        }
        if (found) {
        	generateRSS1((CreativeCommons) module, element);
        } else {
        	generateRSS2((CreativeCommons) module, element);
        }
    }

    @Override
    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    @Override
    public String getNamespaceUri() {
        return CreativeCommons.URI;
    }

    private void generateRSS1(final CreativeCommons module, final Element element) {
        // throw new RuntimeException( "Generating RSS1 Feeds not currently Supported.");

        if (element.getLocalName().equals("channel")) {
            // Do all licenses list.
            final License[] all = module.getAllLicenses();
            for (int i = 0; all != null && i < all.length; i++) {
                final Element license = element.getOwnerDocument().createElementNS(RSS1.getNamespaceURI(), "License");
                license.setPrefix(RSS1.getPrefix());
                license.setAttributeNS(RDF.getNamespaceURI(), "about", all[i].getValue());
                final License.Behaviour[] permits = all[i].getPermits();
                for (int j = 0; permits != null && j < permits.length; j++) {
                    final Element permit = element.getOwnerDocument().createElementNS(RSS1.getNamespaceURI(), "permits");
                    permit.setPrefix(RSS1.getPrefix());
                    permit.setAttributeNS(RDF.getNamespaceURI(), "resource", permits[j].toString());
                    license.appendChild(permit);
                }
                final License.Behaviour[] requires = all[i].getRequires();
                for (int j = 0; requires != null && j < requires.length; j++) {
                    final Element require = element.getOwnerDocument().createElementNS(RSS1.getNamespaceURI(), "requires");
                    require.setPrefix(RSS1.getPrefix());
                    require.setAttributeNS(RDF.getNamespaceURI(), "resource", requires[j].toString());
                    license.appendChild(require);
                }
                element.getParentNode().appendChild(license);
            }
        }

        // Do local licenses
        final License[] licenses = module.getLicenses();
        for (int i = 0; licenses != null && i < licenses.length; i++) {
            final Element license = element.getOwnerDocument().createElementNS(RSS1.getNamespaceURI(), "license");
            license.setPrefix(RSS1.getPrefix());
            license.setAttributeNS(RDF.getNamespaceURI(), "resource", licenses[i].getValue());
            element.appendChild(license);
        }

    }

    private void generateRSS2(final CreativeCommons module, final Element element) {
        final License[] licenses = module.getLicenses();
        for (int i = 0; licenses != null && i < licenses.length; i++) {
            final Element license = element.getOwnerDocument().createElementNS(RSS2.getNamespaceURI(), "license");
            license.setPrefix(RSS2.getPrefix());
            license.setTextContent(licenses[i].getValue());
            element.appendChild(license);
        }
    }
}
