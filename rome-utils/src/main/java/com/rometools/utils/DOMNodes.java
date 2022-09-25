package com.rometools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @version $Rev: 514087 $ $Date: 2007-03-03 01:13:40 -0500 (Sat, 03 Mar 2007) $
 */
public class DOMNodes {

	private static final Logger LOG = LoggerFactory.getLogger(DOMNodes.class);

	/**
     * @param n Node to convert to string
     * @return a String representation of a node.
     * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
     * @version $Revision: 3282 $ $Id: XMLUtil.java 3282 2007-11-01
     *          15:32:29Z timfox $
     */
	public static String nodeToString(Node n) {
		final String name = n.getNodeName();
		short type = n.getNodeType();

		if (Node.CDATA_SECTION_NODE == type) {
			return "<![CDATA[" + n.getNodeValue() + "]]&gt;";
		}

		if (name.startsWith("#")) {
			return n.getNodeValue();
		}

		final StringBuffer sb = new StringBuffer();
		sb.append('<').append(name);

		NamedNodeMap attrs = n.getAttributes();
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				sb.append(' ').append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
			}
		}

		String textContent = null;
		NodeList children = n.getChildNodes();

		if (children.getLength() == 0) {
			if ((textContent = n.getTextContent()) != null && !"".equals(textContent)) {
				sb.append(textContent).append("</").append(name).append('>');
				;
			} else {
				sb.append("/>");
			}
		} else {
			sb.append('>');
			boolean hasValidChildren = false;
			for (int i = 0; i < children.getLength(); i++) {
				String childToString = nodeToString(children.item(i));
				if (!"".equals(childToString)) {
					sb.append(childToString);
					hasValidChildren = true;
				}
			}

			if (!hasValidChildren && ((textContent = n.getTextContent()) != null)) {
				sb.append(textContent);
			}

			sb.append("</").append(name).append('>');
		}

		return sb.toString();
	}
	
	/**
	 * Compare two nodes and returns if true
	 * 
	 * @param expected the first node to compare
	 * @param actual the actual node to be compared
	 * @return if true
	 */
	public static boolean compareNodes(Node expected, Node actual) {
		boolean result = true;
		if (null != expected && null != actual) {
			if (expected.getNodeType() != actual.getNodeType()) {
				LOG.debug("Different types of nodes: " + expected + " " + actual);
				result = false;
			} else if (expected instanceof Document) {
				Document expectedDoc = (Document) expected;
				Document actualDoc = (Document) actual;
				result = compareNodes(expectedDoc.getDocumentElement(), actualDoc.getDocumentElement());
			} else if (expected instanceof Element) {
				Element expectedElement = (Element) expected;
				Element actualElement = (Element) actual;
	
				// compare element names
				if (null == expectedElement.getLocalName() || null == actualElement.getLocalName()) {
					LOG.debug("Element names do not match: " + expectedElement.getLocalName() + " "
							+ actualElement.getLocalName());
					result = false;
				} else if (!expectedElement.getLocalName().equals(actualElement.getLocalName())) {
					LOG.debug("Element names do not match: " + expectedElement.getLocalName() + " "
							+ actualElement.getLocalName());
					result = false;
				} else {
					// compare element ns
					String expectedNS = expectedElement.getNamespaceURI();
					String actualNS = actualElement.getNamespaceURI();
					if ((expectedNS == null && actualNS != null) || (expectedNS != null && !expectedNS.equals(actualNS))) {
						LOG.debug("Element namespaces names do not match: " + expectedNS + " " + actualNS);
						result = false;
					} else {
						String elementName = "{" + expectedElement.getNamespaceURI() + "}" + actualElement.getLocalName();
			
						// compare attributes
						NamedNodeMap expectedAttrs = expectedElement.getAttributes();
						NamedNodeMap actualAttrs = actualElement.getAttributes();
						if (countNonNamespaceAttributes(expectedAttrs) != countNonNamespaceAttributes(actualAttrs)) {
							LOG.debug(elementName + ": Number of attributes do not match up: "
									+ countNonNamespaceAttributes(expectedAttrs) + " " + countNonNamespaceAttributes(actualAttrs));
							result = false;
						} else {
							for (int i = 0; i < expectedAttrs.getLength(); i++) {
								Attr expectedAttr = (Attr) expectedAttrs.item(i);
								if (expectedAttr.getName().startsWith("xmlns")) {
									continue;
								}
								Attr actualAttr = null;
								if (expectedAttr.getNamespaceURI() == null) {
									actualAttr = (Attr) actualAttrs.getNamedItem(expectedAttr.getName());
								} else {
									actualAttr = (Attr) actualAttrs.getNamedItemNS(expectedAttr.getNamespaceURI(),
											expectedAttr.getLocalName());
								}
								if (actualAttr == null) {
									LOG.debug(elementName + ": No attribute found:" + expectedAttr);
									result = false;
								} else if (!expectedAttr.getValue().equals(actualAttr.getValue())) {
									LOG.debug(elementName + ": Attribute values do not match: " + expectedAttr.getValue()
											+ " " + actualAttr.getValue());
									result = false;
								}
							}
				
							if (result) {
								// compare children
								NodeList expectedChildren = expectedElement.getChildNodes();
								NodeList actualChildren = actualElement.getChildNodes();
								if (expectedChildren.getLength() != actualChildren.getLength()) {
									LOG.debug(elementName + ": Number of children do not match up: "
											+ expectedChildren.getLength() + " " + actualChildren.getLength());
									result = false;
								} else {
									for (int i = 0; i < expectedChildren.getLength(); i++) {
										Node expectedChild = expectedChildren.item(i);
										Node actualChild = actualChildren.item(i);
										result = compareNodes(expectedChild, actualChild);
									}
								}
							}
						}
					}
				}
			} else if (expected instanceof Text) {
				String expectedData = ((Text) expected).getData().trim();
				String actualData = ((Text) actual).getData().trim();
	
				if (!expectedData.equals(actualData)) {
					LOG.debug("Text does not match: " + expectedData + " " + actualData);
				}
			}
		}
		return result;
	}

	private static int countNonNamespaceAttributes(NamedNodeMap attrs) {
		int n = 0;
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr attr = (Attr) attrs.item(i);
			if (!attr.getName().startsWith("xmlns")) {
				n++;
			}
		}
		return n;
	}

}
