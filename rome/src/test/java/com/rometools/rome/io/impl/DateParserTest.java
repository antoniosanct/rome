package com.rometools.rome.io.impl;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.junit.Test;

public class DateParserTest {

    @Test
    public void parseW3CDateTimeIsOk() throws Exception {
        assertEquals(
                ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneId.of("UTC")),
                DateParser.parseW3CDateTime("1970-01-01T00:00:01+00:00", Locale.GERMANY)
        );
    }

    @Test
    public void parseW3CDateTimeWithTrailingWhitespaceIsOk() throws Exception {
        assertEquals(
                ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneId.of("UTC")),
                DateParser.parseW3CDateTime("1970-01-01T00:00:01+00:00   ", Locale.GERMANY)
        );
    }
    
    @Test
    public void parseRFC822DateTimeWithTimeZoneIsOk() throws Exception {
    	// Sat, 28 Mar 2020 13:42:38 IST  	
        final ZonedDateTime z1 = ZonedDateTime.of(2020, 3, 28, 13, 42, 38, 0, ZoneId.of("UTC+04:30"));
        final ZonedDateTime z2 = DateParser.parseRFC822("Sa., 28 März 20 09:12:38 MEZ", Locale.GERMANY);
        assertEquals(z1.toInstant(), z2.toInstant());
    }

}
