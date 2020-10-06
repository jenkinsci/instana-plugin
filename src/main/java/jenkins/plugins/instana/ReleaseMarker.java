package jenkins.plugins.instana;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Items;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.plugins.instana.util.HttpRequestNameValuePair;

public class ReleaseMarker extends Builder {


	private @Nonnull
	String releaseName;
	private String serviceName = DescriptorImpl.serviceName;
	private String applicationName = DescriptorImpl.applicationName;
	private String releaseStartTimestamp = DescriptorImpl.releaseStartTimestamp;
	private String releaseEndTimestamp = DescriptorImpl.releaseEndTimestamp;

	@DataBoundConstructor
	public ReleaseMarker(@Nonnull String releaseName) {
		this.releaseName = releaseName;
	}

	public ReleaseMarker(@Nonnull String releaseName, @Nullable String serviceName,
						 @Nullable String applicationName) {
		this.releaseName = releaseName;
		if (serviceName != null) {
			this.serviceName = serviceName;
		}
		if (applicationName != null) {
			this.applicationName = applicationName;
		}
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

	@Initializer(before = InitMilestone.PLUGINS_STARTED)
	public static void xStreamCompatibility() {
		Items.XSTREAM2.aliasField("logResponseBody", ReleaseMarker.class, "consoleLogResponseBody");
		Items.XSTREAM2.aliasField("consoleLogResponseBody", ReleaseMarker.class, "consoleLogResponseBody");
		Items.XSTREAM2.alias("pair", HttpRequestNameValuePair.class);
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

	@Override
	@SuppressFBWarnings(value="NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		if (releaseName.trim().isEmpty()) {
			throw new AbortException("Release name must not be empty");
		}
		if (releaseStartTimestamp.trim().isEmpty()) {
			releaseStartTimestamp = String.valueOf(Instant.now().toEpochMilli());
		}
		HttpRequestExecution exec = HttpRequestExecution.from(this, listener);
		if (launcher != null && launcher.getChannel() != null) {
			launcher.getChannel().call(exec);
			return true;
		} else {
			return false;
		}
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public static final String releaseName = "";
		public static final String serviceName = "";
		public static final String applicationName = "";
		public static final String releaseStartTimestamp = "";
		public static final String releaseEndTimestamp = "";

		public DescriptorImpl() {
			load();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Mark release in Instana";
		}

		public FormValidation doCheckReleaseName(@QueryParameter String value) {
			if (value != null && !value.trim().isEmpty()) {
				return FormValidation.ok();
			} else {
				return FormValidation.error("Field is Mandatory");
			}
		}
	}

}
