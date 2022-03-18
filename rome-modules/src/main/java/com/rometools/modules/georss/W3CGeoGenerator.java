/*
 * Copyright 2006 Marc Wick, geonames.org
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
package com.rometools.modules.georss;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.georss.geometries.AbstractGeometry;
import com.rometools.modules.georss.geometries.Point;
import com.rometools.modules.georss.geometries.Position;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

/**
 * W3CGeoGenerator produces georss elements in georss W3C geo format.
 */
public class W3CGeoGenerator implements ModuleGenerator {

    private static boolean isShort = true;

    private static final Set<Namespace> NAMESPACES;

    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(GeoRSSModule.W3CGEO_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    public static void enableDefaultPointElement() {
        isShort = false;
    }

    /*
     * (non-Javadoc)
     * @see com.rometools.rome.io.ModuleGenerator#getNamespaceUri()
     */
    @Override
    public String getNamespaceUri() {
        return GeoRSSModule.GEORSS_W3CGEO_URI;
    }

    /*
     * (non-Javadoc)
     * @see com.rometools.rome.io.ModuleGenerator#getNamespaces()
     */
    @Override
    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    /*
     * (non-Javadoc)
     * @see com.rometools.rome.io.ModuleGenerator#generate(com.rometools.rome.feed.module.Module,
     * org.jdom2.Element)
     */
    @Override
    public void generate(final Module module, final Element element) {
        // this is not necessary, it is done to avoid the namespace definition
        // in every item.
        Element root = element;
        while (root.getParentNode() != null && root.getParentNode() instanceof Element) {
            root = (Element) element.getParentNode();
        }
        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + GeoRSSModule.W3CGEO_NS.getPrefix(), GeoRSSModule.W3CGEO_NS.getNamespaceURI());

        Element pointElement = element;
        if (!isShort) {
            pointElement = root.getOwnerDocument().createElementNS(GeoRSSModule.W3CGEO_NS.getNamespaceURI(), "Point");
            pointElement.setPrefix(GeoRSSModule.W3CGEO_NS.getPrefix());
            element.appendChild(pointElement);
        }

        final GeoRSSModule geoRSSModule = (GeoRSSModule) module;
        final AbstractGeometry geometry = geoRSSModule.getGeometry();

        if (geometry instanceof Point) {
            final Position pos = ((Point) geometry).getPosition();

            final Element latElement = root.getOwnerDocument().createElementNS(GeoRSSModule.W3CGEO_NS.getNamespaceURI(), "lat");
            latElement.setPrefix(GeoRSSModule.W3CGEO_NS.getPrefix());
            latElement.setTextContent(String.valueOf(pos.getLatitude()));
            pointElement.appendChild(latElement);
            final Element lngElement = root.getOwnerDocument().createElementNS(GeoRSSModule.W3CGEO_NS.getNamespaceURI(), "long");
            lngElement.setPrefix(GeoRSSModule.W3CGEO_NS.getPrefix());
            lngElement.setTextContent(String.valueOf(pos.getLongitude()));
            pointElement.appendChild(lngElement);
        } else {
            System.err.println("W3C Geo format can't handle geometries of type: " + geometry.getClass().getName());
        }
    }
}
