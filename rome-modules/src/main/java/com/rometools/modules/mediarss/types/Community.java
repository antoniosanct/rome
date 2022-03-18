/*
 * This code is currently released under the Mozilla Public License.
 * http://www.mozilla.org/MPL/
 *
 * Alternately you may apply the terms of the Apache Software License
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.rometools.modules.mediarss.types;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * media:community element.
 * 
 * @since MediaRSS 1.5.0
 */
public class Community implements Serializable {
    private static final long serialVersionUID = 1176552685678871066L;
    private StarRating starRating;
    private Statistics statistics;
    private final Set<Tag> tags = new TreeSet<Tag>();

    public Set<Tag> getTags() {
        return tags;
    }

    public StarRating getStarRating() {
        return starRating;
    }

    public void setStarRating(final StarRating starRating) {
        this.starRating = starRating;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(final Statistics statistics) {
        this.statistics = statistics;
    }

    //CHECKSTYLE:OFF
    @Override
    public String toString() {
        return "Community [starRating=" + starRating + ", statistics=" + statistics + ", tags=" + tags + "]";
    }

	@Override
	public int hashCode() {
		return Objects.hash(starRating, statistics, tags);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Community other = (Community) obj;
		return Objects.equals(starRating, other.starRating) && Objects.equals(statistics, other.statistics)
				&& Objects.equals(tags, other.tags);
	}
    //CHECKSTYLE:ON
}
