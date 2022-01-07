package com.rometools.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;

import org.junit.Test;

/**
 * Test case for URIs
 *
 */
public class URIsTest {

	/**
	 * TestParse for URIs.
	 * @throws URISyntaxException any URI syntax exception.
	 */
    @Test
    public void testParse() throws URISyntaxException {
        assertThat(URIs.parse("https://example.com/test 123.png").toString()).isEqualTo("https://example.com/test%20123.png");
    }

}
