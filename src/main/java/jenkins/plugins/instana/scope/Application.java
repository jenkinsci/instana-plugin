package jenkins.plugins.instana.scope;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Application extends AbstractDescribableImpl<Application> {

	private String name;

	@DataBoundConstructor
	public Application(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Symbol("application")
	@Extension
	public static final class DescriptorImpl extends Descriptor<Application> {

		@Override
		public String getDisplayName() {
			return "Application";
		}
	}
}
