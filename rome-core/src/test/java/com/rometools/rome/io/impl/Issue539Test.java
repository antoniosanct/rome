package com.rometools.rome.io.impl;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class Issue539Test {

	@Test
    public void parseDateTimeWithExtendedTimeZone() throws Exception {
		Assert.assertNotNull(
                DateParser.parseDate("Sat, 19 Mar 2022 22:45:00 +0000", Locale.US)
        );
		Assert.assertNotNull(
                DateParser.parseDate("Tue, 01 Mar 2022 16:03:00 +0000", Locale.US)
        );
		Assert.assertNotNull(
                DateParser.parseDate("Tue, 01 Mar 2022 16:03:00 GMT+00:00", Locale.US)
        );
    }
}
