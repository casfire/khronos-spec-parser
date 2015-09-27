package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class RInterface {
	
	public final String profile;
	public final String comment;
	public final String api;
	public final String commands[];
	public final String enums[];
	public final String types[];
	
	public RInterface(Element node) {
		profile = node.hasAttribute("profile") ? node.getAttribute("profile") : null;
		comment = node.hasAttribute("comment") ? node.getAttribute("comment") : null;
		api     = node.hasAttribute("api")     ? node.getAttribute("api")     : null;
		List<String> commands = new ArrayList<String>();
		List<String> enums    = new ArrayList<String>();
		List<String> types    = new ArrayList<String>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			String name = e.getAttribute("name");
			switch (e.getTagName()) {
			case "command":
				commands.add(name);
				break;
			case "enum":
				enums.add(name);
				break;
			case "type":
				types.add(name);
				break;
			}
		}
		this.commands = commands.toArray(new String[commands.size()]);
		this.enums    = enums.toArray   (new String[enums.size()   ]);
		this.types    = types.toArray   (new String[types.size()   ]);
	}

}
