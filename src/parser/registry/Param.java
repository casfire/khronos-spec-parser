package parser.registry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Param {
	
	public final String code;
	public final String ptype;
	public final String name;
	public final String group;
	public final String len;
	
	public Param(Element node) {
		group = node.hasAttribute("group") ? node.getAttribute("group") : null;
		len   = node.hasAttribute("len")   ? node.getAttribute("len")   : null;
		StringBuilder code  = new StringBuilder();
		String        ptype = null;
		String        name  = null;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			code.append(child.getTextContent());
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			switch (e.getTagName()) {
			case "ptype":
				ptype = e.getTextContent();
				break;
			case "name":
				name = e.getTextContent();
				break;
			}
		}
		this.ptype = ptype;
		this.name  = name;
		this.code  = code.toString();
	}

}
