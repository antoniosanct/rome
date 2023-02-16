/*
 * Copyright 2019 Maximilian Irro
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
package com.rometools.modules.fyyd.io;

import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.fyyd.modules.FyydModule;
import com.rometools.modules.fyyd.modules.FyydModuleImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

/**
 * The ModuleParser implementation for the Fyyd module.
 */
public class FyydParser extends ChildNavigator implements ModuleParser {

    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace(FyydModule.URI);

    @Override
    public String getNamespaceUri() {
        return FyydModule.URI;
    }

    @Override
    public Module parse(Element element, Locale locale) {
        if (element.getLocalName().equals("channel") || element.getLocalName().equals("feed")) {
            final Element verify = super.getChild(element, FyydElement.VERIFY, NS);
            if (verify != null && verify.getTextContent() != null) {
                final FyydModule fyyd = new FyydModuleImpl();
                fyyd.setVerify(verify.getTextContent().trim());
                return fyyd;
            }
        }

        return null;
    }

}
