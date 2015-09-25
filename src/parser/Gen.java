package parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import parser.registry.*;

public class Gen {
	
	public final String api;
	public final String version;
	public final String profile;
	
	public final Set<String> types;
	public final Set<String> enums;
	public final Set<String> commands;
	
	public Gen(String api, String version, String profile) {
		this.api      = api;
		this.version  = version;
		this.profile  = profile;
		this.types    = new HashSet<String>();
		this.enums    = new HashSet<String>();
		this.commands = new HashSet<String>();
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
	
	public void push(Gen a) {
		types.addAll(a.types);
		enums.addAll(a.enums);
		commands.addAll(a.commands);
	}
	
	public String name() {
		if (profile == null || profile.isEmpty()) {
			return api + "_" + version.replace('.', '_');
		} else {
			return api + "_" + profile + "_" + version.replace('.', '_');
		}
	}
	
	@Override public String toString() {
		return name();
	}
	
}
