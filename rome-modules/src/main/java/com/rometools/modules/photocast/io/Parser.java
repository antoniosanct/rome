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

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.rometools.modules.photocast.PhotocastModule;
import com.rometools.modules.photocast.PhotocastModuleImpl;
import com.rometools.modules.photocast.types.Metadata;
import com.rometools.modules.photocast.types.PhotoDate;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
import com.rometools.rome.io.impl.ChildNavigator;

public class Parser extends ChildNavigator implements ModuleParser {

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace(PhotocastModule.URI);
    

    public Parser() {
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
    	// 2005-11-29T04:36:06
        final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        // 2006-01-11 16:42:26 -0800
        final DateFormat CROP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        if (element.getLocalName().equals("channel") || element.getLocalName().equals("feed")) {
            return new PhotocastModuleImpl();
        } else if (super.getChild(element, "metadata", Parser.NS) == null && super.getChild(element, "image", Parser.NS) == null) {
            return null;
        }
        final PhotocastModule pm = new PhotocastModuleImpl();
        final List<Element> children = super.getChildren(element);
        final Iterator<Element> it = children.iterator();
        while (it.hasNext()) {
            final Element e = it.next();
            if (!Parser.NS.getNamespaceURI().equals(e.getNamespaceURI())) {
                continue;
            }
            if (e.getLocalName().equals("photoDate")) {
                try {
                    pm.setPhotoDate(PHOTO_DATE_FORMAT.parse(e.getTextContent()));
                } catch (final Exception ex) {
                    LOG.warn("Unable to parse photoDate: " + e.getTextContent(), ex);
                }
            } else if (e.getLocalName().equals("cropDate")) {
                try {
                    pm.setCropDate(CROP_DATE_FORMAT.parse(e.getTextContent()));
                } catch (final Exception ex) {
                    LOG.warn("Unable to parse cropDate: " + e.getTextContent(), ex);
                }
            } else if (e.getLocalName().equals("thumbnail")) {
                try {
                    pm.setThumbnailUrl(new URL(e.getTextContent()));
                } catch (final Exception ex) {
                    LOG.warn("Unable to parse thumnail: " + e.getTextContent(), ex);
                }
            } else if (e.getLocalName().equals("image")) {
                try {
                    pm.setImageUrl(new URL(e.getTextContent()));
                } catch (final Exception ex) {
                    LOG.warn("Unable to parse image: " + e.getTextContent(), ex);
                }
            } else if (e.getLocalName().equals("metadata")) {
                String comments = "";
                PhotoDate photoDate = null;
                if (super.getChild(e, "PhotoDate") != null) {
                    try {
                        photoDate = new PhotoDate(Double.parseDouble(super.getChild(e, "PhotoDate").getTextContent()));
                    } catch (final Exception ex) {
                        LOG.warn("Unable to parse PhotoDate: " + e.getTextContent(), ex);
                    }
                }
                if (super.getChild(e, "Comments") != null) {
                    comments = super.getChild(e, "Comments").getTextContent();
                }
                pm.setMetadata(new Metadata(photoDate, comments));
            }
        }
        return pm;
    }

    @Override
    public String getNamespaceUri() {
        return PhotocastModule.URI;
    }

}
