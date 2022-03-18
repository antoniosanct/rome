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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.georss.geometries.AbstractGeometry;
import com.rometools.modules.georss.geometries.AbstractRing;
import com.rometools.modules.georss.geometries.Envelope;
import com.rometools.modules.georss.geometries.LineString;
import com.rometools.modules.georss.geometries.LinearRing;
import com.rometools.modules.georss.geometries.Point;
import com.rometools.modules.georss.geometries.Polygon;
import com.rometools.modules.georss.geometries.Position;
import com.rometools.modules.georss.geometries.PositionList;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

/**
 * GMLGenerator produces georss elements in georss GML format.
 */
public class GMLGenerator implements ModuleGenerator {

    private static final Set<Namespace> NAMESPACES;

    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(GeoRSSModule.SIMPLE_NS);
        nss.add(GeoRSSModule.GML_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    private Element createPosListElement(final PositionList posList, final Element parent) {
        final Element posElement = parent.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "posList");
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < posList.size(); ++i) {
            sb.append(posList.getLatitude(i)).append(" ").append(posList.getLongitude(i)).append(" ");
        }

        posElement.setTextContent(sb.toString());
        posElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
        return posElement;
    }

    /*
     * (non-Javadoc)
     * @see com.rometools.rome.io.ModuleGenerator#getNamespaceUri()
     */
    @Override
    public String getNamespaceUri() {
        return GeoRSSModule.GEORSS_GML_URI;
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
//        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + GeoRSSModule.SIMPLE_NS.getPrefix(), GeoRSSModule.SIMPLE_NS.getNamespaceURI());
//        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + GeoRSSModule.GML_NS.getPrefix(), GeoRSSModule.GML_NS.getNamespaceURI());

        final Element whereElement = element.getOwnerDocument().createElementNS(GeoRSSModule.SIMPLE_NS.getNamespaceURI(), "where");
        whereElement.setPrefix(GeoRSSModule.SIMPLE_NS.getPrefix());
        element.appendChild(whereElement);

        final GeoRSSModule geoRSSModule = (GeoRSSModule) module;
        final AbstractGeometry geometry = geoRSSModule.getGeometry();

        if (geometry instanceof Point) {
            final Position pos = ((Point) geometry).getPosition();

            final Element pointElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "Point");
            pointElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
            whereElement.appendChild(pointElement);

            final Element posElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "pos");
            posElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
            posElement.setTextContent(String.valueOf(pos.getLatitude()) + " " + String.valueOf(pos.getLongitude()));
            pointElement.appendChild(posElement);
        }

        else if (geometry instanceof LineString) {
            final PositionList posList = ((LineString) geometry).getPositionList();

            final Element lineElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "LineString");
            lineElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
            lineElement.appendChild(createPosListElement(posList, element));
            whereElement.appendChild(lineElement);
        } else if (geometry instanceof Polygon) {
            final Element polygonElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "Polygon");
            polygonElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
            {
                final AbstractRing ring = ((Polygon) geometry).getExterior();
                if (ring instanceof LinearRing) {
                    final Element exteriorElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "exterior");
                    exteriorElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
                    polygonElement.appendChild(exteriorElement);
                    final Element ringElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "LinearRing");
                    ringElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
                    ringElement.appendChild(createPosListElement(((LinearRing) ring).getPositionList(), element));
                    exteriorElement.appendChild(ringElement);
                    

                } else {
                    System.err.println("GeoRSS GML format can't handle rings of type: " + ring.getClass().getName());
                }
            }
            final List<AbstractRing> interiorList = ((Polygon) geometry).getInterior();
            final Iterator<AbstractRing> it = interiorList.iterator();
            while (it.hasNext()) {
                final AbstractRing ring = it.next();
                if (ring instanceof LinearRing) {
                    final Element interiorElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "interior");
                    interiorElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
                    polygonElement.appendChild(interiorElement);
                    final Element ringElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "LinearRing");
                    ringElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
                    ringElement.appendChild(createPosListElement(((LinearRing) ring).getPositionList(), element));
                    interiorElement.appendChild(ringElement);

                } else {
                    System.err.println("GeoRSS GML format can't handle rings of type: " + ring.getClass().getName());
                }
            }
            whereElement.appendChild(polygonElement);
        } else if (geometry instanceof Envelope) {
            final Envelope envelope = (Envelope) geometry;
            final Element envelopeElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "Envelope");
            envelopeElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
            whereElement.appendChild(envelopeElement);

            final Element lowerElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "lowerCorner");
            lowerElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
            lowerElement.setTextContent(String.valueOf(envelope.getMinLatitude()) + " " + String.valueOf(envelope.getMinLongitude()));
            envelopeElement.appendChild(lowerElement);

            final Element upperElement = element.getOwnerDocument().createElementNS(GeoRSSModule.GML_NS.getNamespaceURI(), "upperCorner");
            upperElement.setPrefix(GeoRSSModule.GML_NS.getPrefix());
            upperElement.setTextContent(String.valueOf(envelope.getMaxLatitude()) + " " + String.valueOf(envelope.getMaxLongitude()));
            envelopeElement.appendChild(upperElement);

        } else {
            System.err.println("GeoRSS GML format can't handle geometries of type: " + geometry.getClass().getName());
        }
    }
}
