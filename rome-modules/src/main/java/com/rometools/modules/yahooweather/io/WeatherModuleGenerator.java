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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.modules.yahooweather.YWeatherModule;
import com.rometools.modules.yahooweather.YWeatherModuleImpl;
import com.rometools.modules.yahooweather.types.Forecast;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ChildNavigator;
import com.rometools.rome.io.ModuleGenerator;

/**
 * The ModuleGenerator implementation for the Yahoo Weather plug in.
 */
public class WeatherModuleGenerator implements ModuleGenerator {
    private static final Namespace NS = ChildNavigator.createNamespace("yweather", YWeatherModule.URI);
    

    public WeatherModuleGenerator() {
    }

    @Override
    public void generate(final Module module, final Element element) {
    	final DateFormat TIME_ONLY = new SimpleDateFormat("h:mm a", Locale.US);
        final DateFormat LONG_DATE = new SimpleDateFormat("EEE, d MMM yyyy h:mm a zzz", Locale.US);
        final DateFormat SHORT_DATE = new SimpleDateFormat("d MMM yyyy", Locale.US);
        
        if (!(module instanceof YWeatherModuleImpl)) {
            return;
        }

        final YWeatherModuleImpl weather = (YWeatherModuleImpl) module;

        if (weather.getAstronomy() != null) {
            final Element astro = element.getOwnerDocument().createElementNS(WeatherModuleGenerator.NS.getNamespaceURI(), "astronomy");

            if (weather.getAstronomy().getSunrise() != null) {
                astro.setAttribute("sunrise", TIME_ONLY.format(weather.getAstronomy().getSunrise()).toLowerCase());
            }

            if (weather.getAstronomy().getSunset() != null) {
                astro.setAttribute("sunset", TIME_ONLY.format(weather.getAstronomy().getSunset()).toLowerCase());
            }
            astro.setPrefix(WeatherModuleGenerator.NS.getPrefix());
            element.appendChild(astro);
        }

        if (weather.getAtmosphere() != null) {
            final Element atmos = element.getOwnerDocument().createElementNS(WeatherModuleGenerator.NS.getNamespaceURI(), "atmosphere");
            atmos.setAttribute("humidity", Integer.toString(weather.getAtmosphere().getHumidity()));
            atmos.setAttribute("visibility", Integer.toString((int) (weather.getAtmosphere().getVisibility() * 100d)));
            atmos.setAttribute("pressure", Double.toString(weather.getAtmosphere().getPressure()));

            if (weather.getAtmosphere().getChange() != null) {
                atmos.setAttribute("rising", Integer.toString(weather.getAtmosphere().getChange().getCode()));
            }
            atmos.setPrefix(WeatherModuleGenerator.NS.getPrefix());
            element.appendChild(atmos);
        }

        if (weather.getCondition() != null) {
            final Element condition = element.getOwnerDocument().createElementNS(WeatherModuleGenerator.NS.getNamespaceURI(), "condition");

            if (weather.getCondition().getText() != null) {
                condition.setAttribute("text", weather.getCondition().getText());
            }

            if (weather.getCondition().getCode() != null) {
                condition.setAttribute("code", Integer.toString(weather.getCondition().getCode().getCode()));
            }

            if (weather.getCondition().getDate() != null) {
                condition.setAttribute("date", LONG_DATE.format(weather.getCondition().getDate()).replaceAll("AM", "am").replaceAll("PM", "pm"));
            }

            condition.setAttribute("temp", Integer.toString(weather.getCondition().getTemperature()));
            condition.setPrefix(WeatherModuleGenerator.NS.getPrefix());
            element.appendChild(condition);
        }

        if (weather.getLocation() != null) {
            final Element location = element.getOwnerDocument().createElementNS(WeatherModuleGenerator.NS.getNamespaceURI(), "location");

            if (weather.getLocation().getCity() != null) {
                location.setAttribute("city", weather.getLocation().getCity());
            }

            if (weather.getLocation().getRegion() != null) {
                location.setAttribute("region", weather.getLocation().getRegion());
            }

            if (weather.getLocation().getCountry() != null) {
                location.setAttribute("country", weather.getLocation().getCountry());
            }
            location.setPrefix(WeatherModuleGenerator.NS.getPrefix());
            element.appendChild(location);
        }

        if (weather.getUnits() != null) {
            final Element units = element.getOwnerDocument().createElementNS(WeatherModuleGenerator.NS.getNamespaceURI(), "units");

            if (weather.getUnits().getDistance() != null) {
                units.setAttribute("distance", weather.getUnits().getDistance());
            }

            if (weather.getUnits().getPressure() != null) {
                units.setAttribute("pressure", weather.getUnits().getPressure());
            }

            if (weather.getUnits().getSpeed() != null) {
                units.setAttribute("speed", weather.getUnits().getSpeed());
            }

            if (weather.getUnits().getTemperature() != null) {
                units.setAttribute("temperature", weather.getUnits().getTemperature());
            }
            units.setPrefix(WeatherModuleGenerator.NS.getPrefix());
            element.appendChild(units);
        }

        if (weather.getWind() != null) {
            final Element wind = element.getOwnerDocument().createElementNS(WeatherModuleGenerator.NS.getNamespaceURI(), "wind");
            wind.setAttribute("chill", Integer.toString(weather.getWind().getChill()));
            wind.setAttribute("direction", Integer.toString(weather.getWind().getDirection()));
            wind.setAttribute("speed", Integer.toString(weather.getWind().getSpeed()));
            wind.setPrefix(WeatherModuleGenerator.NS.getPrefix());
            element.appendChild(wind);
        }

        if (weather.getForecasts() != null) {
            for (int i = 0; i < weather.getForecasts().length; i++) {
                final Element forecast = element.getOwnerDocument().createElementNS(WeatherModuleGenerator.NS.getNamespaceURI(), "forecast");
                final Forecast f = weather.getForecasts()[i];

                if (f.getCode() != null) {
                    forecast.setAttribute("code", Integer.toString(f.getCode().getCode()));
                }

                if (f.getDate() != null) {
                    forecast.setAttribute("date", SHORT_DATE.format(f.getDate()));
                }

                if (f.getDay() != null) {
                    forecast.setAttribute("day", f.getDay());
                }

                if (f.getText() != null) {
                    forecast.setAttribute("text", f.getText());
                }

                forecast.setAttribute("high", Integer.toString(f.getHigh()));
                forecast.setAttribute("low", Integer.toString(f.getLow()));
                forecast.setPrefix(WeatherModuleGenerator.NS.getPrefix());
                element.appendChild(forecast);
            }
        }
    }

    @Override
    public Set<Namespace> getNamespaces() {
        final Set<Namespace> set = new HashSet<Namespace>();
        set.add(WeatherModuleGenerator.NS);
        return set;
    }

    @Override
    public String getNamespaceUri() {
        return YWeatherModule.URI;
    }
}
