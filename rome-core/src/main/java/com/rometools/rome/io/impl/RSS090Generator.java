/*
 * Copyright 2004 Sun Microsystems, Inc.
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

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Image;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.rss.TextInput;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.ModuleGenerator;

/**
 * Feed Generator for RSS 0.90
 * 
 */
public class RSS090Generator extends BaseWireFeedGenerator {

    private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RSS_URI = "http://my.netscape.com/rdf/simple/0.9/";
    private static final String CONTENT_URI = "http://purl.org/rss/1.0/modules/content/";

    private static final Namespace RDF_NS = BaseWireFeedParser.createNamespace("rdf", RDF_URI);
    private static final Namespace RSS_NS = BaseWireFeedParser.createNamespace(RSS_URI);
    private static final Namespace CONTENT_NS = BaseWireFeedParser.createNamespace("content", CONTENT_URI);

    public RSS090Generator() {
        this("rss_0.9");
    }

    protected RSS090Generator(final String type) {
        super(type);
    }

    @Override
    public Document generate(final WireFeed feed) throws FeedException {
    	try {
    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
    		dbf.setNamespaceAware(true);
//    		dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
//        	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//        	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    		final Document doc = dbf.newDocumentBuilder().newDocument();
			final Channel channel = (Channel) feed;
	        final Element root = createRootElement(channel, doc);
	        populateFeed(channel, root);
	        purgeUnusedNamespaceDeclarations(root);
	        doc.appendChild(root);
	        return doc;
		} catch (ParserConfigurationException e) {
			throw new FeedException("Document builder failed", e);
		}
    }

    protected Namespace getFeedNamespace() {
        return RSS_NS;
    }

    protected Namespace getRDFNamespace() {
        return RDF_NS;
    }

    protected Namespace getContentNamespace() {
        return CONTENT_NS;
    }

    protected Element createRootElement(final Channel channel, final Document doc) {
        final Element root = doc.createElementNS(getRDFNamespace().getNamespaceURI(), "RDF");
        root.setPrefix(getRDFNamespace().getPrefix());
//        root.addNamespaceDeclaration(getFeedNamespace());
        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns", getFeedNamespace().getNamespaceURI());
//        root.addNamespaceDeclaration(getRDFNamespace());
        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + getRDFNamespace().getPrefix(), getRDFNamespace().getNamespaceURI());
//        root.addNamespaceDeclaration(getContentNamespace());
        root.setAttributeNS(ModuleGenerator.XMLNS_URI, "xmlns:" + getContentNamespace().getPrefix(), getContentNamespace().getNamespaceURI());
        generateModuleNamespaceDefs(root);
        return root;
    }

    protected void populateFeed(final Channel channel, final Element parent) throws FeedException {
        addChannel(channel, parent);
        addImage(channel, parent);
        addTextInput(channel, parent);
        addItems(channel, parent);
        generateForeignMarkup(parent, channel.getForeignMarkup(), null);
    }

    protected void addChannel(final Channel channel, final Element parent) throws FeedException {
        final Element eChannel = parent.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "channel");
        eChannel.setPrefix(getFeedNamespace().getPrefix());
        populateChannel(channel, eChannel);
        checkChannelConstraints(eChannel);
        parent.appendChild(eChannel);
        generateFeedModules(channel.getModules(), eChannel);
    }

    /**
     * Populates the given channel with parsed data from the ROME element that holds the channel
     * data.
     *
     * @param channel the channel into which parsed data will be added.
     * @param eChannel the XML element that holds the data for the channel.
     */
    protected void populateChannel(final Channel channel, final Element eChannel) {
        final String title = channel.getTitle();
        if (title != null) {
            eChannel.appendChild(generateSimpleElement("title", title, eChannel));
        }
        final String link = channel.getLink();
        if (link != null) {
            eChannel.appendChild(generateSimpleElement("link", link, eChannel));
        }
        final String description = channel.getDescription();
        if (description != null) {
            eChannel.appendChild(generateSimpleElement("description", description, eChannel));
        }
    }

    // maxLen == -1 means unlimited.
    protected void checkNotNullAndLength(final Element element, final String childName, final int minLen, final int maxLen) throws FeedException {
        final Element child = super.getChild(element, childName);
        if (child == null) {
            throw new FeedException("Invalid " + getType() + " feed, missing " + element.getNodeName() + " " + childName);
        }
        checkLength(element, childName, minLen, maxLen);
    }

    // maxLen == -1 means unlimited.
    protected void checkLength(final Element parent, final String childName, final int minLen, final int maxLen) throws FeedException {
    	final Element child = super.getChild(parent, childName);
        if (child != null) {
            if (minLen > 0 && child.getTextContent().length() < minLen) {
                throw new FeedException("Invalid " + getType() + " feed, " + parent.getNodeName() + " " + childName + "short of " + minLen + " length");
            }
            if (maxLen > -1 && child.getTextContent().length() > maxLen) {
                throw new FeedException("Invalid " + getType() + " feed, " + parent.getNodeName() + " " + childName + "exceeds " + maxLen + " length");
            }
        }
    }

    protected void addImage(final Channel channel, final Element parent) throws FeedException {
        final Image image = channel.getImage();
        if (image != null) {
            final Element eImage = parent.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "image");
            eImage.setPrefix(getFeedNamespace().getPrefix());
            populateImage(image, eImage);
            checkImageConstraints(eImage);
            parent.appendChild(eImage);
        }
    }

    protected void populateImage(final Image image, final Element eImage) {
        final String title = image.getTitle();
        if (title != null) {
            eImage.appendChild(generateSimpleElement("title", title, eImage));
        }
        final String url = image.getUrl();
        if (url != null) {
            eImage.appendChild(generateSimpleElement("url", url, eImage));
        }
        final String link = image.getLink();
        if (link != null) {
            eImage.appendChild(generateSimpleElement("link", link, eImage));
        }
    }

    // Thxs DW for this one
    protected String getTextInputLabel() {
        return "textInput";
    }

    protected void addTextInput(final Channel channel, final Element parent) throws FeedException {
        final TextInput textInput = channel.getTextInput();
        if (textInput != null) {
            final Element eTextInput = parent.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), getTextInputLabel());
            eTextInput.setPrefix(getFeedNamespace().getPrefix());
            populateTextInput(textInput, eTextInput);
            checkTextInputConstraints(eTextInput);
            parent.appendChild(eTextInput);
        }
    }

    protected void populateTextInput(final TextInput textInput, final Element eTextInput) {
        final String title = textInput.getTitle();
        if (title != null) {
            eTextInput.appendChild(generateSimpleElement("title", title, eTextInput));
        }
        final String description = textInput.getDescription();
        if (description != null) {
            eTextInput.appendChild(generateSimpleElement("description", description, eTextInput));
        }
        final String name = textInput.getName();
        if (name != null) {
            eTextInput.appendChild(generateSimpleElement("name", name, eTextInput));
        }
        final String link = textInput.getLink();
        if (link != null) {
            eTextInput.appendChild(generateSimpleElement("link", link, eTextInput));
        }
    }

    protected void addItems(final Channel channel, final Element parent) throws FeedException {
        final List<Item> items = channel.getItems();
        for (int i = 0; i < items.size(); i++) {
            addItem(items.get(i), parent, i);
        }
        checkItemsConstraints(parent);
    }

    protected void addItem(final Item item, final Element parent, final int index) throws FeedException {
        final Element eItem = parent.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), "item");
        eItem.setPrefix(getFeedNamespace().getPrefix());
        populateItem(item, eItem, index, parent);
        checkItemConstraints(eItem);
        generateItemModules(item.getModules(), eItem);
        parent.appendChild(eItem);
    }

    protected void populateItem(final Item item, final Element eItem, final int index, final Element parent) {
        final String title = item.getTitle();
        if (title != null) {
            eItem.appendChild(generateSimpleElement("title", title, eItem));
        }
        final String link = item.getLink();
        if (link != null) {
            eItem.appendChild(generateSimpleElement("link", link, eItem));
        }
        generateForeignMarkup(eItem, item.getForeignMarkup(), parent);
    }

    protected Element generateSimpleElement(final String name, final String value, final Element e) {
        final Element element = e.getOwnerDocument().createElementNS(getFeedNamespace().getNamespaceURI(), name);
        element.setPrefix(getFeedNamespace().getPrefix());
        element.setTextContent(value);
        return element;
    }

    protected void checkChannelConstraints(final Element eChannel) throws FeedException {
        checkNotNullAndLength(eChannel, "title", 0, 40);
        checkNotNullAndLength(eChannel, "description", 0, 500);
        checkNotNullAndLength(eChannel, "link", 0, 500);
    }

    protected void checkImageConstraints(final Element eImage) throws FeedException {
        checkNotNullAndLength(eImage, "title", 0, 40);
        checkNotNullAndLength(eImage, "url", 0, 500);
        checkNotNullAndLength(eImage, "link", 0, 500);
    }

    protected void checkTextInputConstraints(final Element eTextInput) throws FeedException {
        checkNotNullAndLength(eTextInput, "title", 0, 40);
        checkNotNullAndLength(eTextInput, "description", 0, 100);
        checkNotNullAndLength(eTextInput, "name", 0, 500);
        checkNotNullAndLength(eTextInput, "link", 0, 500);
    }

    protected void checkItemsConstraints(final Element parent) throws FeedException {
    	final List<Element> nodeItems = super.getChildren(parent, "item", getFeedNamespace());
    	if (null == nodeItems || nodeItems.size() < 1 || nodeItems.size() > 15) {
    		final int count = (null == nodeItems ? 0 : nodeItems.size());
            throw new FeedException("Invalid " + getType() + " feed, item count is " + count + " it must be between 1 an 15");
        }
    }

    protected void checkItemConstraints(final Element eItem) throws FeedException {
        checkNotNullAndLength(eItem, "title", 0, 100);
        checkNotNullAndLength(eItem, "link", 0, 500);
    }

}
