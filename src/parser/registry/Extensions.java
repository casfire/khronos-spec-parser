package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Extensions {
	
	public final Extension extensions[];
	
	public Extensions(Element node) {
		List<Extension> extensions = new ArrayList<Extension>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			if (e.getTagName().equals("extension")) extensions.add(new Extension(e));
		}
		this.extensions = extensions.toArray(new Extension[extensions.size()]);
	}
	
}
