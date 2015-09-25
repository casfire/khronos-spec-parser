package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Command {
	
	public final String comment;
	public final Proto  proto;
	public final Param  params[];
	public final String alias;
	public final String vecequiv;
	
	public Command(Element node) {
		Proto       proto    = null;
		List<Param> params   = new ArrayList<Param>();
		String      alias    = null;
		String      vecequiv = null;
		comment = node.hasAttribute("comment") ? node.getAttribute("comment") : null;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			switch (e.getTagName()) {
			case "proto":
				proto = new Proto(e);
				break;
			case "param":
				params.add(new Param(e));
				break;
			case "alias":
				alias = e.getAttribute("name");
				break;
			case "vecequiv":
				vecequiv = e.getAttribute("name");
				break;
			case "glx":
				// TODO glx ?
				break;
			}
		}
		this.proto    = proto;
		this.params   = params.toArray(new Param[params.size()]);
		this.alias    = alias;
		this.vecequiv = vecequiv;
	}

}
