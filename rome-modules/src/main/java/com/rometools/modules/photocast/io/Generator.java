/*
 * Copyright 2006 Robert Cooper, Temple of the Screaming Penguin
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

package com.rometools.modules.photocast.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.photocast.PhotocastModule;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

public class Generator implements ModuleGenerator {

    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace("apple-wallpapers", PhotocastModule.URI);
    private static final HashSet<Namespace> NAMESPACES = new HashSet<Namespace>();
    private static final String FEED_VERSION = "0.9";
    static {
        NAMESPACES.add(NS);
    }

    public Generator() {
        super();
    }

    @Override
    public void generate(final Module module, final Element element) {
    	// 2005-11-29T04:36:06
        final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        // 2006-01-11 16:42:26 -0800
        final DateFormat CROP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        
        if (!(module instanceof PhotocastModule)) {
            return;
        }
        final PhotocastModule pm = (PhotocastModule) module;
        if (element.getLocalName().equals("channel") || element.getLocalName().equals("feed")) {
            element.appendChild(generateSimpleElement("feedVersion", FEED_VERSION, element));
            return;
        }
        element.appendChild(generateSimpleElement("photoDate", PHOTO_DATE_FORMAT.format(pm.getPhotoDate()), element));
        element.appendChild(generateSimpleElement("cropDate", CROP_DATE_FORMAT.format(pm.getCropDate()), element));
        element.appendChild(generateSimpleElement("thumbnail", pm.getThumbnailUrl().toString(), element));
        element.appendChild(generateSimpleElement("image", pm.getImageUrl().toString(), element));
        final Element e = element.getOwnerDocument().createElementNS(NS.getNamespaceURI(), "metadata");
        final Element pd = element.getOwnerDocument().createElement("PhotoDate");
        pd.setTextContent(pm.getMetadata().getPhotoDate().toString());
        e.appendChild(pd);
        final Element com = element.getOwnerDocument().createElement("Comments");
        com.setTextContent(pm.getMetadata().getComments());
        e.appendChild(com);
        element.appendChild(e);
    }

    @Override
    public Set<Namespace> getNamespaces() {
        return Generator.NAMESPACES;
    }

    @Override
    public String getNamespaceUri() {
        return PhotocastModule.URI;
    }

    protected Element generateSimpleElement(final String name, final String value, final Element parent) {
        final Element element = parent.getOwnerDocument().createElementNS(NS.getNamespaceURI(), name);
        element.setPrefix(NS.getPrefix());
        element.setTextContent(value);

        return element;
    }

}
