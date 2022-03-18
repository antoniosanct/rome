/*
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
package com.rometools.rome.io.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.rometools.rome.feed.WireFeedForeignMarkup;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.WireFeedGenerator;

public abstract class BaseWireFeedGenerator extends ChildNavigator implements WireFeedGenerator {

    /**
     * [TYPE].feed.ModuleParser.classes= [className] ...
     */
    private static final String FEED_MODULE_GENERATORS_POSFIX_KEY = ".feed.ModuleGenerator.classes";

    /**
     * [TYPE].item.ModuleParser.classes= [className] ...
     */
    private static final String ITEM_MODULE_GENERATORS_POSFIX_KEY = ".item.ModuleGenerator.classes";

    /**
     * [TYPE].person.ModuleParser.classes= [className] ...
     */
    private static final String PERSON_MODULE_GENERATORS_POSFIX_KEY = ".person.ModuleGenerator.classes";

    private final String type;
    private final ModuleGenerators feedModuleGenerators;
    private final ModuleGenerators itemModuleGenerators;
    private final ModuleGenerators personModuleGenerators;
    private final Namespace[] allModuleNamespaces;

    protected BaseWireFeedGenerator(final String type) {

        this.type = type;

        feedModuleGenerators = new ModuleGenerators(type + FEED_MODULE_GENERATORS_POSFIX_KEY, this);
        itemModuleGenerators = new ModuleGenerators(type + ITEM_MODULE_GENERATORS_POSFIX_KEY, this);
        personModuleGenerators = new ModuleGenerators(type + PERSON_MODULE_GENERATORS_POSFIX_KEY, this);

        final Set<Namespace> allModuleNamespaces = new HashSet<Namespace>();

        for (final Namespace namespace : feedModuleGenerators.getAllNamespaces()) {
            allModuleNamespaces.add(namespace);
        }

        for (final Namespace namespace : itemModuleGenerators.getAllNamespaces()) {
            allModuleNamespaces.add(namespace);
        }

        for (final Namespace namespace : personModuleGenerators.getAllNamespaces()) {
            allModuleNamespaces.add(namespace);
        }

        this.allModuleNamespaces = new Namespace[allModuleNamespaces.size()];

        allModuleNamespaces.toArray(this.allModuleNamespaces);

    }

    @Override
    public String getType() {
        return type;
    }

    protected void generateModuleNamespaceDefs(final Element root) {
        for (final Namespace allModuleNamespace : allModuleNamespaces) {
        	root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
        			String.join(":", XMLConstants.XMLNS_ATTRIBUTE, allModuleNamespace.getPrefix()),
        			allModuleNamespace.getNamespaceURI());
        }
    }

    protected void generateFeedModules(final List<Module> modules, final Element feed) throws DOMException {
        feedModuleGenerators.generateModules(modules, feed);
    }

    public void generateItemModules(final List<Module> modules, final Element item) throws DOMException {
        itemModuleGenerators.generateModules(modules, item);
    }

    public void generatePersonModules(final List<Module> modules, final Element person) throws DOMException {
        personModuleGenerators.generateModules(modules, person);
    }

    protected void generateForeignMarkup(final Element element, final List<WireFeedForeignMarkup> foreignElements, final Element parent) {
        if (foreignElements != null) {
            for (final WireFeedForeignMarkup foreignElement : foreignElements) {
                element.appendChild(element.getOwnerDocument().importNode(foreignElement.getElement(), true));
            }
        }
    }

    /**
     * Purging unused declarations is less optimal, performance-wise, than never adding them in the
     * first place. So, we should still ask the ROME guys to fix their code (not adding dozens of
     * unnecessary module declarations). Having said that: purging them here, before XML generation,
     * is more efficient than parsing and re-molding the XML after ROME generates it.
     * 
     * Note that the calling app could still add declarations/modules to the Feed tree after this.
     * Which is fine. But those modules are then responsible for crawling to the root of the tree,
     * at generate() time, to make sure their namespace declarations are present.
     * 
     * @param root the root element.
     */
    protected void purgeUnusedNamespaceDeclarations(final Element root) {

        final Set<String> usedPrefixes = new HashSet<String>();
        collectUsedPrefixes(root, usedPrefixes);

        final NamedNodeMap list = root.getAttributes();
        final List<Namespace> additionalNamespaces = new ArrayList<>(1);
        for (int i = 0; i < list.getLength(); i++) {
        	// The duplication will prevent a ConcurrentModificationException
        	// below
        	Attr a = (Attr) list.item(i);
        	if (a.getValue().indexOf("http") >= 0) {
        		final String name = null == a.getLocalName() ? a.getName() : a.getLocalName();
        		Namespace n = XMLEventFactory.newDefaultFactory()
        			.createNamespace(name, a.getValue());
        		additionalNamespaces.add(n);
        	}
        }

        for (final Namespace ns : additionalNamespaces) {
            final String prefix = ns.getPrefix();
            if (prefix != null && prefix.length() > 0 && !usedPrefixes.contains(prefix)) {
                root.removeAttribute(String.join(":", XMLConstants.XMLNS_ATTRIBUTE, prefix));
            }
        }

    }
    
    private void collectUsedPrefixes(final Element el, final Set<String> collector) {

        final String prefix = el.getPrefix();
        if (prefix != null && prefix.length() > 0 && !collector.contains(prefix)) {
            collector.add(prefix);
        }

        final List<Element> kids = super.getChildren(el);
        for (Element k : kids) {
            // recursion- worth it
    		collectUsedPrefixes(k, collector);
        }

    }

}
