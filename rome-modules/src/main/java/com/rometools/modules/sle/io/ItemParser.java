/*
 * ItemParser.java
 *
 * Created on April 29, 2006, 4:27 PM
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
package com.rometools.modules.sle.io;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;

import org.w3c.dom.Element;

import com.rometools.modules.sle.SleEntryImpl;
import com.rometools.modules.sle.types.DateValue;
import com.rometools.modules.sle.types.EntryValue;
import com.rometools.modules.sle.types.NumberValue;
import com.rometools.modules.sle.types.Sort;
import com.rometools.modules.sle.types.StringValue;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.impl.DateParser;

public class ItemParser extends ChildNavigator implements com.rometools.rome.io.ModuleParser {

    public ItemParser() {
        super();
    }

    /**
     * Returns the namespace URI this parser handles.

     *
     * @return the namespace URI.
     */
    @Override
    public String getNamespaceUri() {
        return SleModuleParser.TEMP.getNamespaceURI();
    }

    /**
     * Parses the XML node (JDOM element) extracting module information.

     *
     * @param element the XML node (JDOM element) to extract module information from.
     * @return a module instance, <b>null</b> if the element did not have module information.
     */
    @Override
    public Module parse(final Element element, final Locale locale) {
        final SleEntryImpl sle = new SleEntryImpl();
        ArrayList<EntryValue> values = new ArrayList<EntryValue>();
        final List<Element> groups = super.getChildren(element, "group", SleModuleParser.TEMP);

        for (final Element group : groups) {
            final StringValue value = new StringValue();
            value.setElement(group.getAttribute("element"));
            value.setLabel(group.getAttribute("label"));
            value.setValue(group.getAttribute("value"));
            if (group.getAttribute("ns") != null) {
                value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(group.getAttribute("ns")));
            } else {
                value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(
                		element.getOwnerDocument().getDocumentElement().getFirstChild().getNamespaceURI()));
            }
            values.add(value);
            element.removeChild(group);
        }

        sle.setGroupValues(values.toArray(new EntryValue[values.size()]));
        values = values.size() == 0 ? values : new ArrayList<EntryValue>();

        final List<Element> sorts = new ArrayList<Element>(super.getChildren(element, "sort", SleModuleParser.TEMP));

        for (final Element sort : sorts) {
            final String dataType = sort.getAttribute("data-type");
            if (dataType == null || dataType.equals(Sort.TEXT_TYPE)) {
                final StringValue value = new StringValue();
                value.setElement(sort.getAttribute("element"));
                value.setLabel(sort.getAttribute("label"));
                value.setValue(sort.getAttribute("value"));
                if (sort.getAttribute("ns") != null) {
                    value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(sort.getAttribute("ns")));
                } else {
                	value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(
                    		element.getOwnerDocument().getDocumentElement().getFirstChild().getNamespaceURI()));
                }
                values.add(value);

                element.removeChild(sort);

            } else if (dataType.equals(Sort.DATE_TYPE)) {
                final DateValue value = new DateValue();
                value.setElement(sort.getAttribute("element"));
                value.setLabel(sort.getAttribute("label"));
                if (sort.getAttribute("ns") != null) {
                    value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(sort.getAttribute("ns")));
                } else {
                	value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(
                    		element.getOwnerDocument().getDocumentElement().getFirstChild().getNamespaceURI()));
                }
                Date dateValue = null;

                try {
                    dateValue = DateParser.parseRFC822(sort.getAttribute("value"), locale);
                    if (dateValue == null) {
                        dateValue = DateParser.parseW3CDateTime(sort.getAttribute("value"), locale);
                    }
                } catch (final Exception e) {
                    ; // ignore parse exceptions
                }

                value.setValue(dateValue);
                values.add(value);
                element.removeChild(sort);
            } else if (dataType.equals(Sort.NUMBER_TYPE)) {
                final NumberValue value = new NumberValue();
                value.setElement(sort.getAttribute("element"));
                value.setLabel(sort.getAttribute("label"));
                if (sort.getAttribute("ns") != null) {
                    value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(sort.getAttribute("ns")));
                } else {
                	value.setNamespace(XMLEventFactory.newDefaultFactory().createNamespace(
                    		element.getOwnerDocument().getDocumentElement().getFirstChild().getNamespaceURI()));
                }

                try {
                    value.setValue(new BigDecimal(sort.getAttribute("value")));
                } catch (final NumberFormatException nfe) {
                    ; // ignore
                    values.add(value);
                    element.removeChild(sort);
                }
            } else {
                throw new RuntimeException("Unknown datatype");
            }
        }

        sle.setSortValues(values.toArray(new EntryValue[values.size()]));

        return sle;
    }
}
