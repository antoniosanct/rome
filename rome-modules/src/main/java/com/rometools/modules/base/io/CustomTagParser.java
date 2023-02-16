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

package com.rometools.modules.base.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.rometools.modules.base.CustomTag;
import com.rometools.modules.base.CustomTagImpl;
import com.rometools.modules.base.CustomTags;
import com.rometools.modules.base.CustomTagsImpl;
import com.rometools.modules.base.types.DateTimeRange;
import com.rometools.modules.base.types.FloatUnit;
import com.rometools.modules.base.types.IntUnit;
import com.rometools.modules.base.types.ShortDate;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

public class CustomTagParser extends ChildNavigator implements ModuleParser {

    private static final Logger LOG = LoggerFactory.getLogger(CustomTagParser.class);

    static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace("g-custom", CustomTags.URI);

    public CustomTagParser() {
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
    	final DateFormat SHORT_DT_FMT = new SimpleDateFormat("yyyy-MM-dd");
        final DateFormat LONG_DT_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        final CustomTags module = new CustomTagsImpl();
        final ArrayList<CustomTag> tags = new ArrayList<CustomTag>();
        final List<Element> elements = super.getChildren(element);
        final Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            final Element child = it.next();
            if (child.getNamespaceURI().equals(NS.getNamespaceURI())) {
                final String type = child.getAttribute("type");
                try {
                    if (type == null) {
                        continue;
                    } else if (type.equals("string")) {
                        tags.add(new CustomTagImpl(child.getLocalName(), child.getTextContent()));
                    } else if (type.equals("int")) {
                        tags.add(new CustomTagImpl(child.getLocalName(), Integer.valueOf(child.getTextContent())));
                    } else if (type.equals("float")) {
                        tags.add(new CustomTagImpl(child.getLocalName(), Float.valueOf(child.getTextContent())));
                    } else if (type.equals("intUnit")) {
                        tags.add(new CustomTagImpl(child.getLocalName(), new IntUnit(child.getTextContent())));
                    } else if (type.equals("floatUnit")) {
                        tags.add(new CustomTagImpl(child.getLocalName(), new FloatUnit(child.getTextContent())));
                    } else if (type.equals("date")) {
                        try {
                            tags.add(new CustomTagImpl(child.getLocalName(), new ShortDate(SHORT_DT_FMT.parse(child.getTextContent()))));
                        } catch (final ParseException e) {
                            LOG.warn("Unable to parse date type on " + child.getLocalName(), e);
                        }
                    } else if (type.equals("dateTime")) {
                        try {
                            tags.add(new CustomTagImpl(child.getLocalName(), LONG_DT_FMT.parse(child.getTextContent())));
                        } catch (final ParseException e) {
                            LOG.warn("Unable to parse date type on " + child.getLocalName(), e);
                        }
                    } else if (type.equals("dateTimeRange")) {
                        try {
                            tags.add(new CustomTagImpl(child.getLocalName(), 
                            		new DateTimeRange(LONG_DT_FMT.parse(super.getChild(child, "start", CustomTagParser.NS).getTextContent()),
                            				LONG_DT_FMT.parse(super.getChild(child, "end", CustomTagParser.NS).getTextContent()))));
                        } catch (final Exception e) {
                            LOG.warn("Unable to parse date type on " + child.getLocalName(), e);
                        }
                    } else if (type.equals("url")) {
                        try {
                            tags.add(new CustomTagImpl(child.getLocalName(), new URL(child.getTextContent())));
                        } catch (final MalformedURLException e) {
                            LOG.warn("Unable to parse URL type on " + child.getLocalName(), e);
                        }
                    } else if (type.equals("boolean")) {
                        tags.add(new CustomTagImpl(child.getLocalName(), Boolean.valueOf(child.getTextContent().toLowerCase())));
                    } else if (type.equals("location")) {
                        tags.add(new CustomTagImpl(child.getLocalName(), new CustomTagImpl.Location(child.getTextContent())));
                    } else {
                        throw new Exception("Unknown type: " + type);
                    }
                } catch (final Exception e) {
                    LOG.warn("Unable to parse type on " + child.getLocalName(), e);
                }
            }
        }
        module.setValues(tags);
        return module;
    }

    @Override
    public String getNamespaceUri() {
        return CustomTags.URI;
    }
}
