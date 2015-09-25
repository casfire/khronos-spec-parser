package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Api {
	
	public final String api;
	public final List<String> versions;
	public final Set<String>  profiles;
	public final Map<String, String> start;
	
	public Api(String api) {
		this.api = api;
		this.versions = new ArrayList<String>();
		this.profiles = new HashSet<String>();
		this.start    = new HashMap<String, String>();
	}
	
	public void addProfile(String version, String profile) {
		if (version == null) throw new NullPointerException();
		profiles.add(profile);
		if (!start.containsKey(profile) || start.get(profile).compareTo(version) > 0) {
			start.put(profile, version);
		}
	}
	
	public boolean hasProfile(String profile, String version) {
		return start.get(profile).compareTo(version) <= 0;
	}
	
	public void addVersion(String version) {
		versions.add(version);
	}
	
}
