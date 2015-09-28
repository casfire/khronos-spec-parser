package parser;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import parser.registry.*;
import parser.registry.Enum;

public class Generator {
	
	public final List<Type>    types;
	public final List<Enum>    enums;
	public final List<Command> commands;
	public final List<String>  defines;
	public final String        protect;
	public final Interface     iface;
	
	public Generator(Interface Iface) {
		iface     = Iface;
		types     = iface.getTypes();
		enums     = iface.getEnums();
		commands  = iface.getCommands();
		defines   = new ArrayList<String>(iface.defines);
		protect  = iface.protect;
		if (types.contains(null))    throw new NullPointerException();
		if (enums.contains(null))    throw new NullPointerException();
		if (commands.contains(null)) throw new NullPointerException();
		if (defines.contains(null))  throw new NullPointerException();
	}
	
	public boolean hppDefines(Writer out, int indent) throws IOException {
		String ind = new String(new char[indent]).replace('\0', '\t');
		boolean found = false;
		for (Type t : types) if (t.code.startsWith("#")) {
			out.write(ind + t.code + "\n");
			found = true;
		}
		for (String def : defines) {
			out.write(ind + "#define " + def + " 1\n");
			found = true;
		}
		return found;
	}
	
	public boolean hppEnums(Writer out, int indent) throws IOException {
		if (enums.isEmpty()) return false;
		String ind = new String(new char[indent]).replace('\0', '\t');
		if (enums.size() == 1) {
			out.write(ind + "enum { ");
			for (Enum e : enums) {
				out.write(e.name + " = " + e.value + (e.type == null ? "" : e.type) + ", ");
			}
			out.write("};\n");
		} else {
			out.write(ind + "enum {\n");
			for (Enum e : enums) {
				out.write(ind + "\t" + e.name + " = " + e.value + (e.type == null ? "" : e.type) + ",\n");
			}
			out.write(ind + "};\n");
		}
		return true;
	}
	
	public boolean hppTypes(Writer out, int indent) throws IOException {
		String ind = new String(new char[indent]).replace('\0', '\t');
		boolean found = false;
		for (Type t : types) if (!t.code.startsWith("#")) {
			out.write(ind + t.code + "\n");
			found = true;
		}
		return found;
	}
	
	public void hppCommand(Writer out, String indent, Command c) throws IOException {
		out.write(indent + "extern " + c.proto.code + "(*" + c.proto.name + ")(");
		boolean first = true;
		for (Param p : c.params) {
			if (!first) out.write(", ");
			out.write(p.code);
			first = false;
		}
		out.write(");\n");
	}
	
	public boolean hppCommands(Writer out, int indent) throws IOException {
		String ind = new String(new char[indent]).replace('\0', '\t');
		for (Command c : commands) hppCommand(out, ind, c);
		return !commands.isEmpty();
	}
	
	public void cppCommand(Writer out, String indent, Command c) throws IOException {
		out.write(indent + c.proto.code + "(*" + c.proto.name + ")(");
		boolean first = true;
		for (Param p : c.params) {
			if (!first) out.write(", ");
			out.write(p.code);
			first = false;
		}
		out.write(") = nullptr;\n");
	}
	
	public boolean cppCommands(Writer out, int indent) throws IOException {
		String ind = new String(new char[indent]).replace('\0', '\t');
		for (Command c : commands) cppCommand(out, ind, c);
		return !commands.isEmpty();
	}
	
	public boolean protectTop(Writer out, int indent, Collection<String> protect) throws IOException {
		if (protect == null || protect.isEmpty()) return false;
		String ind = new String(new char[indent]).replace('\0', '\t');
		out.write(ind + "#if ");
		boolean f = false;
		for (String p : protect) {
			if (f) out.write(" || ");
			out.write("!defined(" + p + ")");
			f = true;
		}
		out.write("\n");
		return true;
	}
	
	public boolean protectBottom(Writer out, int indent, Collection<String> protect) throws IOException {
		if (protect == null || protect.isEmpty()) return false;
		String ind = new String(new char[indent]).replace('\0', '\t');
		out.write(ind + "#endif\n");
		return true;
	}
	
	public static void notice(Writer out) throws IOException {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
		out.write("/* Generated " + date + " by Casfire Khronos Specification Parser - admin@casfire.com */\n");
	}
	
}
