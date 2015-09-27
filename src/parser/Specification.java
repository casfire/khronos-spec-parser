package parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import parser.registry.*;
import parser.registry.Enum;

public class Specification {
	
	public final String   name;
	public final File     file;
	public final Document document;
	public final Registry registry;
	
	public Specification(String spec) throws ParserConfigurationException, SAXException, IOException {
		name = spec;
		file = new File("spec/" + name + ".xml");
		if (file.exists()) {
			System.out.println("spec/" + spec + ".xml already exists.");
		} else {
			System.out.println("Downloading " + spec + ".xml to spec/.");
			download(spec, file);
		}
		document = load(file);
		registry = new Registry(document.getDocumentElement());
	}
	
	private static Document load(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(file);
	}
	
	private static void download(String spec, File file) throws IOException {
		URL website = new URL("https://cvs.khronos.org/svn/repos/ogl/trunk/doc/registry/public/api/" + spec + ".xml");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(file);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
	
	private class Api {
		final List<String> versions     = new ArrayList<String>();
		final Set<String>  profiles     = new HashSet<String>();
		final Map<String, String> start = new HashMap<String, String>();
	}
	
	public List<Loader> loaders() {
		Map<String, Api> apis = new HashMap<String, Api>();
		for (Feature f : registry.features) {
			if (f.api    == null || f.api.isEmpty())    throw new NullPointerException();
			if (f.number == null || f.number.isEmpty()) throw new NullPointerException();
			Api api = apis.get(f.api);
			if (api == null) apis.put(f.api, api = new Api());
			api.versions.add(f.number);
			for (RInterface r : Stream.of(f.require, f.remove).flatMap(Stream::of).toArray(RInterface[]::new)) {
				api.profiles.add(r.profile);
				if (!api.start.containsKey(r.profile) || api.start.get(r.profile).compareTo(f.number) > 0) {
					api.start.put(r.profile, f.number);
				}
			}
		}
		List<Loader> list = new ArrayList<Loader>();
		for (Entry<String, Api> e : apis.entrySet()) {
			for (String version : e.getValue().versions) {
				for (String profile : e.getValue().profiles) {
					if (e.getValue().start.get(profile).compareTo(version) > 0) continue;
					list.add(new Loader(this, e.getKey(), version, profile));
				}
			}
		}
		return list;
	}
	
	public Command findCommand(String name) {
		if (name == null) return null;
		for (Commands cmds : registry.commands) for (Command c : cmds.commands) {
			if (c.proto.name.equals(name)) return c;
		}
		System.out.println("WARNING: Command " + name + " not found.");
		return null;
	}
	
	public Type findType(String name) {
		if (name == null) return null;;
		for (Types types : registry.types) for (Type t : types.types) {
			if (t.name.equals(name)) return t;
		}
		System.out.println("WARNING: Type " + name + " not found.");
		return null;
	}
	
	public Enum findEnum(String name) {
		if (name == null) return null;
		for (Enums enums : registry.enums) for (Enum e : enums.enums) {
			if (e.name.equals(name)) return e;
		}
		System.out.println("WARNING: Enum " + name + " not found.");
		return null;
	}
	
}
