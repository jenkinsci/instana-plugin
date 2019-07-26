package jenkins.plugins.instana;

import static jenkins.plugins.instana.Registers.registerReleaseEndpointChecker;
import static jenkins.plugins.instana.Registers.registerTimeout;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

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
