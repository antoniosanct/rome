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
package com.rometools.modules.yahooweather.types;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;

/**
 * Forecast information about current astronomical conditions. Attributes:
 * <ul class="topspace">
 * <li>sunrise: today's sunrise time. The time is a string in a local time format of "h:mm am/pm",
 * for example "7:02 am" (string)</li>
 * <li>sunset today's sunset time. The time is a string in a local time format of "h:mm am/pm", for
 * example "4:51 pm" (string)</li>
 * </ul>
 */
public class Astronomy implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    private LocalTime sunrise;
    private LocalTime sunset;

    /**
     * Simple constructor.
     */
    public Astronomy() {
        super();
    }

    /**
     * @param sunrise time of sunrise (from 0ms)
     * @param sunset time of sunset (from 0ms)
     */
    public Astronomy(final LocalTime sunrise, final LocalTime sunset) {
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    @Override
    public Object clone() {
        return new Astronomy(getSunrise() != null ? getSunrise() : null,
            getSunset() != null ? getSunset() : null);
    }

    @Override
    public boolean equals(final Object o) {
        return EqualsBean.beanEquals(Astronomy.class, this, o);
    }

    @Override
    public int hashCode() {
        return EqualsBean.beanHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBean.toString(Astronomy.class, this);
    }

    /**
     * Time of sunrise
     *
     * @return ime of sunrise (from 0ms)
     */
    public LocalTime getSunrise() {
        return sunrise;
    }

    /**
     * Time of sunrise
     *
     * @param sunrise ime of sunrise (from 0ms)
     */
    public void setSunrise(final LocalTime sunrise) {
        this.sunrise = sunrise;
    }

    /**
     * Time of sunset
     *
     * @return time of sunset (from 0ms)
     */
    public LocalTime getSunset() {
        return sunset;
    }

    /**
     * Time of sunset
     *
     * @param sunset time of sunset (from 0ms)
     */
    public void setSunset(final LocalTime sunset) {
        this.sunset = sunset;
    }
}
