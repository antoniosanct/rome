/*
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

package com.rometools.modules.opensearch.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.opensearch.OpenSearchModule;
import com.rometools.modules.opensearch.RequiredAttributeMissingException;
import com.rometools.modules.opensearch.entity.OSQuery;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleGenerator;

public class OpenSearchModuleGenerator implements ModuleGenerator {

    private static final Namespace OS_NS = ChildNavigator.createNamespace("opensearch", OpenSearchModule.URI);

    @Override
    public String getNamespaceUri() {
        return OpenSearchModule.URI;
    }

    private static final Set<Namespace> NAMESPACES;

    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(OS_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    /**
     * Returns a set with all the URIs (JDOM Namespace elements) this module generator uses.
     * 
     * It is used by the the feed generators to add their namespace definition in the root element
     * of the generated document (forward-missing of Java 5.0 Generics).
     *
     * @return a set with all the URIs (JDOM Namespace elements) this module generator uses.
     */
    @Override
    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    @Override
    public void generate(final Module module, final Element element) {

        final OpenSearchModule osm = (OpenSearchModule) module;

        if (osm.getItemsPerPage() > -1) {
            element.appendChild(generateSimpleElement("itemsPerPage", Integer.toString(osm.getItemsPerPage()), element));
        }

        if (osm.getTotalResults() > -1) {
            element.appendChild(generateSimpleElement("totalResults", Integer.toString(osm.getTotalResults()), element));
        }

        final int startIndex = osm.getStartIndex() > 0 ? osm.getStartIndex() : 1;
        element.appendChild(generateSimpleElement("startIndex", Integer.toString(startIndex), element));

        if (osm.getQueries() != null) {

            final List<OSQuery> queries = osm.getQueries();

            for (final OSQuery query : queries) {
                if (query != null) {
                    element.appendChild(generateQueryElement(query, element));
                }
            }
        }

        if (osm.getLink() != null) {
            element.appendChild(generateLinkElement(osm.getLink(), element));
        }
    }

    protected Element generateQueryElement(final OSQuery query, final Element parent) {

        final Element qElement = parent.getOwnerDocument().createElementNS(OS_NS.getNamespaceURI(), "Query");

        if (query.getRole() != null) {
            qElement.setAttribute("role", query.getRole());
        } else {
            throw new RequiredAttributeMissingException("If declaring a Query element, the field 'role' must be be specified");
        }

        if (query.getOsd() != null) {
            qElement.setAttribute("osd", query.getOsd());
        }

        if (query.getSearchTerms() != null) {
            qElement.setAttribute("searchTerms", query.getSearchTerms());
        }

        if (query.getStartPage() > -1) {
            final int startPage = query.getStartPage() != 0 ? query.getStartPage() : 1;
            qElement.setAttribute("startPage", Integer.toString(startPage));
        }

        if (query.getTitle() != null) {
            qElement.setAttribute("title", query.getTitle());
        }

        if (query.getTotalResults() > -1) {
            qElement.setAttribute("totalResults", Integer.toString(query.getTotalResults()));
        }

        return qElement;
    }

    protected Element generateLinkElement(final Link link, final Element parent) {
        final Element linkElement = parent.getOwnerDocument().createElementNS(OS_NS.getNamespaceURI(), "link");

        if (link.getRel() != null) {
            linkElement.setAttribute("rel", "search");
        }

        if (link.getType() != null) {
            linkElement.setAttribute("type", link.getType());
        }

        if (link.getHref() != null) {
            linkElement.setAttribute("href", link.getHref());
        }

        if (link.getHreflang() != null) {
            linkElement.setAttribute("hreflang", link.getHreflang());
        }
        linkElement.setPrefix(OS_NS.getPrefix());
        return linkElement;
    }

    protected Element generateSimpleElement(final String name, final String value, final Element parent) {

        final Element element = parent.getOwnerDocument().createElementNS(OS_NS.getNamespaceURI(), name);
        element.setPrefix(OS_NS.getPrefix());
        element.setTextContent(value);

        return element;
    }

}
