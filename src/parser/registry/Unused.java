package parser.registry;

import org.w3c.dom.Element;

public class Unused {
	
	public final Integer start;
	public final Integer end;
	public final String  comment;
	
	public Unused(Element node) {
		start   = node.hasAttribute("start")   ? Integer.decode(node.getAttribute("start")) : null;
		end     = node.hasAttribute("end")     ? Integer.decode(node.getAttribute("end"))   : null;
		comment = node.hasAttribute("comment") ? node.getAttribute("comment")               : null;
	}
	
}
