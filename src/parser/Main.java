package parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.registry.*;
import parser.registry.Enum;

public class Main {
	
	public static void main(String args[]) throws Exception {
		System.out.println("Khronos XML API Registry parser.");
		
		generate(new Spec("gl"));
		generate(new Spec("egl"));
		generate(new Spec("glx"));
		generate(new Spec("wgl"));
		
		System.out.println("Done.");
	}
	
	private static void generate(Spec spec) throws IOException {
		Registry  reg  = spec.reg;
		List<Api> apis = apis(reg);
		List<Gen> gens = gens(reg, apis);
		fill(reg, gens);
		for (Gen g : gens) {
			Data data = data(reg, g);
			header(data, g);
			source(data, g);
		}
	}
	
	private static List<Api> apis(Registry reg) {
		Map<String, Api> apis = new HashMap<String, Api>();
		for (Feature feature : reg.features) {
			Api api = apis.get(feature.api);
			if (api == null) apis.put(feature.api, api = new Api(feature.api));
			api.addVersion(feature.number);
			for (Require r : feature.require) api.addProfile(feature.number, r.profile);
			for (Remove  r : feature.remove ) api.addProfile(feature.number, r.profile);
		}
		return new ArrayList<Api>(apis.values());
	}
	
	private static List<Gen> gens(Registry reg, List<Api> apis) {
		List<Gen> gens = new ArrayList<Gen>();
		for (Api api : apis) {
			for (String version : api.versions) {
				for (String profile : api.profiles) {
					if (api.hasProfile(profile, version)) {
						gens.add(new Gen(api.api, version, profile));
					}
				}
			}
		}
		return gens;
	}
	
	private static void fill(Registry reg, List<Gen> gens) {
		Arrays.sort(reg.features, (a, b) -> a.number.compareTo(b.number));
		for (Feature feature : reg.features) {
			for (Require r : feature.require) push(gens, feature, r);
			for (Remove  r : feature.remove ) push(gens, feature, r);
		}
	}
	
	private static void push(List<Gen> gens, Feature f, RInterface r) {
		String api     = f.api;
		String version = f.number;
		String profile = r.profile;
		for (Gen gen : gens) {
			if (
				gen.api.equals(api) &&
				(profile == null || profile.isEmpty() || profile.equals(gen.profile)) &&
				gen.version.compareTo(version) >= 0
			) {
				if (r instanceof Require) gen.push((Require) r);
				if (r instanceof Remove ) gen.push((Remove)  r);
			}
		}
	}
	
	private static Command Command(Registry reg, String name) {
		if (name == null) return null;
		for (Commands cmds : reg.commands) for (Command c : cmds.commands) {
			if (c.proto.name.equals(name)) return c;
		}
		System.out.println("WARNING: Command " + name + " not found.");
		return null;
	}
	
	private static Type Type(Registry reg, String name) {
		if (name == null) return null;;
		for (Types types : reg.types) for (Type t : types.types) {
			if (t.name.equals(name)) return t;
		}
		System.out.println("WARNING: Type " + name + " not found.");
		return null;
	}
	
	private static Enum Enum(Registry reg, String name) {
		if (name == null) return null;
		for (Enums enums : reg.enums) for (Enum e : enums.enums) {
			if (e.name.equals(name)) return e;
		}
		System.out.println("WARNING: Enum " + name + " not found.");
		return null;
	}
	
	private static class Data {
		final List<Type>    types    = new ArrayList<Type>();
		final List<Enum>    enums    = new ArrayList<Enum>();
		final List<Command> commands = new ArrayList<Command>();
	}
	
	private static void addType(Registry reg, List<Type> list, Type add) {
		if (add != null && add.requires != null) {
			addType(reg, list, Type(reg, add.requires));
		}
		if (!list.contains(add)) list.add(add);
	}
	
	private static Data data(Registry reg, Gen gen) {
		Set<Type>    types    = new LinkedHashSet<Type>();
		Set<Enum>    enums    = new LinkedHashSet<Enum>();
		Set<Command> commands = new LinkedHashSet<Command>();
		for (String name : gen.commands) {
			Command cmd = Command(reg, name);
			types.add(Type(reg, cmd.proto.ptype));
			for (Param param : cmd.params) {
				types.add(Type(reg, param.ptype));
			}
			commands.add(cmd);
		}
		for (String name : gen.enums) {
			enums.add(Enum(reg, name));
		}
		Data data = new Data();
		for (Groups groups : reg.groups) for (Group g : groups.groups) for (String name : g.enums) {
			for (Enum e : enums) if (e.name.equals(name)) {
				data.enums.add(e);
				enums.remove(e);
				break;
			}
		}
		for (Commands cmds : reg.commands) for (Command cmd : cmds.commands) {
			for (Command c : commands) if (c == cmd) {
				data.commands.add(c);
				commands.remove(c);
				break;
			}
		}
		data.enums.addAll(enums);
		data.commands.addAll(commands);
		for (Type t : types) addType(reg, data.types, t);
		return data;
	}
	
	private static void header(Data data, Gen gen) throws IOException {
		System.out.println("Generating " + gen + ".hpp");
		if (!new File("gen").exists()) new File("gen").mkdir();
		BufferedWriter hpp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gen/" + gen.name() + ".hpp"), "utf-8"));
		
		hpp.write("#ifndef _" + gen.name().toUpperCase() + "_HPP_\n");
		hpp.write("#define _" + gen.name().toUpperCase() + "_HPP_\n");
		hpp.write("\n");
		
		boolean found = false;
		for (Type t : data.types) if (t != null && t.code.startsWith("#")) {
			hpp.write(t.code + "\n");
			found = true;
		}
		if (found) hpp.write("\n");
		
		hpp.write("namespace " + gen.api.toUpperCase() + " {\n");
		hpp.write("\t\n");
		
		for (Type t : data.types) if (t != null && !t.code.startsWith("#")) {
			hpp.write("\t" + t.code.replace("\n", "\n\t") + "\n");
		}
		if (data.types.size() > 0) hpp.write("\t\n");
		
		if (data.enums.size() > 0) hpp.write("\tenum {\n");
		for (Enum e : data.enums) if (e != null) {
			hpp.write("\t\t" + e.name + " = " + e.value + (e.type == null ? "" : e.type) + ",\n");
		}
		if (data.enums.size() > 0) hpp.write("\t};\n\t\n");
		
		for (Command c : data.commands) if (c != null) {
			hpp.write("\textern " + c.proto.code + "(*" + c.proto.name + ")(");
			boolean first = true;
			for (Param p : c.params) {
				if (!first) hpp.write(", ");
				hpp.write(p.code);
				first = false;
			}
			hpp.write(");\n");
		}
		if (data.commands.size() > 0) hpp.write("\t\n");
		
		hpp.write("\ttypedef void* (*GLFunctionLoader)(const char*);\n");
		hpp.write("\textern bool glLoadFunctionsLoader(const GLFunctionLoader &load);\n");
		hpp.write("\ttemplate <class T> inline bool glLoadFunctions(const T &proc) {\n");
		hpp.write("\t\treturn glLoadFunctionsLoader(reinterpret_cast<GLFunctionLoader>(proc));\n");
		hpp.write("\t}\n");
		hpp.write("\t\n");
		
		hpp.write("} // " + gen.api.toUpperCase() + "\n");
		hpp.write("\n");
		hpp.write("#endif // _" + gen.name().toUpperCase() + "_HPP_\n");
		
		hpp.close();
	}
	
	private static void source(Data data, Gen gen) throws IOException {
		System.out.println("Generating " + gen + ".cpp");
		if (!new File("gen").exists()) new File("gen").mkdir();
		BufferedWriter cpp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gen/" + gen.name() + ".cpp"), "utf-8"));
		
		cpp.write("#include \"" + gen.name() + ".hpp\"\n");
		cpp.write("\n");
		cpp.write("namespace " + gen.api.toUpperCase() + " {\n");
		cpp.write("\t\n");
		
		for (Command c : data.commands) if (c != null) {
			cpp.write("\t" + c.proto.code + "(*" + c.proto.name + ")(");
			boolean first = true;
			for (Param p : c.params) {
				if (!first) cpp.write(", ");
				cpp.write(p.code);
				first = false;
			}
			cpp.write(");\n");
		}
		if (data.commands.size() > 0) cpp.write("\t\n");
		
		cpp.write("\ttemplate <class F>\n");
		cpp.write("\tinline int Load(F &f, const GLFunctionLoader &load, const char* name) {\n");
		cpp.write("\t\treturn (f = reinterpret_cast<F>(load(name))) ? 0 : 1;\n");
		cpp.write("\t};\n");
		cpp.write("\t\n");
		cpp.write("\tbool glLoadFunctionsLoader(const GLFunctionLoader &load) {\n");
		cpp.write("\t\tint failed = 0;\n");
		for (Command c : data.commands) if (c != null) {
			cpp.write("\t\tfailed += Load(" + c.proto.name + ", load, \"" + c.proto.name + "\");\n");
		}
		cpp.write("\t\treturn failed == 0;\n");
		cpp.write("\t}\n");
		cpp.write("\t\n");
		
		cpp.write("} // " + gen.api.toUpperCase() + "\n");
		cpp.close();
	}
	
}
