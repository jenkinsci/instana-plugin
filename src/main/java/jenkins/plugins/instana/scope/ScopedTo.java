package jenkins.plugins.instana.scope;

import java.util.List;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class ScopedTo extends AbstractDescribableImpl<ScopedTo> {

	private List<Application> applications;

	@DataBoundConstructor
	public ScopedTo(List<Application> applications) {
		this.applications = applications;
	}

	public List<Application> getApplications() {
		return applications;
	}

	@Symbol("scopedTo")
	@Extension
	public static final class DescriptorImpl extends Descriptor<ScopedTo> {

		@Override
		public String getDisplayName() {
			return "ScopedTo";
		}
	}

}
