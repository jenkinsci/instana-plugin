package jenkins.plugins.instana;

import static jenkins.plugins.instana.Registers.registerAlways200;
import static jenkins.plugins.instana.Registers.registerReleaseEndpointChecker;
import static jenkins.plugins.instana.Registers.registerFailedAuthEndpoint;
import static jenkins.plugins.instana.Registers.registerTimeout;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * @author Martin d'Anjou
 */
public class ReleaseEventStepTest extends ReleaseEventTestBase {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void correctStepDefinitonWithAllParamatersSetTest() throws Exception {
		// Prepare the server
		registerReleaseEndpointChecker("testReleaseName", "123456787689", TEST_API_TOKEN);

		// Configure the build
		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseEvent releaseName: 'testReleaseName', releaseStartTimestamp: '123456787689' \n" +
						"println('Status: '+response.status)\n" +
						"println('Response: '+response.content)\n",
				true));

		// Execute the build
		WorkflowRun run = proj.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatusSuccess(run);
		j.assertLogContains("Status: 200", run);
	}

	@Test
	public void wrongStepDefinitonWithAllParamatersSetTest() throws Exception {
		// Prepare the server
		registerAlways200();

		// Configure the build
		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseEvent foo: 'testReleaseName', bar: '123456787689' \n" +
						"println('Status: '+response.status)\n" +
						"println('Response: '+response.content)\n",
				true));

		// Execute the build
		WorkflowRun run = proj.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatus(Result.FAILURE,run);
	}

	@Test
	public void failIfAPITokenIsNotConfigured() throws Exception {
		// Prepare the server
		registerFailedAuthEndpoint("testReleaseName", "123456787689", TEST_API_TOKEN);
		// Configure the build
		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseEvent releaseName: 'testReleaseName', releaseStartTimestamp: '123456787689' \n" +
						"println('Status: '+response.status)\n" +
						"println('Response: '+response.content)\n",
				true));

		// Execute the build
		WorkflowRun run = proj.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatus(Result.FAILURE,run);
		j.assertLogContains("Unauthorized request", run);
	}

	@Test
	public void timeoutFailsTheBuild() throws Exception {
		// Prepare the server
		registerTimeout();


		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseEvent releaseName: 'testReleaseName', releaseStartTimestamp: '123456787689' \n" +
						"println('Status: '+response.status)\n" +
						"println('Response: '+response.content)\n",
				true));

		// Execute the build
		WorkflowRun run = proj.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, run);
	}
}
