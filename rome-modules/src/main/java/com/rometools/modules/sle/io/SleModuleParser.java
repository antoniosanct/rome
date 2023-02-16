/*
 * ModuleParser.java
 *
 * Created on April 27, 2006, 10:37 PM
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.rometools.modules.sle.SimpleListExtension;
import com.rometools.modules.sle.SimpleListExtensionImpl;
import com.rometools.modules.sle.types.Group;
import com.rometools.modules.sle.types.Sort;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

public class SleModuleParser extends ChildNavigator implements ModuleParser {

    private static final Logger LOG = LoggerFactory.getLogger(SleModuleParser.class);

    static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace("cf", SimpleListExtension.URI);
    public static final Namespace TEMP = XMLEventFactory.newDefaultFactory().createNamespace("rome-sle", "urn:rome:sle");

    public SleModuleParser() {
        super();
    }

    /**
     * Returns the namespace URI this parser handles.

     *
     * @return the namespace URI.
     */
    @Override
    public String getNamespaceUri() {
        return SimpleListExtension.URI;
    }

    /**
     * Parses the XML node (JDOM element) extracting module information.

     *
     * @param element the XML node (JDOM element) to extract module information from.
     * @return a module instance, <b>null</b> if the element did not have module information.
     */
    @Override
    public Module parse(final Element element, final Locale locale) {
        if (super.getChild(element, "treatAs", NS) == null) {
            return null;
        }

        final SimpleListExtension sle = new SimpleListExtensionImpl();
        sle.setTreatAs(super.getChild(element, "treatAs", NS).getTextContent());

        final Element listInfo = super.getChild(element, "listinfo", NS);
        ArrayList<Object> values = new ArrayList<Object>();
        for (final Element ge : super.getChildren(listInfo, "group", NS)) {
        	final String nsUri = ge.getAttribute("ns") == null ? element.getNamespaceURI() : ge.getAttribute("ns");
            final Namespace ns = XMLEventFactory.newDefaultFactory().createNamespace(nsUri);
            final String elementName = ge.getAttribute("element");
            final String label = ge.getAttribute("label");
            values.add(new Group(ns, elementName, label));
        }

        sle.setGroupFields(values.toArray(new Group[values.size()]));
        values = values.size() == 0 ? values : new ArrayList<Object>();

        for (final Element se : super.getChildren(listInfo, "sort", NS)) {
            LOG.debug("Parse cf:sort {} {}", se.getAttribute("element"), se.getAttribute("data-type"));
            final String nsUri = se.getAttribute("ns") == null ? element.getNamespaceURI() : se.getAttribute("ns");
            final Namespace ns = XMLEventFactory.newDefaultFactory().createNamespace(nsUri);
            final String elementName = se.getAttribute("element");
            final String label = se.getAttribute("label");
            final String dataType = se.getAttribute("data-type");
            final boolean defaultOrder = se.getAttribute("default") == null ? false : Boolean.valueOf(se.getAttribute("default")).booleanValue();
            values.add(new Sort(ns, elementName, dataType, label, defaultOrder));
        }

        sle.setSortFields(values.toArray(new Sort[values.size()]));
        insertValues(sle, super.getChildren(element), element);

        return sle;
    }

    protected void addNotNullAttribute(final Element target, final String name, final Object value) {
        if (target == null || value == null) {
            return;
        } else {
            target.setAttribute(name, value.toString());
        }
    }

    public void insertValues(final SimpleListExtension sle, final List<Element> elements, final Element parent) {
        for (int i = 0; elements != null && i < elements.size(); i++) {
            final Element e = elements.get(i);
            final Group[] groups = sle.getGroupFields();

            for (final Group group2 : groups) {
                final Element value = super.getChild(e, group2.getElement(), group2.getNamespace());

                if (value == null) {
                    continue;
                }

                final Element group = parent.getOwnerDocument().createElementNS(TEMP.getNamespaceURI(), "group");
                addNotNullAttribute(group, "element", group2.getElement());
                addNotNullAttribute(group, "label", group2.getLabel());
                addNotNullAttribute(group, "value", value.getTextContent());
                addNotNullAttribute(group, "ns", group2.getNamespace().getNamespaceURI());

                e.appendChild(group);
            }

            final Sort[] sorts = sle.getSortFields();

            for (final Sort sort2 : sorts) {
                LOG.debug("Inserting for {} {}", sort2.getElement(), sort2.getDataType());
                final Element sort = parent.getOwnerDocument().createElementNS(TEMP.getNamespaceURI(), "sort");
                // this is the default sort order, so I am just going to ignore
                // the actual values and add a number type. It really shouldn't
                // work this way. I should be checking to see if any of the elements
                // defined have a value then use that value. This will preserve the
                // sort order, however, if anyone is using the SleEntry to display
                // the value of the field, it will not give the correct value.
                // This, however, would require knowledge in the item parser that I don't
                // have right now.
                if (sort2.getDefaultOrder()) {
                    sort.setAttribute("label", sort2.getLabel());
                    sort.setAttribute("value", Integer.toString(i));
                    sort.setAttribute("data-type", Sort.NUMBER_TYPE);
                    e.appendChild(sort);

                    continue;
                }

                final Element value = super.getChild(e, sort2.getElement(), sort2.getNamespace());
                if (value == null) {
                    LOG.debug("No value for {} : {}", sort2.getElement(), sort2.getNamespace());
                } else {
                    LOG.debug("{} value: {}", sort2.getElement(), value.getTextContent());
                }
                if (value == null) {
                    continue;
                }

                addNotNullAttribute(sort, "label", sort2.getLabel());
                addNotNullAttribute(sort, "element", sort2.getElement());
                addNotNullAttribute(sort, "value", value.getTextContent());
                addNotNullAttribute(sort, "data-type", sort2.getDataType());
                addNotNullAttribute(sort, "ns", sort2.getNamespace().getNamespaceURI());
                e.appendChild(sort);
                LOG.debug("Added {} {} = {}", sort, sort2.getLabel(), value.getTextContent());
            }
        }
    }
}
