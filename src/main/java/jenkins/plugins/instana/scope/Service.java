package jenkins.plugins.instana.scope;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Service extends AbstractDescribableImpl<Service> {

	private String name;

	@DataBoundConstructor
	public Service(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<Service> {

		@Override
		public String getDisplayName() {
			return "Service";
		}
	}
}
