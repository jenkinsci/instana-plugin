package jenkins.plugins.instana;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.groovy.util.StringUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Items;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import hudson.util.FormValidation;
import jenkins.plugins.instana.util.HttpRequestNameValuePair;

/**
 * @author Janario Oliveira
 */
public class ReleaseEvent extends Builder {


	private @Nonnull String releaseName;
	private String releaseStartTimestamp = DescriptorImpl.releaseStartTimestamp;
	private String releaseEndTimestamp = DescriptorImpl.releaseEndTimestamp;

	@DataBoundConstructor
	public ReleaseEvent(@Nonnull String releaseName) {
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

	@Initializer(before = InitMilestone.PLUGINS_STARTED)
	public static void xStreamCompatibility() {
		Items.XSTREAM2.aliasField("logResponseBody", ReleaseEvent.class, "consoleLogResponseBody");
		Items.XSTREAM2.aliasField("consoleLogResponseBody", ReleaseEvent.class, "consoleLogResponseBody");
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
		headers.add(new HttpRequestNameValuePair("Authorization", "apiToken " + InstanaPluginGlobalConfig.get().getToken(), true));
		return headers;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		if(releaseName.trim().isEmpty())
		{
			throw new AbortException("Release name must not be empty");
		}
		if(releaseStartTimestamp.trim().isEmpty())
		{
			releaseStartTimestamp = String.valueOf(Instant.now().toEpochMilli());
		}
		HttpRequestExecution exec = HttpRequestExecution.from(this, listener);
		launcher.getChannel().call(exec);

		return true;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public static final String releaseName = "";
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
			return "Ingest release into Instana";
		}

		public FormValidation doCheckReleaseName(@QueryParameter String value) {
			if(value != null && !value.trim().isEmpty()) {
				return FormValidation.ok();
			}
			else {
				return FormValidation.error("Field ist Mandatory");
			}
		}
	}

}
