package com.rometools.rome.io;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class XmLReaderHttpHeaderTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Before
    public void init() {
        stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                .withBody("<?xml version=\"1.0\" encoding=\"utf-8\"?><rss version=\"2.0\"><channel/></rss>")));
    }

    @Test
    public void testUrlWithoutHeaders() throws IOException {
        final URL url = new URL("http://localhost:" + wireMockRule.port() + "/test");
        final XmlReader xmlReader = new XmlReader(url.openStream());
        xmlReader.close();
        verify(getRequestedFor(urlEqualTo("/test")));
    }

}