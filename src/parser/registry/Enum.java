package parser.registry;

import org.w3c.dom.Element;

public class Enum {
	
	public final String value;
	public final String name;
	public final String api;   // Optional
	public final String type;  // Optional
	public final String alias; // Optional
	
	public Enum(Element node) {
		value = node.hasAttribute("value") ? node.getAttribute("value") : null;
		name  = node.hasAttribute("name")  ? node.getAttribute("name")  : null;
		api   = node.hasAttribute("api")   ? node.getAttribute("api")   : null;
		type  = node.hasAttribute("type")  ? node.getAttribute("type")  : null;
		alias = node.hasAttribute("alias") ? node.getAttribute("alias") : null;
	}
	
}
