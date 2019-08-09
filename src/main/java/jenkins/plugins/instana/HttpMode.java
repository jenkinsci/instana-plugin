package jenkins.plugins.instana;

import hudson.util.ListBoxModel;

public enum HttpMode {
	GET,
	HEAD,
	POST,
	PUT,
	DELETE,
	OPTIONS,
	PATCH;

	public static ListBoxModel getFillItems() {
		ListBoxModel items = new ListBoxModel();
		for (HttpMode httpMode : values()) {
			items.add(httpMode.name());
		}
		return items;
	}
}
