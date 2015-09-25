package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Extension {
	
	public final String name;
	public final String supported;
	public final String protect;
	public final String comment;
	public final Require require[];
	public final Remove  remove[];
	
	public Extension(Element node) {
		name      = node.hasAttribute("name")      ? node.getAttribute("name")      : null;
		supported = node.hasAttribute("supported") ? node.getAttribute("supported") : null;
		protect   = node.hasAttribute("protect")   ? node.getAttribute("protect")   : null;
		comment   = node.hasAttribute("comment")   ? node.getAttribute("comment")   : null;
		List<Require> require = new ArrayList<Require>();
		List<Remove>  remove  = new ArrayList<Remove>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			switch (e.getTagName()) {
			case "require":
				require.add(new Require(e));
				break;
			case "remove":
				remove.add(new Remove(e));
				break;
			}
		}
		this.require = require.toArray(new Require[require.size()]);
		this.remove  = remove.toArray (new Remove [remove.size()]);
	}
	
}
