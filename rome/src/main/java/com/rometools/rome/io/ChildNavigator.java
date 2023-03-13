package com.rometools.rome.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility class for DOM navigation
 *
 */
public class ChildNavigator {

	public static Namespace createNamespace(final String uri) {
		return XMLEventFactory.newFactory().createNamespace(uri);
	}

	public static Namespace createNamespace(final String prefix, final String uri) {
		return XMLEventFactory.newFactory().createNamespace(prefix, uri);
	}

	protected List<Element> getChildren(final Element element) {
		List<Element> result = new ArrayList<>(1);
		if (null != element && null != element.getChildNodes() && element.getChildNodes().getLength() > 0) {
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				if (element.getChildNodes().item(i) instanceof Element) {
					result.add((Element) element.getChildNodes().item(i));
				}
			}
		}
		return result;
		
	}
	
	/**
	 * Get the first child nested in element with child local name
	 * @param element the element to get the first occurrence
	 * @param child the child local name
	 * @return the DOM Element object
	 */
	protected Element getChild(final Element element, final String child) {
		Element result = null;
		boolean found = false;
		if (null != element && null != element.getChildNodes() && element.getChildNodes().getLength() > 0) {
			for (int i = 0; !found && i < element.getChildNodes().getLength(); i++) {
				if (element.getChildNodes().item(i) instanceof Element) {
					Element candidate = (Element) element.getChildNodes().item(i);
					if (child.equals(candidate.getLocalName())) {
						found = true;
						result = (Element) element.getChildNodes().item(i);
					}
				}
			}
		}
		return result;
		
	}
	
	/**
	 * Get the first child nested in element with child local name, using a determinated Namespace URI.
	 * @param element the element to get the first occurrence.
	 * @param child the child local name.
	 * @param namespace the Namespace URI (null or exact occurrence).
	 * @return the DOM Element object
	 */
	protected Element getChild(final Element element, final String child, final Namespace namespace) {
		List<Element> results = new ArrayList<>(1);
		final List<Namespace> allNamespaces = getAllNamespaceDeclarations(getFirstChildElement(element.getOwnerDocument()));
		if (null != element && null != element.getChildNodes() && element.getChildNodes().getLength() > 0) {
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				if (element.getChildNodes().item(i) instanceof Element) {
					Element candidate = (Element) element.getChildNodes().item(i);
					if (child.equals(candidate.getLocalName()) &&
							(null == candidate.getNamespaceURI() ||
									namespace.getNamespaceURI().equals(candidate.getNamespaceURI()))) {
						results.add(candidate);
					}
				}
			}
		}
		Element result = null;
		if (results.size() > 0) {
			// two nodes with same local name ¿? how about prefix?
			// gets the first not null
			result = results.get(0);
			// if found a prefix-joined child name at the same namespace
			// returns this.
			boolean found = false;
			for (int i = 1; !found && i < results.size(); i++) {
				for (Namespace n : allNamespaces) {
					final String nodeToCompare = String.join(":", n.getPrefix(), child);
					if (results.get(i).getNodeName().equals(nodeToCompare)) {
						result = results.get(i);
						found = true;
					}
				}
			}
			
		}
		return result;
		
	}
	
	protected Element getFirstChildElement(Document ownerDocument) {
		Element result = null;
		boolean found = false;
		for (int i = 0; !found && i < ownerDocument.getChildNodes().getLength(); i++) {
			Node n = ownerDocument.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				found = true;
				result = (Element) n;
			}
		}
		return result;
	}

	private List<Namespace> getAllNamespaceDeclarations(final Element elem) {
		List<Namespace> listNamespaces = new ArrayList<>(1);
		if (null != elem && null != elem.getAttributes()) {
			for (int i = 0; i < elem.getAttributes().getLength(); i++) {
				Attr a = (Attr) elem.getAttributes().item(i);
				if (a.getNodeName().indexOf("xmlns:") == 0) {
					listNamespaces.add(ChildNavigator.createNamespace(
							a.getNodeName().substring(a.getNodeName().indexOf("xmlns:")+6), a.getValue()));
				}
			}
		}
		return listNamespaces;
		
	}

	/**
	 * Get the all child occurrences nested in element with child local name
	 * @param element the element to get the first occurrence
	 * @param child the child local name
	 * @return a list of DOM Element objects
	 */
	protected List<Element> getChildren(final Element element, final String child) {
		List<Element> result = new ArrayList<>(1);
		
		
		if (null != element && null != element.getChildNodes() && element.getChildNodes().getLength() > 0) {
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				if (element.getChildNodes().item(i) instanceof Element) {
					Element candidate = (Element) element.getChildNodes().item(i);
					if (child.equals(candidate.getLocalName())) {
						result.add((Element) element.getChildNodes().item(i));
					}
				}
			}
		}
		return result;
		
	}
	
	/**
	 * Get the all child occurrences nested in element with child local name, using a determinated Namespace URI.
	 * @param element the element to get the first occurrence.
	 * @param child the child local name.
	 * @param namespace the Namespace URI (null or exact occurrence).
	 * @return a list of DOM Element objects
	 */
	protected List<Element> getChildren(final Element element, final String child, final Namespace namespace) {
		List<Element> previousResults = new ArrayList<>(1);
		List<Element> results = new ArrayList<>(1);
		final List<Namespace> allNamespaces = getAllNamespaceDeclarations(getFirstChildElement(element.getOwnerDocument()));
		
		if (null != element && null != element.getChildNodes() && element.getChildNodes().getLength() > 0) {
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				if (element.getChildNodes().item(i) instanceof Element) {
					Element candidate = (Element) element.getChildNodes().item(i);
					if (child.equals(candidate.getLocalName()) &&
							(null == candidate.getNamespaceURI() ||
							namespace.getNamespaceURI().equals(candidate.getNamespaceURI()))) {
						previousResults.add((Element) element.getChildNodes().item(i));
					}
				}
			}
			
			for (int i = 0; i < previousResults.size(); i++) {
				for (Namespace n : allNamespaces) {
					final String nodeToCompare = String.join(":", n.getPrefix(), child);
					if (previousResults.get(i).getNodeName().equals(nodeToCompare)) {
						results.add(previousResults.get(i));
					}
				}
			}
		}
		if (results.isEmpty()) {
			return previousResults;
		}
		return results;
		
	}
	
	protected String getAttributeNotBlank(final String name, final Element e) {
		String result = null;
		if (null != e.getAttribute(name) && !"".equals(e.getAttribute(name))) {
			result = e.getAttribute(name);
		}
		return result;
	}
	
	protected String getAttributeNotBlank(final String name, final Element e, final Namespace n) {
		String result = null;
		if (null != e.getAttributeNS(n.getNamespaceURI(), name) && !"".equals(e.getAttributeNS(n.getNamespaceURI(), name))) {
			result = e.getAttributeNS(n.getNamespaceURI(), name);
		}
		return result;
	}
}
