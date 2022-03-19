package com.rometools.rome.feed;

import java.io.Serializable;

import org.w3c.dom.Element;

import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;

public class WireFeedForeignMarkup implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

	private Element element;

	public WireFeedForeignMarkup() {
		this(null);
	}

	public WireFeedForeignMarkup(Element element) {
		super();
		this.element = element;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		WireFeedForeignMarkup wffm = new WireFeedForeignMarkup();
		wffm.setElement((Element) element.cloneNode(true));
		return wffm;
	}

	@Override
    public int hashCode() {
        return EqualsBean.beanHashCode(this);
    }

	@Override
    public boolean equals(final Object other) {
        if (!(other instanceof WireFeedForeignMarkup)) {
            return false;
        }
        return EqualsBean.beanEquals(this.getClass(), this, other);
    }
	
	@Override
    public String toString() {
        return ToStringBean.toString(this.getClass(), this);
    }

	/**
	 * @return the element
	 */
	public final Element getElement() {
		return element;
	}

	/**
	 * @param element the element to set
	 */
	public final void setElement(Element element) {
		this.element = element;
	}
	
}
