package parser.registry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Type {
	
	public final String code;
	public final String name;
	public final String requires;
	public final String api;
	public final String comment;
	
	public Type(Element node) {
		String name = null;
		StringBuilder code = new StringBuilder();
		requires = node.hasAttribute("requires") ? node.getAttribute("requires") : null;
		api      = node.hasAttribute("api")      ? node.getAttribute("api")      : null;
		comment  = node.hasAttribute("comment")  ? node.getAttribute("comment")  : null;
		name     = node.hasAttribute("name")     ? node.getAttribute("name")     : null;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element) {
				Element e = (Element) child;
				if (e.getTagName().equals("apientry")) {
					//code.append("APIENTRY");
				} else if (e.getTagName().equals("name")) {
					code.append(name = child.getTextContent());
				}
			} else if (child.getNodeType() == Node.TEXT_NODE) {
				code.append(child.getTextContent());
			}
		}
		this.name = name;
		this.code = code.toString();
	}
	
}
