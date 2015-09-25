package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Group {
	
	public final String   name;
	public final String[] enums;
	
	public Group(Element node) {
		name = node.hasAttribute("name") ? node.getAttribute("name") : null;
		List<String> enums = new ArrayList<String>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			if (e.getTagName().equals("enum")) enums.add(e.getAttribute("name"));
		}
		this.enums = enums.toArray(new String[enums.size()]);
	}
	
}
