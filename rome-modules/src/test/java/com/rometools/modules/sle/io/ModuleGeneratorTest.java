/*
 * ModuleGeneratorTest.java
 * JUnit based test
 *
 * Created on April 29, 2006, 9:34 PM
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

package com.rometools.modules.sle.io;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.modules.AbstractTestCase;
import com.rometools.modules.sle.SimpleListExtension;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ModuleGeneratorTest extends AbstractTestCase {

	private static final Logger LOG = LoggerFactory.getLogger(ModuleGeneratorTest.class);
	
    public ModuleGeneratorTest(final String testName) {
        super(testName);
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite(ModuleGeneratorTest.class);

        return suite;
    }

    /**
     * Test of generate method, of class com.rometools.rome.feed.module.sle.io.ModuleGenerator.
     */
    public void testGenerate() throws Exception {
    	LOG.debug("testGenerate");

        final SyndFeedInput input = new SyndFeedInput();
        final SyndFeed feed = input.build(new XmlReader(new File(getTestFile("xml/test-rdf.xml"))));
        final SyndEntry entry = feed.getEntries().get(0);
        final SleModuleGenerator cm = (SleModuleGenerator) entry.getModule(SimpleListExtension.URI);
        final SyndFeedOutput output = new SyndFeedOutput();
        output.output(feed, new File("target/test-rdf-testGenerate.xml"));
        final SyndFeed feed2 = input.build(new File("target/test-rdf-testGenerate.xml"));
        final SyndEntry entry2 = feed2.getEntries().get(0);
        final SleModuleGenerator cm2 = (SleModuleGenerator) entry2.getModule(SimpleListExtension.URI);
        assertEquals(cm, cm2);
    }

}
