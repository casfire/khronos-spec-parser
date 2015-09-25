package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Groups {
	
	public final Group groups[];
	
	public Groups(Element node) {
		List<Group> groups = new ArrayList<Group>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			if (e.getTagName().equals("group")) groups.add(new Group(e));
		}
		this.groups = groups.toArray(new Group[groups.size()]);
	}
	
}
