/*
 * Copyright 2005 Robert Cooper, Temple of the Screaming Penguin
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
 *
 */
package com.rometools.modules.content;

import java.util.List;
import java.util.Objects;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;

import com.rometools.rome.feed.impl.ToStringBean;
import com.rometools.utils.DOMNodes;

/**
 * This class represents a content item per the "Original Syntax".
 * http://purl.org/rss/1.0/modules/content/
 */
public class ContentItem implements Cloneable {

    private String contentFormat;
    private String contentEncoding;
    private String contentValue;
    private Element contentValueDOM;
    private String contentAbout;
    private String contentValueParseType;
    private List<Namespace> contentValueNamespace;
    private String contentResource;

    public ContentItem() {
    }

    public String getContentFormat() {
        return contentFormat;
    }

    public void setContentFormat(final String contentFormat) {
        this.contentFormat = contentFormat;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentValue() {
        return contentValue;
    }

    public void setContentValue(final String contentValue) {
        this.contentValue = contentValue;
    }

    public Element getContentValueDOM() {
        return contentValueDOM;
    }

    public void setContentValueDOM(final Element contentValueDOM) {
        this.contentValueDOM = contentValueDOM;
    }

    public String getContentAbout() {
        return contentAbout;
    }

    public void setContentAbout(final String contentAbout) {
        this.contentAbout = contentAbout;
    }

    public String getContentValueParseType() {
        return contentValueParseType;
    }

    public void setContentValueParseType(final String contentValueParseType) {
        this.contentValueParseType = contentValueParseType;
    }

    public List<Namespace> getContentValueNamespaces() {
        return contentValueNamespace;
    }

    public void setContentValueNamespaces(final List<Namespace> contentValueNamespace) {
        this.contentValueNamespace = contentValueNamespace;
    }

    public String getContentResource() {
        return contentResource;
    }

    public void setContentResource(final String contentResource) {
        this.contentResource = contentResource;
    }

    

    @Override
	public int hashCode() {
		return Objects.hash(contentAbout, contentEncoding, contentFormat, contentResource, contentValue,
				contentValueNamespace, contentValueParseType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContentItem other = (ContentItem) obj;
		String thisCV = null;
		String thatCV = null;
		if (null != contentValue && null != other.contentValue) {
			thisCV = contentValue.replaceAll(" xmlns=\"http://www.w3.org/1999/xhtml\"", "").trim();
	        thatCV = other.contentValue.replaceAll(" xmlns=\"http://www.w3.org/1999/xhtml\"", "").trim();
		}
		return Objects.equals(contentAbout, other.contentAbout)
				&& Objects.equals(contentEncoding, other.contentEncoding)
				&& Objects.equals(contentFormat, other.contentFormat)
				&& Objects.equals(contentResource, other.contentResource)
				&& Objects.equals(thisCV, thatCV)
				&& DOMNodes.compareNodes(contentValueDOM, other.contentValueDOM)
				&& Objects.equals(contentValueNamespace, other.contentValueNamespace)
				&& Objects.equals(contentValueParseType, other.contentValueParseType);
	}

	@Override
    public Object clone() {
        final ContentItem o = new ContentItem();
        o.contentAbout = contentAbout;
        o.contentEncoding = contentEncoding;
        o.contentFormat = contentFormat;
        o.contentResource = contentResource;
        o.contentValue = contentValue;
        o.contentValueDOM = contentValueDOM;
        o.contentValueNamespace = contentValueNamespace;
        o.contentValueParseType = contentValueParseType;

        return o;
    }

	@Override
	public String toString() {
		return ToStringBean.toString(ContentItem.class, this);
	}
	
	
}
