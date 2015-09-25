package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Types {
	
	public final Type types[];
	
	public Types(Element node) {
		List<Type> types = new ArrayList<Type>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			if (e.getTagName().equals("type")) types.add(new Type(e));
		}
		this.types = types.toArray(new Type[types.size()]);
	}
	
}
