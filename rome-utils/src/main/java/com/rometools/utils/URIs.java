package com.rometools.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.experimental.UtilityClass;

/**
 * A URI util class
 *
 */
@UtilityClass
public class URIs {

    private static final Map<String, String> FIXES = new HashMap<>();

    static {
        FIXES.put(" ", "%20"); // fix spaces
    }

    /**
     * Parses a String to a URI object.
     * @param str a URI in string format.
     * @return a new URI object, fixed by spaces.
     * @throws URISyntaxException any URI syntax exception.
     */
    public URI parse(final String str) throws URISyntaxException {

        String fixed = str;
        for (final Entry<String, String> entry : FIXES.entrySet()) {
            final String search = entry.getKey();
            final String replace = entry.getValue();
            fixed = fixed.replace(search, replace);
        }

        return new URI(fixed);

    }

}
