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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.rometools.modules.opensearch.OpenSearchModule;
import com.rometools.modules.opensearch.entity.OSQuery;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
import com.rometools.rome.io.impl.ChildNavigator;

public class OpenSearchModuleParser extends ChildNavigator implements ModuleParser {

    private static final Namespace OS_NS = XMLEventFactory.newDefaultFactory().createNamespace("opensearch", OpenSearchModule.URI);
    
    @Override
    public String getNamespaceUri() {
        return OpenSearchModule.URI;
    }

    @Override
    public Module parse(final Element dcRoot, final Locale locale) {

        final URL baseURI = findBaseURI(dcRoot);

        boolean foundSomething = false;
        final OpenSearchModule osm = new OpenSearchModuleImpl();

        Element e = super.getChild(dcRoot, "totalResults", OS_NS);

        if (e != null) {
            try {
                osm.setTotalResults(Integer.parseInt(e.getTextContent()));
                foundSomething = true;
            } catch (final NumberFormatException ex) {
                // Ignore setting the field and post a warning
                System.err.println("Warning: The element totalResults must be an integer value: " + ex.getMessage());
            }
        }

        e = super.getChild(dcRoot, "itemsPerPage", OS_NS);
        if (e != null) {
            try {
                osm.setItemsPerPage(Integer.parseInt(e.getTextContent()));
                foundSomething = true;
            } catch (final NumberFormatException ex) {
                // Ignore setting the field and post a warning
                System.err.println("Warning: The element itemsPerPage must be an integer value: " + ex.getMessage());
            }
        }

        e = super.getChild(dcRoot, "startIndex", OS_NS);
        if (e != null) {
            try {
                osm.setStartIndex(Integer.parseInt(e.getTextContent()));
                foundSomething = true;
            } catch (final NumberFormatException ex) {
                // Ignore setting the field and post a warning
                System.err.println("Warning: The element startIndex must be an integer value: " + ex.getMessage());
            }
        }

        final List<Element> queries = super.getChildren(dcRoot, "Query", OS_NS);

        if (queries != null && !queries.isEmpty()) {

            // Create the OSQuery list
            final List<OSQuery> osqList = new LinkedList<OSQuery>();

            for (final Iterator<Element> iter = queries.iterator(); iter.hasNext();) {
                e = iter.next();
                osqList.add(parseQuery(e));
            }

            osm.setQueries(osqList);
        }

        e = super.getChild(dcRoot, "link", OS_NS);

        if (e != null) {
            osm.setLink(parseLink(e, baseURI));
        }

        return foundSomething ? osm : null;
    }

    private static OSQuery parseQuery(final Element e) {

        final OSQuery query = new OSQuery();

        String att = e.getAttribute("role");
        query.setRole(att);

        att = e.getAttribute("osd");
        query.setOsd(att);

        att = e.getAttribute("searchTerms");
        query.setSearchTerms(att);

        att = e.getAttribute("title");
        query.setTitle(att);

        try {

            // someones mistake should not cause the parser to fail, since these
            // are only optional attributes

            att = e.getAttribute("totalResults");
            if (null != att && !"".equals(att)) {
                query.setTotalResults(Integer.parseInt(att));
            }

            att = e.getAttribute("startPage");
            if (null != att && !"".equals(att)) {
                query.setStartPage(Integer.parseInt(att));
            }

        } catch (final NumberFormatException ex) {
            System.err.println("Warning: Exception caught while trying to parse a non-numeric Query attribute " + ex.getMessage());
        }

        return query;
    }

    private static Link parseLink(final Element e, final URL baseURI) {

        final Link link = new Link();

        String att = e.getAttribute("rel");// getAtomNamespace()); DONT
        // KNOW WHY DOESN'T WORK

        if (att != null) {
            link.setRel(att);
        }

        att = e.getAttribute("type");// getAtomNamespace()); DONT KNOW WHY
        // DOESN'T WORK

        if (att != null) {
            link.setType(att);
        }

        att = e.getAttribute("href");// getAtomNamespace()); DONT KNOW WHY
        // DOESN'T WORK

        if (att != null) {

            if (isRelativeURI(att)) { //
                link.setHref(resolveURI(baseURI, e, ""));
            } else {
                link.setHref(att);
            }
        }

        att = e.getAttribute("hreflang");// getAtomNamespace()); DONT KNOW
        // WHY DOESN'T WORK

        if (att != null) {
            link.setHreflang(att);
        }

        att = e.getAttribute("length");// getAtomNamespace()); DONT KNOW
        // WHY DOESN'T WORK

        return link;
    }

    private static boolean isRelativeURI(final String uri) {
        if (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("/")) {
            return false;
        }
        return true;
    }

    /**
     * Use xml:base attributes at feed and entry level to resolve relative links
     */
    private static String resolveURI(final URL baseURI, final Element parent, String url) {
        url = url.equals(".") || url.equals("./") ? "" : url;
        if (isRelativeURI(url) && parent != null && parent instanceof Element
        		&& parent.getParentNode() instanceof Element) {
            final Attr baseAtt = ((Element) parent).getAttributeNodeNS(XMLConstants.XML_NS_URI, "base");
            String xmlBase = baseAtt == null ? "" : baseAtt.getValue();
            if (!isRelativeURI(xmlBase) && !xmlBase.endsWith("/")) {
                xmlBase = xmlBase.substring(0, xmlBase.lastIndexOf("/") + 1);
            }
            return resolveURI(baseURI, (Element) parent.getParentNode(), xmlBase + url);
        } else if (isRelativeURI(url) && parent == null) {
            return baseURI + url;
        } else if (baseURI != null && url.startsWith("/")) {
            String hostURI = baseURI.getProtocol() + "://" + baseURI.getHost();
            if (baseURI.getPort() != baseURI.getDefaultPort()) {
                hostURI = hostURI + ":" + baseURI.getPort();
            }
            return hostURI + url;
        }
        return url;
    }

    /** Use feed links and/or xml:base attribute to determine baseURI of feed */
    private URL findBaseURI(final Element root) {
        URL baseURI = null;
        final List<Element> linksList = super.getChildren(root, "link", OS_NS);
        if (linksList != null) {
            for (final Element element : linksList) {
                final Element link = element;
                if (!root.equals(link.getParentNode())) {
                    break;
                }
                String href = link.getAttribute("href");
                if (link.getAttributeNodeNS(OS_NS.getNamespaceURI(), "rel") == null ||
                		link.getAttributeNodeNS(OS_NS.getNamespaceURI(), "rel").getValue().equals("alternate")) {
                    href = resolveURI(null, link, href);
                    try {
                        baseURI = new URL(href);
                        break;
                    } catch (final MalformedURLException e) {
                        System.err.println("Base URI is malformed: " + href);
                    }
                }
            }
        }
        return baseURI;
    }
}
