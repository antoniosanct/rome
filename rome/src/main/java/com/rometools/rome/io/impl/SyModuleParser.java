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

import java.util.Locale;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.module.SyModule;
import com.rometools.rome.feed.module.SyModuleImpl;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

public class SyModuleParser extends ChildNavigator implements ModuleParser {

    @Override
    public String getNamespaceUri() {
        return SyModule.URI;
    }

    private Namespace getSynNamespace() {
        return BaseWireFeedParser.createNamespace(SyModule.URI);
    }

    @Override
    public Module parse(final Element syndRoot, final Locale locale) {

        boolean foundSomething = false;

        final SyModule sm = new SyModuleImpl();

        final Element updatePeriod = super.getChild(syndRoot, "updatePeriod", getSynNamespace());
        if (updatePeriod != null) {
            foundSomething = true;
            sm.setUpdatePeriod(updatePeriod.getTextContent().trim());
        }

        final Element updateFrequency = super.getChild(syndRoot, "updateFrequency", getSynNamespace());
        if (updateFrequency != null) {
            foundSomething = true;
            sm.setUpdateFrequency(Integer.parseInt(updateFrequency.getTextContent().trim()));
        }

        final Element updateBase = super.getChild(syndRoot, "updateBase", getSynNamespace());
        if (updateBase != null) {
            foundSomething = true;
            sm.setUpdateBase(DateParser.parseDate(updateBase.getTextContent(), locale));
        }

        if (foundSomething) {
            return sm;
        } else {
            return null;
        }

    }

}
