package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Enums {
	
	public final String   namespace;
	public final String   type;
	public final String   start;
	public final String   end;
	public final String   vendor;
	public final String   comment;
	public final Enum[]   enums;
	public final Unused[] unused;
	
	public Enums(Element node) {
		namespace = node.hasAttribute("namespace") ? node.getAttribute("namespace") : null;
		type      = node.hasAttribute("type")      ? node.getAttribute("type")      : null;
		vendor    = node.hasAttribute("vendor")    ? node.getAttribute("vendor")    : null;
		comment   = node.hasAttribute("comment")   ? node.getAttribute("comment")   : null;
		start     = node.hasAttribute("start")     ? node.getAttribute("start")     : null;
		end       = node.hasAttribute("end")       ? node.getAttribute("end")       : null;
		List<Enum>   enums  = new ArrayList<Enum>();
		List<Unused> unused = new ArrayList<Unused>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			switch (e.getTagName()) {
			case "enum":
				enums.add(new Enum(e));
				break;
			case "unused":
				unused.add(new Unused(e));
				break;
			}
		}
		this.enums  = enums.toArray (new   Enum[enums.size() ]);
		this.unused = unused.toArray(new Unused[unused.size()]);
	}
	
}
