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

package com.rometools.modules.atom.io;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.atom.modules.AtomLinkModule;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.ModuleGenerator;

public class AtomModuleGenerator implements ModuleGenerator {

    static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace("atom", AtomLinkModule.URI);
    private static final Set<Namespace> NAMESPACES;

    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    @Override
    public final String getNamespaceUri() {
        return AtomLinkModule.URI;
    }

    @Override
    public final Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    @Override
    public void generate(Module module, Element element) {
        if (module instanceof AtomLinkModule) {
//        	if (null != element.getParentNode()) {
//        		((Element) element.getParentNode()).setAttributeNS(
//        				XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
//        				String.join(":", XMLConstants.XMLNS_ATTRIBUTE , NS.getPrefix()),
//        				NS.getNamespaceURI());
//        	}
            final AtomLinkModule atom = (AtomLinkModule) module;
            generateLinks(atom.getLinks(), element);
            generatePersons(AtomPersonElement.AUTHOR_PREFIX, atom.getAuthors(), element);
            generatePersons(AtomPersonElement.CONTRIBUTOR_PREFIX, atom.getContributors(), element);
        }
    }

    private Element generateLink(Link link, final Element elem) {
        Element linkElement = elem.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "link");

        if (link.getHref() != null) {
            linkElement.setAttribute(AtomLinkAttribute.HREF, link.getHref());
        }
        if (link.getType() != null) {
            linkElement.setAttribute(AtomLinkAttribute.TYPE, link.getType());
        }
        if (link.getRel() != null) {
            linkElement.setAttribute(AtomLinkAttribute.REL, link.getRel());
        }

        if (link.getHreflang() != null) {
            linkElement.setAttribute(AtomLinkAttribute.HREF_LANG, link.getHreflang());
        }

        if (link.getTitle() != null) {
            linkElement.setAttribute(AtomLinkAttribute.TITLE, link.getTitle());
        }

        if (link.getLength() != 0) {
            linkElement.setAttribute(AtomLinkAttribute.LENGTH, Long.toString(link.getLength()));
        }
        linkElement.setPrefix(NS.getPrefix());
        return linkElement;
    }

    private void generateLinks(List<Link> links, Element element) {
        for (Link link : links) {
            element.appendChild(generateLink(link, element));
        }

    }

    private void generatePersons(String elementName, List<SyndPerson> persons, Element parent) {
        for (SyndPerson person : persons) {
            parent.appendChild(generatePerson(elementName, person, parent));
        }
    }

    private Element generatePerson(String elementName, SyndPerson person, final Element parent) {
        final Element element = parent.getOwnerDocument().createElementNS(NS.getNamespaceURI(), elementName);

        if (person.getName() != null) {
            final Element name = parent.getOwnerDocument().createElementNS(NS.getNamespaceURI(), AtomPersonElement.NAME_ELEMENT);
            name.setPrefix(NS.getPrefix());
            name.setTextContent(person.getName());
            element.appendChild(name);
        }

        if (person.getEmail() != null) {
            final Element email = parent.getOwnerDocument().createElementNS(NS.getNamespaceURI(), AtomPersonElement.EMAIL_ELEMENT);
            email.setPrefix(NS.getPrefix());
            email.setTextContent(person.getEmail());
            element.appendChild(email);
        }

        if (person.getUri() != null) {
            final Element uri = parent.getOwnerDocument().createElementNS(NS.getNamespaceURI(), AtomPersonElement.URI_ELEMENT);
            uri.setPrefix(NS.getPrefix());
            uri.setTextContent(person.getUri());
            element.appendChild(uri);
        }
        element.setPrefix(NS.getPrefix());
        return element;
    }

}
