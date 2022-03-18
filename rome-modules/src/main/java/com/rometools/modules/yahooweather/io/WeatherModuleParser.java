/*
 * Copyright 2008 Robert Cooper, Temple of the Screaming Penguin
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
package com.rometools.modules.yahooweather.io;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.rometools.modules.yahooweather.YWeatherModule;
import com.rometools.modules.yahooweather.YWeatherModuleImpl;
import com.rometools.modules.yahooweather.types.Astronomy;
import com.rometools.modules.yahooweather.types.Atmosphere;
import com.rometools.modules.yahooweather.types.Condition;
import com.rometools.modules.yahooweather.types.ConditionCode;
import com.rometools.modules.yahooweather.types.Forecast;
import com.rometools.modules.yahooweather.types.Location;
import com.rometools.modules.yahooweather.types.Units;
import com.rometools.modules.yahooweather.types.Wind;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
import com.rometools.rome.io.impl.ChildNavigator;

/**
 * ModuleParser implementation for Slash RSS.
 */
public class WeatherModuleParser extends ChildNavigator implements ModuleParser {
    private static final Logger LOG = LoggerFactory.getLogger(WeatherModuleParser.class);
    private static final Namespace NS = XMLEventFactory.newDefaultFactory().createNamespace(YWeatherModule.URI);

    @Override
    public String getNamespaceUri() {
        return YWeatherModule.URI;
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        final YWeatherModuleImpl module = new YWeatherModuleImpl();
        final Element location = super.getChild(element, "location", WeatherModuleParser.NS);

        if (location != null) {
            final Location l = new Location(location.getAttribute("city"), location.getAttribute("region"), location.getAttribute("country"));
            module.setLocation(l);
        }

        final Element units = super.getChild(element, "units", WeatherModuleParser.NS);

        if (units != null) {
            final Units u = new Units(units.getAttribute("temperature"), units.getAttribute("distance"), units.getAttribute("pressure"),
                    units.getAttribute("speed"));
            module.setUnits(u);
        }

        final Element wind = super.getChild(element, "wind", WeatherModuleParser.NS);

        if (wind != null) {
            try {
                final Wind w = new Wind(Integer.parseInt(wind.getAttribute("chill")), Integer.parseInt(wind.getAttribute("direction")),
                        Integer.parseInt(wind.getAttribute("speed")));
                module.setWind(w);
            } catch (final NumberFormatException nfe) {
                LOG.warn("NumberFormatException processing <wind> tag.", nfe);
            }
        }

        final Element atmosphere = super.getChild(element, "atmosphere", WeatherModuleParser.NS);

        if (atmosphere != null) {
            try {
                final Atmosphere a = new Atmosphere(Integer.parseInt(atmosphere.getAttribute("humidity")), Double.parseDouble(atmosphere
                        .getAttribute("visibility")) / 100, Double.parseDouble(atmosphere.getAttribute("pressure")),
                        Atmosphere.PressureChange.fromCode(Integer.parseInt(atmosphere.getAttribute("rising"))));
                module.setAtmosphere(a);
            } catch (final NumberFormatException nfe) {
                LOG.warn("NumberFormatException processing <atmosphere> tag.", nfe);
            }
        }

        final Element astronomy = super.getChild(element, "astronomy", WeatherModuleParser.NS);

        if (astronomy != null) {
            try {
                final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", locale);
                final Astronomy a = new Astronomy(timeFormat.parse(astronomy.getAttribute("sunrise").replaceAll("am", "AM").replaceAll("pm", "PM")),
                        timeFormat.parse(astronomy.getAttribute("sunset").replaceAll("am", "AM").replaceAll("pm", "PM")));
                module.setAstronomy(a);
            } catch (final ParseException pe) {
                LOG.warn("ParseException processing <astronomy> tag.", pe);
            }
        }

        final Element condition = super.getChild(element, "condition", WeatherModuleParser.NS);

        if (condition != null) {
            try {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy h:mm a zzz", locale);
                final Condition c = new Condition(condition.getAttribute("text"), ConditionCode.fromCode(Integer.parseInt(condition
                        .getAttribute("code"))), Integer.parseInt(condition.getAttribute("temp")), dateFormat.parse(condition
                        .getAttribute("date").replaceAll("pm", "PM").replaceAll("am", "AM")));
                module.setCondition(c);
            } catch (final NumberFormatException nfe) {
                LOG.warn("NumberFormatException processing <condition> tag.", nfe);
            } catch (final ParseException pe) {
                LOG.warn("ParseException processing <condition> tag.", pe);
            }
        }

        final List<Element> forecasts = super.getChildren(element, "forecast", WeatherModuleParser.NS);

        if (forecasts != null) {
            final Forecast[] f = new Forecast[forecasts.size()];
            int i = 0;

            final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", locale);
            for (final Iterator<Element> it = forecasts.iterator(); it.hasNext(); i++) {
                final Element forecast = it.next();

                try {
                    f[i] = new Forecast(forecast.getAttribute("day"), dateFormat.parse(forecast.getAttribute("date")), Integer.parseInt(forecast
                            .getAttribute("low")), Integer.parseInt(forecast.getAttribute("high")), forecast.getAttribute("text"),
                            ConditionCode.fromCode(Integer.parseInt(forecast.getAttribute("code"))));
                } catch (final NumberFormatException nfe) {
                    LOG.warn("NumberFormatException processing <forecast> tag.", nfe);
                } catch (final ParseException pe) {
                    LOG.warn("ParseException processing <forecast> tag.", pe);
                }
            }

            module.setForecasts(f);
        }

        return module;
    }
}
