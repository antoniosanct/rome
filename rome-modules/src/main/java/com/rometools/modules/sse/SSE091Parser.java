/*
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

package com.rometools.modules.sse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.rometools.modules.sse.modules.Conflict;
import com.rometools.modules.sse.modules.Conflicts;
import com.rometools.modules.sse.modules.History;
import com.rometools.modules.sse.modules.Related;
import com.rometools.modules.sse.modules.SSEModule;
import com.rometools.modules.sse.modules.Sharing;
import com.rometools.modules.sse.modules.Sync;
import com.rometools.modules.sse.modules.Update;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.io.DelegatingModuleParser;
import com.rometools.rome.io.WireFeedParser;
import com.rometools.rome.io.impl.ChildNavigator;
import com.rometools.rome.io.impl.DateParser;
import com.rometools.rome.io.impl.RSS20Parser;

/**
 * Parses embedded SSE content from RSS channel and item content.
 */
public class SSE091Parser extends ChildNavigator implements DelegatingModuleParser {

    // root of the sharing element
    private RSS20Parser rssParser;

    public SSE091Parser() {
    }

    @Override
    public void setFeedParser(final WireFeedParser feedParser) {
        rssParser = (RSS20Parser) feedParser;
    }

    @Override
    public String getNamespaceUri() {
        return SSEModule.SSE_SCHEMA_URI;
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        SSEModule sseModule = null;
        final String name = element.getLocalName();

        if (name.equals("rss")) {
            sseModule = parseSharing(element, locale);
        } else if (name.equals("item")) {
            sseModule = parseSync(element, locale);
        }
        return sseModule;
    }

    private Sharing parseSharing(final Element element, final Locale locale) {
        final Element root = getRoot(element);

        Sharing sharing = null;
        final Element sharingChild = super.getChild(root, Sharing.NAME, SSEModule.SSE_NS);
        if (sharingChild != null) {
            sharing = new Sharing();
            sharing.setOrdered(parseBooleanAttr(sharingChild, Sharing.ORDERED_ATTRIBUTE));
            sharing.setSince(parseDateAttribute(sharingChild, Sharing.SINCE_ATTRIBUTE, locale));
            sharing.setUntil(parseDateAttribute(sharingChild, Sharing.UNTIL_ATTRIBUTE, locale));
            sharing.setWindow(parseIntegerAttribute(sharingChild, Sharing.WINDOW_ATTRIBUTE));
            sharing.setVersion(parseStringAttribute(sharingChild, Sharing.VERSION_ATTRIBUTE));
            parseRelated(root, sharing, locale);
        }

        return sharing;
    }

    private void parseRelated(final Element root, final Sharing sharing, final Locale locale) {
        Related related;
        final Element relatedChild = super.getChild(root, Related.NAME, SSEModule.SSE_NS);
        if (relatedChild != null) {
            related = new Related();
            // TODO; is this an attribute?
            related.setLink(parseStringAttribute(relatedChild, Related.LINK_ATTRIBUTE));
            related.setSince(parseDateAttribute(relatedChild, Related.SINCE_ATTRIBUTE, locale));
            related.setTitle(parseStringAttribute(relatedChild, Related.TITLE_ATTRIBUTE));
            related.setType(parseIntegerAttribute(relatedChild, Related.TYPE_ATTRIBUTE));
            related.setUntil(parseDateAttribute(relatedChild, Related.UNTIL_ATTRIBUTE, locale));
            sharing.setRelated(related);
        }
    }

    private Sync parseSync(final Element element, final Locale locale) {
        // Now I am going to get the item specific tags
        final Element syncChild = super.getChild(element, Sync.NAME, SSEModule.SSE_NS);
        Sync sync = null;

        if (syncChild != null) {
            sync = new Sync();
            sync.setId(parseStringAttribute(syncChild, Sync.ID_ATTRIBUTE));
            sync.setVersion(parseIntegerAttribute(syncChild, Sync.VERSION_ATTRIBUTE));
            sync.setDeleted(parseBooleanAttr(syncChild, Sync.DELETED_ATTRIBUTE));
            sync.setConflict(parseBooleanAttr(syncChild, Sync.CONFLICT_ATTRIBUTE));
            sync.setHistory(parseHistory(syncChild, locale));
            sync.setConflicts(parseConflicts(syncChild, locale));
        }
        return sync;
    }

    private List<Conflict> parseConflicts(final Element syncElement, final Locale locale) {
        List<Conflict> conflicts = null;

        final List<Element> conflictsContent = super.getChildren(syncElement, Conflicts.NAME);
        for (final Element conflictsElement : conflictsContent) {
            final List<Element> conflictContent = super.getChildren(conflictsElement, Conflict.NAME);
            for (final Element element : conflictContent) {
                final Element conflictElement = element;

                final Conflict conflict = new Conflict();
                conflict.setBy(parseStringAttribute(conflictElement, Conflict.BY_ATTRIBUTE));
                conflict.setWhen(parseDateAttribute(conflictElement, Conflict.WHEN_ATTRIBUTE, locale));
                conflict.setVersion(parseIntegerAttribute(conflictElement, Conflict.VERSION_ATTRIBUTE));

                final List<Element> conflictItemContent = super.getChildren(conflictElement, "item");
                for (final Element element2 : conflictItemContent) {
                    final Element conflictItemElement = element2;
                    final Element root = getRoot(conflictItemElement);
                    final Item conflictItem = rssParser.parseItem(root, conflictItemElement, locale);
                    conflict.setItem(conflictItem);

                    if (conflicts == null) {
                        conflicts = new ArrayList<Conflict>();
                    }
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    private Element getRoot(final Element start) {
        // reach up to grab the sharing element out of the root
        Element root = start;

        while (root.getParentNode() != null && root.getParentNode() instanceof Element) {
            root = (Element) root.getParentNode();
        }
        return root;
    }

    private History parseHistory(final Element historyElement, final Locale locale) {
        final Element historyContent = getFirstContent(historyElement, History.NAME);

        History history = null;
        if (historyContent != null) {
            history = new History();
            history.setBy(parseStringAttribute(historyContent, History.BY_ATTRIBUTE));
            history.setWhen(parseDateAttribute(historyContent, History.WHEN_ATTRIBUTE, locale));
            parseUpdates(historyContent, history, locale);
        }
        return history;
    }

    private Element getFirstContent(final Element element, final String name) {
        final List<Element> filterList = super.getChildren(element, name);
        Element firstContent = null;
        if (filterList != null && !filterList.isEmpty()) {
            firstContent = filterList.get(0);
        }
        return firstContent;
    }

    private void parseUpdates(final Element historyChild, final History history, final Locale locale) {
        final List<Element> updatedChildren = super.getChildren(historyChild, Update.NAME);
        for (final Element updateChild : updatedChildren) {
            final Update update = new Update();
            update.setBy(parseStringAttribute(updateChild, Update.BY_ATTRIBUTE));
            update.setWhen(parseDateAttribute(updateChild, Update.WHEN_ATTRIBUTE, locale));
            history.addUpdate(update);
        }
    }

    private String parseStringAttribute(final Element syncChild, final String attrName) {
        final Attr idAttribute = syncChild.getAttributeNode(attrName);
        return idAttribute != null ? idAttribute.getValue().trim() : null;
    }

    private Integer parseIntegerAttribute(final Element sharingChild, final String attrName) {
        final Attr integerAttribute = sharingChild.getAttributeNode(attrName);
        Integer integerAttr = null;
        if (integerAttribute != null) {
            integerAttr = Integer.valueOf(integerAttribute.getValue());
        }
        return integerAttr;
    }

    private Boolean parseBooleanAttr(final Element sharingChild, final String attrName) {
        final Attr attribute = sharingChild.getAttributeNode(attrName);
        Boolean attrValue = null;
        if (attribute != null) {
            attrValue = Boolean.valueOf(attribute.getTextContent());
        }
        return attrValue;
    }

    private Date parseDateAttribute(final Element childElement, final String attrName, final Locale locale) {
        final Attr dateAttribute = childElement.getAttributeNode(attrName);
        final Date date = null;
        if (dateAttribute != null) {
            // SSE spec requires the timezone to be 'GMT'
            // admittedly, this is a bit heavy-handed
            final String dateAttr = dateAttribute.getValue().trim();
            return DateParser.parseRFC822(dateAttr, locale);
        }
        return date;
    }

}
