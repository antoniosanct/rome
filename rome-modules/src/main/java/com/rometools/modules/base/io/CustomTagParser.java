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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.modules.base.CustomTag;
import com.rometools.modules.base.CustomTagImpl;
import com.rometools.modules.base.CustomTags;
import com.rometools.modules.base.CustomTagsImpl;
import com.rometools.modules.base.types.DateTimeRange;
import com.rometools.modules.base.types.FloatUnit;
import com.rometools.modules.base.types.IntUnit;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;

public class CustomTagParser implements ModuleParser {

    private static final Logger LOG = LoggerFactory.getLogger(CustomTagParser.class);

    static final Namespace NS = Namespace.getNamespace("g-custom", CustomTags.URI);

    public CustomTagParser() {
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        final CustomTags module = new CustomTagsImpl();
        final ArrayList<CustomTag> tags = new ArrayList<CustomTag>();
        final List<Element> elements = element.getChildren();
        final Iterator<Element> it = elements.iterator();
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        final DateTimeFormatter longDtFmt = builder.toFormatter().withLocale(Locale.US);
        builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        final DateTimeFormatter shortDtFmt = builder.toFormatter().withLocale(Locale.US);
        
        while (it.hasNext()) {
            final Element child = it.next();
            if (child.getNamespace().equals(NS)) {
                final String type = child.getAttributeValue("type");
                try {
                    if (type == null) {
                        continue;
                    } else if (type.equals("string")) {
                        tags.add(new CustomTagImpl(child.getName(), child.getText()));
                    } else if (type.equals("int")) {
                        tags.add(new CustomTagImpl(child.getName(), new Integer(child.getTextTrim())));
                    } else if (type.equals("float")) {
                        tags.add(new CustomTagImpl(child.getName(), new Float(child.getTextTrim())));
                    } else if (type.equals("intUnit")) {
                        tags.add(new CustomTagImpl(child.getName(), new IntUnit(child.getTextTrim())));
                    } else if (type.equals("floatUnit")) {
                        tags.add(new CustomTagImpl(child.getName(), new FloatUnit(child.getTextTrim())));
                    } else if (type.equals("date")) {
                        try {
                            final String dateStr = child.getTextTrim();
                            final LocalDate ldt = LocalDate.parse(dateStr, shortDtFmt);
                            final ZonedDateTime zdt = ldt.atStartOfDay().atZone(ZoneId.of("UTC"));
                            tags.add(new CustomTagImpl(child.getName(), zdt));
                        } catch (final DateTimeParseException e) {
                            LOG.warn("Unable to parse date type on " + child.getName(), e);
                        }
                    } else if (type.equals("dateTime")) {
                        try {
                            final String dateStr = child.getTextTrim();
                            final ZonedDateTime zdt = LocalDateTime.parse(dateStr, longDtFmt).atZone(ZoneId.of("UTC"));
                            tags.add(new CustomTagImpl(child.getName(), zdt));
                        } catch (final DateTimeParseException e) {
                            try {
                                final String dateStr = child.getTextTrim();
                                final LocalDate ldt = LocalDate.parse(dateStr, shortDtFmt);
                                final ZonedDateTime zdt = ldt.atStartOfDay().atZone(ZoneId.of("UTC"));
                                tags.add(new CustomTagImpl(child.getName(), zdt));
                            } catch (final DateTimeParseException f) {
                                LOG.warn("Unable to parse date type on " + child.getName(), f);
                            }
                        }
                    } else if (type.equals("dateTimeRange")) {
                        try {
                            final ZonedDateTime start = LocalDateTime.parse(
                                child.getChild("start", CustomTagParser.NS).getText().trim(),
                                longDtFmt).atZone(ZoneId.of("UTC"));
                            final ZonedDateTime end = LocalDateTime.parse(
                                child.getChild("end", CustomTagParser.NS).getText().trim(), 
                                longDtFmt).atZone(ZoneId.of("UTC"));
                            tags.add(new CustomTagImpl(child.getName(), new DateTimeRange(start, end)));
                        } catch (final Exception e) {
                            LOG.warn("Unable to parse date type on " + child.getName(), e);
                        }
                    } else if (type.equals("url")) {
                        try {
                            tags.add(new CustomTagImpl(child.getName(), new URL(child.getTextTrim())));
                        } catch (final MalformedURLException e) {
                            LOG.warn("Unable to parse URL type on " + child.getName(), e);
                        }
                    } else if (type.equals("boolean")) {
                        tags.add(new CustomTagImpl(child.getName(), new Boolean(child.getTextTrim().toLowerCase())));
                    } else if (type.equals("location")) {
                        tags.add(new CustomTagImpl(child.getName(), new CustomTagImpl.Location(child.getText())));
                    } else {
                        throw new Exception("Unknown type: " + type);
                    }
                } catch (final Exception e) {
                    LOG.warn("Unable to parse type on " + child.getName(), e);
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
