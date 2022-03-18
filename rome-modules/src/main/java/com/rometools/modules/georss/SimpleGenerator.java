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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * SimpleGenerator produces georss elements in georss simple format.
 */
public class SimpleGenerator implements ModuleGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleGenerator.class);

    private static final Set<Namespace> NAMESPACES;
    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(GeoRSSModule.SIMPLE_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    /**
     * @param posList PositionList to convert
     * @return String representation
     */
    private String posListToString(final PositionList posList) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < posList.size(); ++i) {
            sb.append(posList.getLatitude(i)).append(" ").append(posList.getLongitude(i)).append(" ");
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * @see com.rometools.rome.io.ModuleGenerator#getNamespaceUri()
     */
    @Override
    public String getNamespaceUri() {
        return GeoRSSModule.GEORSS_GEORSS_URI;
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

        final GeoRSSModule geoRSSModule = (GeoRSSModule) module;

        final AbstractGeometry geometry = geoRSSModule.getGeometry();
        if (geometry instanceof Point) {
            final Position pos = ((Point) geometry).getPosition();

            final Element pointElement = root.getOwnerDocument().createElementNS(GeoRSSModule.SIMPLE_NS.getNamespaceURI(), "point");
            pointElement.setPrefix(GeoRSSModule.SIMPLE_NS.getPrefix());
            pointElement.setTextContent(pos.getLatitude() + " " + pos.getLongitude());
            element.appendChild(pointElement);
        } else if (geometry instanceof LineString) {
            final PositionList posList = ((LineString) geometry).getPositionList();

            final Element lineElement = root.getOwnerDocument().createElementNS(GeoRSSModule.SIMPLE_NS.getNamespaceURI(), "line");
            lineElement.setPrefix(GeoRSSModule.SIMPLE_NS.getPrefix());
            lineElement.setTextContent(posListToString(posList));
            element.appendChild(lineElement);
        } else if (geometry instanceof Polygon) {
            final AbstractRing ring = ((Polygon) geometry).getExterior();
            if (ring instanceof LinearRing) {
                final PositionList posList = ((LinearRing) ring).getPositionList();
                final Element polygonElement = root.getOwnerDocument().createElementNS(GeoRSSModule.SIMPLE_NS.getNamespaceURI(), "polygon");
                polygonElement.setPrefix(GeoRSSModule.SIMPLE_NS.getPrefix());
                polygonElement.setTextContent(posListToString(posList));
                element.appendChild(polygonElement);
            } else {
                LOG.error("GeoRSS simple format can't handle rings of type: " + ring.getClass().getName());
            }
            if (((Polygon) geometry).getInterior() != null && !((Polygon) geometry).getInterior().isEmpty()) {
                LOG.error("GeoRSS simple format can't handle interior rings (ignored)");
            }
        } else if (geometry instanceof Envelope) {
            final Envelope envelope = (Envelope) geometry;
            final Element boxElement = root.getOwnerDocument().createElementNS(GeoRSSModule.SIMPLE_NS.getNamespaceURI(), "box");
            boxElement.setPrefix(GeoRSSModule.SIMPLE_NS.getPrefix());
            boxElement.setTextContent(envelope.getMinLatitude() + " " + envelope.getMinLongitude() + " " + envelope.getMaxLatitude() + " "
                    + envelope.getMaxLongitude());
            element.appendChild(boxElement);
        } else {
            LOG.error("GeoRSS simple format can't handle geometries of type: " + geometry.getClass().getName());
        }

        if (geoRSSModule.getFeatureNameTag() != null) {
            Element featureNameElement = root.getOwnerDocument().createElementNS(GeoRSSModule.SIMPLE_NS.getNamespaceURI(), "featurename");
            featureNameElement.setPrefix(GeoRSSModule.SIMPLE_NS.getPrefix());
            featureNameElement.setTextContent(geoRSSModule.getFeatureNameTag());
            element.appendChild(featureNameElement);
        }
    }

}
