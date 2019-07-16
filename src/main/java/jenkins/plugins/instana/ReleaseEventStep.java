package jenkins.plugins.instana;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;

import jenkins.plugins.instana.util.HttpRequestNameValuePair;

/**
 * @author Martin d'Anjou
 */
public final class ReleaseEventStep extends AbstractStepImpl {

	private @Nonnull
	String releaseName;
	private String releaseStartTimestamp = DescriptorImpl.releaseStartTimestamp;
	private String releaseEndTimestamp = DescriptorImpl.releaseEndTimestamp;

	@DataBoundConstructor
	public ReleaseEventStep(String releaseName) {
		this.releaseName = releaseName;
	}

	@Nonnull
	public String getReleaseName() {
		return releaseName;
	}

	public String getReleaseStartTimestamp() {
		return releaseStartTimestamp;
	}

	@DataBoundSetter
	public void setReleaseStartTimestamp(String releaseStartTimestamp) {
		this.releaseStartTimestamp = releaseStartTimestamp;
	}

	public String getReleaseEndTimestamp() {
		return releaseEndTimestamp;
	}

	@DataBoundSetter
	public void setReleaseEndTimestamp(String releaseEndTimestamp) {
		this.releaseEndTimestamp = releaseEndTimestamp;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	String resolveUrl() {
		return InstanaPluginGlobalConfig.get().getInstanaUrl();
	}

	HttpMode resolveHttpMode() {
		return InstanaPluginGlobalConfig.get().getHttpMode();
	}

	String resolveProxy() {
		return InstanaPluginGlobalConfig.get().getProxy();
	}

	List<HttpRequestNameValuePair> resolveHeaders() {
		final List<HttpRequestNameValuePair> headers = new ArrayList<>();
		headers.add(new HttpRequestNameValuePair("Content-type", "application/json"));
		headers.add(new HttpRequestNameValuePair("Authorization", "apiToken " + InstanaPluginGlobalConfig.get().getToken(), true));
		return headers;
	}

	@Extension
	public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
		public static final String releaseName = ReleaseEvent.DescriptorImpl.releaseName;
		public static final String releaseStartTimestamp = ReleaseEvent.DescriptorImpl.releaseStartTimestamp;
		public static final String releaseEndTimestamp = ReleaseEvent.DescriptorImpl.releaseEndTimestamp;

		public DescriptorImpl() {
			super(Execution.class);
		}

		@Override
		public String getFunctionName() {
			return "releaseEvent";
		}

		@Override
		public String getDisplayName() {
			return "Perform an HTTP Request and return a response object";
		}

	}

	public static final class Execution extends AbstractSynchronousNonBlockingStepExecution<ResponseContentSupplier> {
		@Inject
		private transient ReleaseEventStep step;

		@StepContextParameter
		private transient Run<?, ?> run;
		@StepContextParameter
		private transient TaskListener listener;

		@Override
		protected ResponseContentSupplier run() throws Exception {
			HttpRequestExecution exec = HttpRequestExecution.from(step, listener);

			Launcher launcher = getContext().get(Launcher.class);
			if (launcher != null) {
				return launcher.getChannel().call(exec);
			}

			return exec.call();
		}

		private static final long serialVersionUID = 1L;

		public Item getProject() {
			return run.getParent();
		}
	}
}
