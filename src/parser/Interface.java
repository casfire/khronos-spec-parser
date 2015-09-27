package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import parser.registry.Command;
import parser.registry.Commands;
import parser.registry.Enum;
import parser.registry.Enums;
import parser.registry.Group;
import parser.registry.Groups;
import parser.registry.Param;
import parser.registry.RInterface;
import parser.registry.Remove;
import parser.registry.Require;
import parser.registry.Type;

public class Interface {
	
	public final Set<String> types;
	public final Set<String> enums;
	public final Set<String> commands;
	public final Set<String> defines;
	public final String      protect;
	private final Specification spec;
	
	public Interface(Specification spec, String protect) {
		this.spec     = spec;
		this.protect  = protect;
		this.types    = new LinkedHashSet<String>();
		this.enums    = new LinkedHashSet<String>();
		this.commands = new LinkedHashSet<String>();
		this.defines  = new LinkedHashSet<String>();
	}
	
	public void push(RInterface r) {
		if (r instanceof Require) push((Require) r);
		if (r instanceof Remove ) push((Remove)  r);
	}
	
	public void push(Require r) {
		types.addAll(Arrays.asList(r.types));
		enums.addAll(Arrays.asList(r.enums));
		commands.addAll(Arrays.asList(r.commands));
	}
	
	public void push(Remove r) {
		for (String s : r.types) types.remove(s);
		for (String s : r.enums) enums.remove(s);
		for (String s : r.commands) commands.remove(s);
	}
	
	public void define(String def) {
		if (def == null || def.isEmpty()) throw new NullPointerException();
		defines.add(def);
	}
	
	private void addType(List<Type> list, Type add) {
		if (add != null && add.requires != null) {
			addType(list, spec.findType(add.requires));
		}
		if (add != null && !list.contains(add)) list.add(add);
	}
	
	public List<Type> getTypes() {
		Set<Type> required = new LinkedHashSet<Type>();
		for (String name : commands) {
			Command cmd = spec.findCommand(name);
			required.add(spec.findType(cmd.proto.ptype));
			for (Param param : cmd.params) {
				required.add(spec.findType(param.ptype));
			}
		}
		List<Type> list = new ArrayList<Type>();
		for (Type t : required) addType(list, t);
		StringBuilder neverused = new StringBuilder();
		for (String name : types) {
			boolean found = false;
			for (Type t : list) if (t.name.equals(name)) {
				found = true;
				break;
			}
			if (!found) {
				if (neverused.length() > 0) neverused.append(", ");
				neverused.append(name);
				addType(list, spec.findType(name));
			}
		}
		if (neverused.length() > 0) {
			System.out.println("Required types " + neverused + " never used in any commands. Added anyway.");
		}
		return list;
	}
	
	public List<Enum> getEnums() {
		Set<Enum>  set  = new LinkedHashSet<Enum>();
		List<Enum> list = new ArrayList<Enum>();
		for (String name : enums) set.add(spec.findEnum(name));
		for (Groups groups : spec.registry.groups) for (Group g : groups.groups) for (String name : g.enums) {
			for (Enum e : set) if (e.name.equals(name)) {
				list.add(e);
				set.remove(e);
				break;
			}
		}
		if (!set.isEmpty()) {
			//System.out.println("No group found for " + set.size() + " enums.");
			list.addAll(set);
		}
		return list;
	}
	
	public List<Command> getCommands() {
		Set<Command>  set  = new LinkedHashSet<Command>();
		List<Command> list = new ArrayList<Command>();
		for (String name : commands) set.add(spec.findCommand(name));
		for (Commands cmds : spec.registry.commands) for (Command cmd : cmds.commands) {
			for (Command c : set) if (c == cmd) {
				list.add(c);
				set.remove(c);
				break;
			}
		}
		if (!set.isEmpty()) throw new RuntimeException();
		return list;
	}
	
	public boolean containsEnum(String name) {
		if (enums.contains(name)) return true;
		boolean found = false;
		for (Enums enms : spec.registry.enums) if (!found) for (Enum e : enms.enums) {
			if ((name.equals(e.name) || name.equals(e.alias)) && (enums.contains(e.name) || enums.contains(e.alias))) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public boolean containsCommand(String name) {
		if (commands.contains(name)) return true;
		boolean found = false;
		for (Commands cmds : spec.registry.commands) if (!found) for (Command cmd : cmds.commands) {
			if ((name.equals(cmd.proto.name) || name.equals(cmd.alias)) && (enums.contains(cmd.proto.name) || enums.contains(cmd.alias))) {
				found = true;
				break;
			}
		}
		return found;
	}
	
}
