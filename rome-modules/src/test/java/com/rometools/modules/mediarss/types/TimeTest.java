/*
 * TimeTest.java
 * JUnit based test
 *
 * Created on April 18, 2006, 10:01 PM
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

package com.rometools.modules.mediarss.types;

import org.junit.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TimeTest extends TestCase {

    public TimeTest(final String testName) {
        super(testName);
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite(TimeTest.class);

        return suite;
    }

    /**
     * Test of toString method, of class com.rometools.rome.feed.module.mediarss.types.Time.
     */
    public void testToString() {
    	final String[][] matrix = {
    			{"12:05:35.3","12:05:35.3"},
//    			{"3:54.00001","3:54.00001"},
//    			{"00:00:28","00:00:28.0"},
//    			{"00:00:14","00:00:14.0"},
//    			{"00:00:42","00:00:42.0"},
//    			{"00:03:00.500","00:03:0.5"},
//    			{"00:01:30.250","00:01:30.25"},
//    			{"00:04:30.750","00:04:30.75"},
//    			{"00:05:24",""},
//    			{"00:02:42",""},
//    			{"00:08:06",""},
//    			{"00:02:12",""},
//    			{"00:01:06",""},
//    			{"00:03:18",""},
//    			{"00:01:42",""},
//    			{"00:00:51",""},
//    			{"00:02:33",""},
//    			{"00:01:03.500",""},
//    			{"00:00:31.750",""},
//    			{"00:01:35.250",""},
//    			{"00:00:08.500",""},
//    			{"00:00:04.250",""},
//    			{"00:00:12.750",""},
//    			{"00:01:50",""},
//    			{"00:00:55",""},
//    			{"00:02:45",""},
//    			{"00:02:29",""},
//    			{"00:01:14.500",""},
//    			{"00:03:43.500",""},
//    			{"00:01","00:01:0.0"},
//    			{"01:00","01:00:0.0"},
    			{"00:15","00:00:15.0"},
    			{"00:45","00:00:45.0"},
    			};

    	for (int i = 0; i < matrix.length; i++) {
    		final Time t = new Time(matrix[i][0]);
    		Assert.assertEquals(matrix[i][1], t.toString());
    		final Time t2 = new Time(t.toString());
    		Assert.assertEquals(t.toString(), t2.toString());
    	}
    }

}
