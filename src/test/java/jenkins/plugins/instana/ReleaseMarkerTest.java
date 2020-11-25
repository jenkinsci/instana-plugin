package jenkins.plugins.instana;

import static jenkins.plugins.instana.Registers.registerAlways200;
import static jenkins.plugins.instana.Registers.registerReleaseEndpointChecker;
import static jenkins.plugins.instana.Registers.registerTimeout;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.plugins.instana.scope.Application;
import jenkins.plugins.instana.scope.Service;

public class ReleaseMarkerTest extends ReleaseMarkerTestBase {

	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void correctJobDefinitonWithAllParamatersSetTest() throws Exception {
		// Prepare the server
		registerReleaseEndpointChecker("testReleaseName", "123456787689", TEST_API_TOKEN);

		// Prepare ReleaseEvent
		ReleaseMarker releaseMarker = new ReleaseMarker("testReleaseName");
		releaseMarker.setReleaseStartTimestamp("123456787689");
		releaseMarker.setApplications(Arrays.asList(new Application("application1"), new Application("application2")));
		releaseMarker.setServices(Arrays.asList(new Service("service1"), new Service("service2")));

		// Run build
		FreeStyleProject project = j.createFreeStyleProject();
		project.getBuildersList().add(releaseMarker);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatusSuccess(build);
		j.assertLogContains("\"name\":\"testReleaseName\",\"start\":\"123456787689\"", build);
		j.assertLogContains("\"services\":[{\"name\":\"service1\"},{\"name\":\"service2\"}]," +
				"\"applications\":[{\"name\":\"application1\"},{\"name\":\"application2\"}]", build);
		j.assertLogContains("200", build);
	}


	@Test
	public void emptyReleaseNameJobDefinitonWithAllParamatersSetTest() throws Exception {
		// Prepare the server
		registerAlways200();

		// Prepare ReleaseEvent
		ReleaseMarker releaseMarker = new ReleaseMarker("");
		releaseMarker.setReleaseStartTimestamp("");

		// Run build
		FreeStyleProject project = j.createFreeStyleProject();
		project.getBuildersList().add(releaseMarker);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatus(Result.FAILURE,build);
	}

	@Test
	public void timeoutFailsTheBuild() throws Exception {
		// Prepare the server
		registerTimeout();


		// Prepare ReleaseEvent
		ReleaseMarker releaseMarker = new ReleaseMarker("testReleaseName");
		releaseMarker.setReleaseStartTimestamp("123456787689");


		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseMarker);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

}
