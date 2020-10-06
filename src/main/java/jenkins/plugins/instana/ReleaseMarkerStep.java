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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.TaskListener;
import jenkins.plugins.instana.util.HttpRequestNameValuePair;

public final class ReleaseMarkerStep extends Step {

	private String releaseName;
	private String serviceName = DescriptorImpl.serviceName;
	private String applicationName = DescriptorImpl.applicationName;
	private String releaseStartTimestamp = DescriptorImpl.releaseStartTimestamp;
	private String releaseEndTimestamp = DescriptorImpl.releaseEndTimestamp;

	@DataBoundConstructor
	public ReleaseMarkerStep(String releaseName) {
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

	public String getServiceName() {
		return serviceName;
	}

	@DataBoundSetter
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	@DataBoundSetter
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public StepExecution start(StepContext stepContext) {
		return new ReleaseMarkerStep.Execution(this, stepContext);
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
		headers.add(new HttpRequestNameValuePair("Authorization", "apiToken " + InstanaPluginGlobalConfig.get().getToken().getPlainText(), true));
		return headers;
	}

	@Extension
	public static final class DescriptorImpl extends StepDescriptor {
		public static final String releaseName = ReleaseMarker.DescriptorImpl.releaseName;
		public static final String serviceName = ReleaseMarker.DescriptorImpl.serviceName;
		public static final String applicationName = ReleaseMarker.DescriptorImpl.applicationName;
		public static final String releaseStartTimestamp = ReleaseMarker.DescriptorImpl.releaseStartTimestamp;
		public static final String releaseEndTimestamp = ReleaseMarker.DescriptorImpl.releaseEndTimestamp;

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return Collections.singleton(TaskListener.class);
		}

		@Override
		public String getFunctionName() {
			return "releaseMarker";
		}

		@Override
		public String getDisplayName() {
			return "Perform an HTTP Request and return a response object";
		}

	}

	public static final class Execution extends SynchronousNonBlockingStepExecution<ResponseContentSupplier> {
		@Inject
		private transient ReleaseMarkerStep step;

		protected Execution(ReleaseMarkerStep step, @Nonnull StepContext context) {
			super(context);
			this.step = step;
		}

		@Override
		@SuppressFBWarnings(value="NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
		protected ResponseContentSupplier run() throws Exception {
			validateInputAndSetDefaults();
			HttpRequestExecution exec = HttpRequestExecution.from(step, this.getContext().get(TaskListener.class));

			Launcher launcher = getContext().get(Launcher.class);
			if (launcher != null && launcher.getChannel() != null) {
				final ResponseContentSupplier supplier = launcher.getChannel().call(exec);
				if (supplier == null){
					throw new AbortException("Unexpected ReponseContentSupplier is null");
				}
				return supplier;
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
