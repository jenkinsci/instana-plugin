package jenkins.plugins.instana;

import static jenkins.plugins.instana.Registers.registerAlways200;
import static jenkins.plugins.instana.Registers.registerFailedAuthEndpoint;
import static jenkins.plugins.instana.Registers.registerReleaseEndpointChecker;
import static jenkins.plugins.instana.Registers.registerTimeout;

import java.util.Arrays;
import java.util.Collections;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hudson.model.Result;
import jenkins.plugins.instana.scope.Application;
import jenkins.plugins.instana.scope.ScopedTo;
import jenkins.plugins.instana.scope.Service;


public class ReleaseMarkerStepTest extends ReleaseMarkerTestBase {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void correctStepDefinitonWithParametersSetTest() throws Exception {
		// Prepare the server
		registerReleaseEndpointChecker("testReleaseName", "123456787689", TEST_API_TOKEN);

		// Configure the build
		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseMarker releaseName: 'testReleaseName', releaseStartTimestamp: '123456787689' \n" +
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
	public void correctStepDefinitonWithAllParametersSetTest() throws Exception {
		Service scopedService = new Service("testServiceName3");
		scopedService.setScopedTo(new ScopedTo(Collections.singletonList(new Application("testApplication1"))));
		// Prepare the server
		registerReleaseEndpointChecker("testReleaseName",
				Arrays.asList(new Service("testServiceName1"), new Service("testServiceName2"), scopedService),
				Arrays.asList(new Application("testApplicationName1"), new Application("testApplicationName2")),
				"123456787689", TEST_API_TOKEN);

		// Configure the build
		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseMarker releaseName: 'testReleaseName', releaseStartTimestamp: '123456787689', " +
						"services: [service(name:'testServiceName1'), service(name:'testServiceName2'), " +
						"	service(name:'testServiceName3', scopedTo:scopedTo(applications:[application('testApplicationName1')]))], " +
						"applications: [application('testApplicationName1'), application('testApplicationName2')] \n" +
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
				"def response = releaseMarker foo: 'testReleaseName', bar: '123456787689' \n" +
						"println('Status: '+response.status)\n" +
						"println('Response: '+response.content)\n",
				true));

		// Execute the build
		WorkflowRun run = proj.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatus(Result.FAILURE, run);
	}

	@Test
	public void failIfAPITokenIsNotConfigured() throws Exception {
		// Prepare the server
		registerFailedAuthEndpoint("testReleaseName", "123456787689", TEST_API_TOKEN);
		// Configure the build
		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseMarker releaseName: 'testReleaseName', releaseStartTimestamp: '123456787689' \n" +
						"println('Status: '+response.status)\n" +
						"println('Response: '+response.content)\n",
				true));

		// Execute the build
		WorkflowRun run = proj.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatus(Result.FAILURE, run);
		j.assertLogContains("Unauthorized request", run);
	}

	@Test
	public void timeoutFailsTheBuild() throws Exception {
		// Prepare the server
		registerTimeout();


		WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
		proj.setDefinition(new CpsFlowDefinition(
				"def response = releaseMarker releaseName: 'testReleaseName', releaseStartTimestamp: '123456787689' \n" +
						"println('Status: '+response.status)\n" +
						"println('Response: '+response.content)\n",
				true));

		// Execute the build
		WorkflowRun run = proj.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, run);
	}
}
