package parser.registry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Proto {
	
	public final String code;
	public final String ptype;
	public final String name;
	public final String group;
	
	public Proto(Element node) {
		group = node.hasAttribute("group") ? node.getAttribute("group") : null;
		StringBuilder code  = new StringBuilder();
		String        ptype = null;
		String        name  = null;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.TEXT_NODE) {
				code.append(child.getTextContent());
			}
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			switch (e.getTagName()) {
			case "ptype":
				code.append(child.getTextContent());
				ptype = e.getTextContent();
				break;
			case "name":
				name = e.getTextContent();
				break;
			default:
				code.append(child.getTextContent());
			}
		}
		this.ptype = ptype;
		this.name  = name;
		this.code  = code.toString();
	}
	
}
