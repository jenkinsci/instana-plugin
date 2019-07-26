package jenkins.plugins.instana;

import static jenkins.plugins.instana.Registers.registerReleaseEndpointChecker;
import static jenkins.plugins.instana.Registers.registerTimeout;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.eclipse.jetty.server.Request;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hudson.model.Cause.UserIdCause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.StringParameterValue;

import jenkins.plugins.instana.auth.FormAuthentication;
import jenkins.plugins.instana.util.HttpRequestNameValuePair;
import jenkins.plugins.instana.util.RequestAction;

/**
 * @author Martin d'Anjou
 */
public class ReleaseEventTest extends ReleaseEventTestBase {

	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void correctJobDefinitonWithAllParamatersSetTest() throws Exception {
		// Prepare the server
		registerReleaseEndpointChecker("testReleaseName", "123456787689", TEST_API_TOKEN);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent("testReleaseName");
		releaseEvent.setReleaseStartTimestamp("123456787689");

		// Run build
		FreeStyleProject project = j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatusSuccess(build);
		j.assertLogContains("{\"name\":\"testReleaseName\",\"start\":\"123456787689\"}", build);
		j.assertLogContains("200", build);
	}

	@Test
	public void timeoutFailsTheBuild() throws Exception {
		// Prepare the server
		registerTimeout();


		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent("testReleaseName");
		releaseEvent.setReleaseStartTimestamp("123456787689");


		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

}
