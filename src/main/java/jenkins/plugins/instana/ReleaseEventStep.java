package jenkins.plugins.instana;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.TaskListener;

import jenkins.plugins.instana.util.HttpRequestNameValuePair;

/**
 * @author Martin d'Anjou
 */
public final class ReleaseEventStep extends Step {

	private String releaseName;
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
	public StepExecution start(StepContext stepContext) {
		return new ReleaseEventStep.Execution(this, stepContext);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	String resolveUrl() {
		return InstanaPluginGlobalConfig.get().getInstanaUrl() + InstanaPluginGlobalConfig.RELEASES_API;
	}

	HttpMode resolveHttpMode() {
		return InstanaPluginGlobalConfig.get().getHttpMode();
	}

	List<HttpRequestNameValuePair> resolveHeaders() {
		final List<HttpRequestNameValuePair> headers = new ArrayList<>();
		headers.add(new HttpRequestNameValuePair("Content-type", "application/json"));
		headers.add(new HttpRequestNameValuePair("Authorization", "apiToken " + InstanaPluginGlobalConfig.get().getToken(), true));
		return headers;
	}

	@Extension
	public static final class DescriptorImpl extends StepDescriptor {
		public static final String releaseName = ReleaseEvent.DescriptorImpl.releaseName;
		public static final String releaseStartTimestamp = ReleaseEvent.DescriptorImpl.releaseStartTimestamp;
		public static final String releaseEndTimestamp = ReleaseEvent.DescriptorImpl.releaseEndTimestamp;

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return Collections.singleton(TaskListener.class);
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

	public static final class Execution extends SynchronousNonBlockingStepExecution<ResponseContentSupplier> {
		@Inject
		private transient ReleaseEventStep step;

		protected Execution(ReleaseEventStep step, @Nonnull StepContext context) {
			super(context);
			this.step = step;
		}

		@Override
		protected ResponseContentSupplier run() throws Exception {
			validateInputAndSetDefaults();
			HttpRequestExecution exec = HttpRequestExecution.from(step, this.getContext().get(TaskListener.class));

			Launcher launcher = getContext().get(Launcher.class);
			if (launcher != null && launcher.getChannel() != null) {
				return launcher.getChannel().call(exec);
			}

			return exec.call();
		}

		private void validateInputAndSetDefaults() throws AbortException {
			if (step.getReleaseName().trim().isEmpty()) {
				throw new AbortException("Release name must not be empty");
			}
			if (step.getReleaseStartTimestamp().trim().isEmpty()) {
				step.setReleaseStartTimestamp(String.valueOf(Instant.now().toEpochMilli()));
			}
		}

		private static final long serialVersionUID = 1L;
	}
}
