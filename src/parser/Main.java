package parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import parser.registry.Command;
import parser.registry.Enum;
import parser.registry.Type;

public class Main {
	
	public static void main(String args[]) throws Exception {
		System.out.println("Casfire Khronos XML API Registry parser.");
		
		if (!new File("gen").exists()) {
			System.out.println("Creating gen/ directory.");
			new File("gen").mkdir();
		}
		
		if (!new File("spec").exists()) {
			System.out.println("Creating spec/ directory.");
			new File("spec").mkdir();
		}
		
		generate(new Specification("gl"));
		generate(new Specification("egl"));
		generate(new Specification("glx"));
		generate(new Specification("wgl"));
		
		System.out.println("Done.");
	}
	
	private static void generate(Specification spec) throws IOException {
		for (Loader loader : spec.loaders()) {
			Generator gen = loader.create();
			List<Generator> exts = loader.extensions(gen);
			src_hpp(loader.filename(), loader.api, gen);
			src_cpp(loader.filename(), loader.api, gen);
			ext_hpp(loader.filename("ext"), loader.filename(), loader.api, exts, loader.extensionTypes(gen, exts));
			ext_cpp(loader.filename("ext"), loader.api, exts);
		}
	}
	
	private static void src_hpp(String filename, String namespace, Generator gen) throws IOException {
		System.out.println("Generating gen/" + filename + ".hpp");
		BufferedWriter hpp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gen/" + filename + ".hpp"), "utf-8"));
		
		String ns = namespace.toLowerCase();
		String NS = namespace.toUpperCase();
		
		Generator.notice(hpp);
		hpp.write("#ifndef _" + filename.toUpperCase() + "_HPP_\n");
		hpp.write("#define _" + filename.toUpperCase() + "_HPP_\n");
		hpp.write("\n");
		
		if (gen.hppDefines(hpp, 0)) hpp.write("\n");
		
		hpp.write("namespace " + NS + " {\n");
		hpp.write("\t\n");
		
		if (gen.hppTypes   (hpp, 1)) hpp.write("\t\n");
		if (gen.hppEnums   (hpp, 1)) hpp.write("\t\n");
		if (gen.hppCommands(hpp, 1)) hpp.write("\t\n");
		
		hpp.write("\ttypedef void* (*" + NS + "FunctionLoader)(const char*);\n");
		hpp.write("\textern bool " + ns + "LoadFunctions(const " + NS + "FunctionLoader &load);\n");
		hpp.write("\textern bool " + ns + "LoadFunctions();\n");
		hpp.write("\ttemplate <typename P> inline bool " + ns + "LoadFunctions(const P &proc) {\n");
		hpp.write("\t\treturn " + ns + "LoadFunctions(reinterpret_cast<" + NS + "FunctionLoader>(proc));\n");
		hpp.write("\t}\n");
		hpp.write("\t\n");
		
		hpp.write("} // " + NS + "\n");
		hpp.write("\n");
		hpp.write("#endif // _" + filename.toUpperCase() + "_HPP_\n");
		hpp.close();
	}
	
	private static void src_cpp(String filename, String namespace, Generator gen) throws IOException {
		System.out.println("Generating gen/" + filename + ".cpp");
		BufferedWriter cpp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gen/" + filename + ".cpp"), "utf-8"));
		
		String ns = namespace.toLowerCase();
		String NS = namespace.toUpperCase();
		
		Generator.notice(cpp);
		cpp.write("#include \"" + filename + ".hpp\"\n");
		cpp.write("\n");
		Generator.platformDefines(cpp);
		cpp.write("namespace " + NS + " {\n");
		cpp.write("\t\n");
		
		if (gen.cppCommands(cpp, 1)) cpp.write("\t\n");
		
		Generator.platformLoaderSrc(cpp, NS, ns);
		
		cpp.write("\ttemplate <typename Fn>\n");
		cpp.write("\tinline int Load(Fn &f, const " + NS + "FunctionLoader &load, const char* name) {\n");
		cpp.write("\t\treturn (f = reinterpret_cast<Fn>(load(name))) ? 0 : 1;\n");
		cpp.write("\t};\n");
		cpp.write("\t\n");
		
		cpp.write("\tbool " + ns + "LoadFunctions(const " + NS + "FunctionLoader &load) {\n");
		cpp.write("\t\tint failed = 0;\n");
		for (Command c : gen.commands) {
			cpp.write("\t\tfailed += Load(" + c.proto.name + ", load, \"" + c.proto.name + "\");\n");
		}
		cpp.write("\t\treturn failed == 0;\n");
		cpp.write("\t}\n");
		cpp.write("\t\n");
		
		cpp.write("} // " + NS + "\n");
		cpp.close();
	}
	
	private static void ext_hpp(String filename, String include, String namespace, List<Generator> extensions, Map<Type, List<Generator>> types) throws IOException {
		System.out.println("Generating gen/" + filename + ".hpp");
		BufferedWriter hpp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gen/" + filename + ".hpp"), "utf-8"));
		
		String ns = namespace.toLowerCase();
		String NS = namespace.toUpperCase();
		
		Generator.notice(hpp);
		hpp.write("#ifndef _" + filename.toUpperCase() + "_HPP_\n");
		hpp.write("#define _" + filename.toUpperCase() + "_HPP_\n");
		hpp.write("\n");
		
		hpp.write("#include \"" + include + ".hpp\"\n\n");
		
		hpp.write("// Uncomment to enable\n");
		for (Generator gen : extensions) {
			hpp.write("//#define " + gen.protect + " 1\n");
		}
		hpp.write("\n");
		
		hpp.write("namespace " + NS + " {\n");
		hpp.write("\t\n");
		
		boolean found = false;
		for (Entry<Type, List<Generator>> e : types.entrySet()) {
			hpp.write("\t#if ");
			boolean f = false;
			for (Generator gen : e.getValue()) {
				if (f) hpp.write(" || ");
				hpp.write("defined(" + gen.protect + ")");
				f = true;
			}
			hpp.write("\n");
			hpp.write("\t\t" + e.getKey().code.replace("\n", "\n\t\t") + "\n");
			hpp.write("\t#endif\n");
			found = true;
		}
		if (found) hpp.write("\t\n");
		
		Map<Enum,    Set<String>> eProtect = new HashMap<Enum,    Set<String>>();
		Map<Command, Set<String>> cProtect = new HashMap<Command, Set<String>>();
		Set<String> last;
		
		for (Generator gen : extensions) {
			hpp.write("\t#ifdef " + gen.protect + "\n");
			hpp.write("\textern bool ext_" + gen.protect + ";\n");
			
			last = Collections.emptySet();
			if (!gen.enums.isEmpty()) hpp.write("\tenum {\n");
			for (Enum e : gen.enums) {
				Set<String> curr = eProtect.get(e);
				if (curr == null) eProtect.put(e, curr = new LinkedHashSet<String>());
				if (!last.equals(curr)) {
					gen.protectBottom(hpp, 2, last);
					gen.protectTop(hpp, 2, curr);
					last = new LinkedHashSet<String>(curr);
				}
				hpp.write("\t\t" + e.name + " = " + e.value + (e.type == null ? "" : e.type) + ",\n");
				curr.add(gen.protect);
			}
			gen.protectBottom(hpp, 2, last);
			if (!gen.enums.isEmpty()) hpp.write("\t};\n");
			
			last = Collections.emptySet();
			for (Command c : gen.commands) {
				Set<String> curr = cProtect.get(c);
				if (curr == null) cProtect.put(c, curr = new LinkedHashSet<String>());
				if (!last.equals(curr)) {
					gen.protectBottom(hpp, 1, last);
					gen.protectTop(hpp, 1, curr);
					last = new LinkedHashSet<String>(curr);
				}
				gen.hppCommand(hpp, "\t", c);
				curr.add(gen.protect);
			}
			gen.protectBottom(hpp, 1, last);
			
			hpp.write("\t#endif // " + gen.protect + "\n");
			hpp.write("\t\n");
		}
		
		hpp.write("\textern void " + ns + "LoadExtensions(const " + NS + "FunctionLoader &load);\n");
		hpp.write("\textern void " + ns + "LoadExtensions();\n");
		hpp.write("\ttemplate <typename P> inline void " + ns + "LoadExtensions(const P &proc) {\n");
		hpp.write("\t\t" + ns + "LoadExtensions(reinterpret_cast<" + NS + "FunctionLoader>(proc));\n");
		hpp.write("\t}\n");
		hpp.write("\t\n");
		
		hpp.write("} // " + NS + "\n");
		hpp.write("\n");
		hpp.write("#endif // _" + filename.toUpperCase() + "_HPP_\n");
		hpp.close();
	}
	
	private static void ext_cpp(String filename, String namespace, List<Generator> extensions) throws IOException {
		System.out.println("Generating gen/" + filename + ".cpp");
		BufferedWriter cpp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gen/" + filename + ".cpp"), "utf-8"));
		
		String ns = namespace.toLowerCase();
		String NS = namespace.toUpperCase();
		
		Generator.notice(cpp);
		cpp.write("#include \"" + filename + ".hpp\"\n");
		cpp.write("\n");
		Generator.platformDefines(cpp);
		cpp.write("namespace " + NS + " {\n");
		cpp.write("\t\n");
		
		Map<Command, Set<String>> cProtect = new HashMap<Command, Set<String>>();
		
		for (Generator gen : extensions) {
			cpp.write("\t#ifdef " + gen.protect + "\n");
			cpp.write("\tbool ext_" + gen.protect + " = false;\n");
			
			Set<String> last = Collections.emptySet();
			for (Command c : gen.commands) {
				Set<String> curr = cProtect.get(c);
				if (curr == null) cProtect.put(c, curr = new LinkedHashSet<String>());
				if (!last.equals(curr)) {
					gen.protectBottom(cpp, 1, last);
					gen.protectTop(cpp, 1, curr);
					last = new LinkedHashSet<String>(curr);
				}
				gen.cppCommand(cpp, "\t", c);
				curr.add(gen.protect);
			}
			gen.protectBottom(cpp, 1, last);
			cpp.write("\t#endif // " + gen.protect + "\n");
		}
		cpp.write("\t\n");
		
		Generator.platformLoaderExt(cpp, NS, ns);
		
		cpp.write("\ttemplate <typename Fn>\n");
		cpp.write("\tinline bool Load(Fn &f, const " + NS + "FunctionLoader &load, const char* name) {\n");
		cpp.write("\t\treturn (f = reinterpret_cast<Fn>(load(name)));\n");
		cpp.write("\t};\n");
		cpp.write("\t\n");
		
		cpp.write("\t#ifdef GL_VERSION_3_0\n");
		cpp.write("\tbool Check(const char* name, const GLubyte** exts, GLint count) {\n");
		cpp.write("\t\tfor (GLint j, i = 0; i < count; i++) {\n");
		cpp.write("\t\t\tfor (j = 0; exts[i][j] != '\\0' && name[j] != '\\0' && name[j] == exts[i][j]; j++);\n");
		cpp.write("\t\t\tif (name[j] == '\\0') return true;\n");
		cpp.write("\t\t}\n");
		cpp.write("\t\treturn false;\n");
		cpp.write("\t}\n");
		cpp.write("\t#else\n");
		cpp.write("\tbool Check(const char* name, const GLubyte* exts, GLint) {\n");
		cpp.write("\t\tfor (GLint j, i = 0; exts[i] != '\\0'; i++) {\n");
		cpp.write("\t\t\tfor (j = 0; name[j] != '\\0' && exts[i + j] != '\\0' && name[j] == exts[i + j]; j++);\n");
		cpp.write("\t\t\tif (name[j] == '\\0') return true;\n");
		cpp.write("\t\t}\n");
		cpp.write("\t\treturn false;\n");
		cpp.write("\t}\n");
		cpp.write("\t#endif\n");
		cpp.write("\t\n");
		
		cpp.write("\tvoid " + ns + "LoadExtensions(const " + NS + "FunctionLoader &load) {\n");
		cpp.write("\t\t#ifdef GL_VERSION_3_0\n");
		cpp.write("\t\t\tGLint count = 0;\n");
		cpp.write("\t\t\tglGetIntegerv(GL_NUM_EXTENSIONS, &count);\n");
		cpp.write("\t\t\tconst GLubyte** exts = count > 0 ? new const GLubyte*[count] : nullptr;\n");
		cpp.write("\t\t\tfor (GLint i = 0; i < count; i++) exts[i] = glGetStringi(GL_EXTENSIONS, i);\n");
		cpp.write("\t\t#else\n");
		cpp.write("\t\t\tconst GLint count = 0;\n");
		cpp.write("\t\t\tconst GLubyte* exts = glGetString(GL_EXTENSIONS);\n");
		cpp.write("\t\t#endif\n");
		for (Generator gen : extensions) {
			cpp.write("\t\t#ifdef " + gen.protect + "\n");
			cpp.write("\t\text_" + gen.protect + " = Check(\"" + gen.protect + "\", exts, count);\n");
			for (Command c : gen.commands) {
				cpp.write("\t\tif (!Load(" + c.proto.name + ", load, \"" + c.proto.name + "\")) ext_" + gen.protect + " = false;\n");
			}
			cpp.write("\t\t#endif // " + gen.protect + "\n");
		}
		cpp.write("\t\t#ifdef GL_VERSION_3_0\n");
		cpp.write("\t\tdelete[] exts;\n");
		cpp.write("\t\t#endif\n");
		cpp.write("\t}\n");
		cpp.write("\t\n");
		
		cpp.write("} // " + NS + "\n");
		cpp.close();
	}
	
}
