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

package com.rometools.modules.slash.io;

import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.slash.Slash;
import com.rometools.modules.slash.SlashImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

/**
 * ModuleParser implementation for Slash RSS.
 */
public class SlashModuleParser extends ChildNavigator implements ModuleParser {

    private static final Namespace NS = ChildNavigator.createNamespace(Slash.URI);

    public SlashModuleParser() {
        super();
    }

    @Override
    public String getNamespaceUri() {
        return Slash.URI;
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        final SlashImpl si = new SlashImpl();
        Element tag = super.getChild(element, "hit_parade", SlashModuleParser.NS);
        if (tag != null) {
            final StringTokenizer tok = new StringTokenizer(tag.getTextContent(), ",");
            final Integer[] hp = new Integer[tok.countTokens()];
            for (int i = 0; tok.hasMoreTokens(); i++) {
                hp[i] = Integer.valueOf(tok.nextToken());
            }
            si.setHitParade(hp);
        }
        tag = null;
        tag = super.getChild(element, "comments", SlashModuleParser.NS);
        if (tag != null && !tag.getTextContent().trim().isEmpty()) {
            si.setComments(Integer.valueOf(tag.getTextContent().trim()));
        }
        tag = null;
        tag = super.getChild(element, "department", SlashModuleParser.NS);
        if (tag != null) {
            si.setDepartment(tag.getTextContent().trim());
        }
        tag = null;
        tag = super.getChild(element, "section", SlashModuleParser.NS);
        if (tag != null) {
            si.setSection(tag.getTextContent().trim());
        }
        if (si.getHitParade() != null || si.getComments() != null || si.getDepartment() != null || si.getSection() != null) {
            return si;
        }
        return null;
    }

}
