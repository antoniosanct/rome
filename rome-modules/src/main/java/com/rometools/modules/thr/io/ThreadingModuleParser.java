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
package com.rometools.modules.thr.io;

import java.util.Locale;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.thr.ThreadingModule;
import com.rometools.modules.thr.ThreadingModuleImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleParser;

/**
 * Currently no support for thr:count, thr:updated, thr:total link attributes.
 */
public class ThreadingModuleParser extends ChildNavigator implements ModuleParser {

    private static final Namespace NS = ChildNavigator.createNamespace(ThreadingModule.URI);

    @Override
    public String getNamespaceUri() {
        return ThreadingModule.URI;
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        final ThreadingModule tm = new ThreadingModuleImpl();
        Element inReplyTo = super.getChild(element, "in-reply-to", ThreadingModuleParser.NS);

        if (inReplyTo != null) {
        	String value = null != inReplyTo.getAttribute("href") && !"".equals(inReplyTo.getAttribute("href")) ? inReplyTo.getAttribute("href") : null;
            tm.setHref(value);
            value = null != inReplyTo.getAttribute("ref") && !"".equals(inReplyTo.getAttribute("ref")) ? inReplyTo.getAttribute("ref") : null;
            tm.setRef(value);
            value = null != inReplyTo.getAttribute("source") && !"".equals(inReplyTo.getAttribute("source")) ? inReplyTo.getAttribute("source") : null;
            tm.setSource(value);
            value = null != inReplyTo.getAttribute("type") && !"".equals(inReplyTo.getAttribute("type")) ? inReplyTo.getAttribute("type") : null;
            tm.setType(value);
            return tm;
        }

        return null;
    }
}
