package parser.registry;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Registry {
	
	public final String     comments[];
	public final Types      types[];
	public final Groups     groups[];
	public final Enums      enums[];
	public final Commands   commands[];
	public final Feature    features[];
	public final Extensions extensions[];
	
	public Registry(Element node) {
		List<String>     comments   = new ArrayList<String>();
		List<Types>      types      = new ArrayList<Types>();
		List<Groups>     groups     = new ArrayList<Groups>();
		List<Enums>      enums      = new ArrayList<Enums>();
		List<Commands>   commands   = new ArrayList<Commands>();
		List<Feature>    features   = new ArrayList<Feature>();
		List<Extensions> extensions = new ArrayList<Extensions>();
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!(child instanceof Element)) continue;
			Element e = (Element) child;
			switch (e.getTagName()) {
			case "comment":
				comments.add(e.getTextContent());
				break;
			case "types":
				types.add(new Types(e));
				break;
			case "groups":
				groups.add(new Groups(e));
				break;
			case "enums":
				enums.add(new Enums(e));
				break;
			case "commands":
				commands.add(new Commands(e));
				break;
			case "feature":
				features.add(new Feature(e));
				break;
			case "extensions":
				extensions.add(new Extensions(e));
				break;
			}
		}
		this.comments   = comments.toArray  (new String    [comments.size()  ]);
		this.types      = types.toArray     (new Types     [types.size()     ]);
		this.groups     = groups.toArray    (new Groups    [groups.size()    ]);
		this.enums      = enums.toArray     (new Enums     [enums.size()     ]);
		this.commands   = commands.toArray  (new Commands  [commands.size()  ]);
		this.features   = features.toArray  (new Feature   [features.size()  ]);
		this.extensions = extensions.toArray(new Extensions[extensions.size()]);
	}
	
}
