/*
 * Copyright 2005 Robert Cooper, Temple of the Screaming Penguin
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

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.rometools.modules.base.GoogleBase;
import com.rometools.modules.base.GoogleBaseImpl;
import com.rometools.modules.base.types.CurrencyEnumeration;
import com.rometools.modules.base.types.DateTimeRange;
import com.rometools.modules.base.types.FloatUnit;
import com.rometools.modules.base.types.GenderEnumeration;
import com.rometools.modules.base.types.IntUnit;
import com.rometools.modules.base.types.PaymentTypeEnumeration;
import com.rometools.modules.base.types.PriceTypeEnumeration;
import com.rometools.modules.base.types.ShippingType;
import com.rometools.modules.base.types.ShortDate;
import com.rometools.modules.base.types.Size;
import com.rometools.modules.base.types.YearType;
import com.rometools.rome.feed.impl.PropertyDescriptor;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleGenerator;

public class GoogleBaseGenerator implements ModuleGenerator {
    private static final Namespace NS = ChildNavigator.createNamespace("g-core", GoogleBase.URI);

    private static final Logger LOG = LoggerFactory.getLogger(GoogleBaseGenerator.class);

    public GoogleBaseGenerator() {
        super();
    }

    @Override
    public String getNamespaceUri() {
        return GoogleBase.URI;
    }

    @Override
    public Set<Namespace> getNamespaces() {
        final HashSet<Namespace> set = new HashSet<Namespace>();
        set.add(GoogleBaseGenerator.NS);
        return set;
    }

    @Override
    public void generate(final Module module, final Element element) {
        final GoogleBaseImpl mod = (GoogleBaseImpl) module;
        final HashMap<Object, Object> props2tags = new HashMap<Object, Object>(GoogleBaseParser.PROPS2TAGS);
        final List<PropertyDescriptor> pds = GoogleBaseParser.pds;

        for (final PropertyDescriptor pd : pds) {
            final String tagName = (String) props2tags.get(pd.getName());

            if (tagName == null) {
                continue;
            }

            Object[] values = null;

            try {
                if (pd.getPropertyType().isArray()) {
                    values = (Object[]) pd.getReadMethod().invoke(mod, (Object[]) null);
                } else {
                    values = new Object[] {pd.getReadMethod().invoke(mod, (Object[]) null)};
                }

                for (int j = 0; values != null && j < values.length; j++) {
                    if (values[j] != null) {
                        element.appendChild(generateTag(values[j], tagName, element));
                    }
                }
            } catch (final Exception e) {
                LOG.error("Error", e);
            }
        }
    }

    public Element generateTag(final Object o, final String tagName, final Element parent) {
    	final DateFormat SHORT_DT_FMT = new SimpleDateFormat("yyyy-MM-dd");
        final DateFormat LONG_DT_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if (o instanceof URL || o instanceof Float || o instanceof Boolean || o instanceof Integer || o instanceof String || o instanceof FloatUnit
                || o instanceof IntUnit || o instanceof GenderEnumeration || o instanceof PaymentTypeEnumeration || o instanceof PriceTypeEnumeration
                || o instanceof CurrencyEnumeration || o instanceof Size || o instanceof YearType) {
            return generateSimpleElement(tagName, o.toString(), parent);
        } else if (o instanceof ShortDate) {
            return generateSimpleElement(tagName, SHORT_DT_FMT.format(o), parent);
        } else if (o instanceof Date) {
            return generateSimpleElement(tagName, LONG_DT_FMT.format(o), parent);
        } else if (o instanceof ShippingType) {
            final ShippingType st = (ShippingType) o;
            final Element element = parent.getOwnerDocument().createElementNS(GoogleBaseGenerator.NS.getNamespaceURI(), tagName);

            element.setPrefix(GoogleBaseGenerator.NS.getPrefix());
            element.appendChild(generateSimpleElement("country", st.getCountry(), parent));
            element.appendChild(generateSimpleElement("service", st.getService().toString(), parent));
            element.appendChild(generateSimpleElement("price", st.getPrice().toString(), parent));

            return element;
        } else if (o instanceof DateTimeRange) {
            final DateTimeRange dtr = (DateTimeRange) o;
            final Element element = parent.getOwnerDocument().createElementNS(GoogleBaseGenerator.NS.getNamespaceURI(), tagName);
            
            element.setPrefix(GoogleBaseGenerator.NS.getPrefix());
            element.appendChild(generateSimpleElement("start", LONG_DT_FMT.format(dtr.getStart()), parent));
            element.appendChild(generateSimpleElement("end", LONG_DT_FMT.format(dtr.getEnd()), parent));

            return element;
        }

        throw new RuntimeException("Unknown class type to handle: " + o.getClass().getName());
    }

    protected Element generateSimpleElement(final String name, final String value, final Element parent) {
        final Element element = parent.getOwnerDocument().createElementNS(GoogleBaseGenerator.NS.getNamespaceURI(), name);
        element.setPrefix(GoogleBaseGenerator.NS.getPrefix());
        element.setTextContent(value);

        return element;
    }
}
