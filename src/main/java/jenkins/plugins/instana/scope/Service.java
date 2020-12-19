package jenkins.plugins.instana.scope;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Service extends AbstractDescribableImpl<Service> {

	private String name;

	private ScopedTo scopedTo;

	@DataBoundConstructor
	public Service(String name) {
		this.name = name;
	}

	@DataBoundSetter
	public void setScopedTo(ScopedTo scopedTo) {
		this.scopedTo = scopedTo;
	}

	public String getName() {
		return name;
	}

	public ScopedTo getScopedTo() {
		return scopedTo;
	}

	@Symbol("service")
	@Extension
	public static final class DescriptorImpl extends Descriptor<Service> {

		@Override
		public String getDisplayName() {
			return "Service";
		}
	}
}
