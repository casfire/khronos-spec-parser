package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import parser.registry.*;
import parser.registry.Enum;

public class Loader {
	
	public final String api;
	public final String version;
	public final String profile;
	private final Specification spec;
	
	public Loader(Specification spec, String api, String version, String profile) {
		this.spec    = spec;
		this.api     = api;
		this.version = version.endsWith(".") ? version.substring(0, version.length() - 1) : version;
		this.profile = (profile == null || profile.isEmpty()) ? null : profile;
		if (api     == null || api.isEmpty())     throw new NullPointerException();
		if (version == null || version.isEmpty()) throw new NullPointerException();
	}
	
	public String filename() {
		if (profile == null) {
			return api + "_" + version.replace('.', '_');
		} else {
			return api + "_" + profile + "_" + version.replace('.', '_');
		}
	}
	
	public String filename(String extra) {
		if (profile == null) {
			return api + "_" + extra + "_" + version.replace('.', '_');
		} else {
			return api + "_" + profile + "_" + extra + "_" + version.replace('.', '_');
		}
	}
	
	public Generator create() {
		Feature features[] = spec.registry.features.clone();
		Arrays.sort(features, (a, b) -> a.number.compareTo(b.number));
		Interface iface = new Interface(spec, null);
		for (Feature f : features) {
			for (RInterface r : Stream.of(f.require, f.remove).flatMap(Stream::of).toArray(RInterface[]::new)) {
				if (api.equals(f.api) && (r.profile == null || r.profile.equals(profile)) && version.compareTo(f.number) >= 0) {
					iface.define(f.name != null ? f.name : f.protect);
					iface.push(r);
				}
			}
		}
		return new Generator(iface);
	}
	
	public List<Generator> extensions(Generator source) {
		List<Generator> list = new ArrayList<Generator>();
		for (Extensions exts : spec.registry.extensions) for (Extension ext : exts.extensions) {
			if (!api.matches(ext.supported)) continue;
			if (ext.remove.length > 0) throw new RuntimeException("Extension " + ext.name + " removes interfaces.");
			String protect = ext.name != null ? ext.name : ext.protect;
			Interface iface = new Interface(spec, protect);
			for (Require r : ext.require) iface.push(r);
			boolean add = false;
			if (!add) for (String name : iface.enums) if (!source.iface.containsEnum(name)) {
				add = true;
				break;
			}
			if (!add) for (String name : iface.commands) if (!source.iface.containsCommand(name)) {
				add = true;
				break;
			}
			if (add) {
				Generator gen = new Generator(iface);
				for (Type    t : source.types   ) gen.types.remove(t);
				for (Enum    e : source.enums   ) gen.enums.remove(e);
				for (Command c : source.commands) gen.commands.remove(c);
				list.add(gen);
			}
		}
		return list;
	}
	
	public Map<Type, List<Generator>> extensionTypes(Generator source, List<Generator> extensions) {
		Map<Type, List<Generator>> map = new LinkedHashMap<Type, List<Generator>>();
		for (Generator gen : extensions) {
			for (Type t : gen.types) {
				if (!map.containsKey(t)) map.put(t, new ArrayList<Generator>());
				map.get(t).add(gen);
			}
		}
		return map;
	}
	
}
