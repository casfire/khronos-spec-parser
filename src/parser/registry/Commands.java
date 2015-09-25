package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Commands {
	
	public final String  namespace;
	public final Command commands[];
	
	public Commands(Element node) {
		namespace = node.hasAttribute("namespace") ? node.getAttribute("namespace") : null;
		List<Command> commands = new ArrayList<Command>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			if (e.getTagName().equals("command")) commands.add(new Command(e));
		}
		this.commands = commands.toArray(new Command[commands.size()]);
	}
	
}
