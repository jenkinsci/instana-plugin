package jenkins.plugins.instana;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;

public class ReleaseMarkerRoundtripTest {

	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	@Test
	public void testConfigRoundtrip() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName"));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName");
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithService() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", "testServiceName", null));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", "testServiceName", null);
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithApplication() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", null, "testApplicationName"));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", null, "testApplicationName");
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithServiceAndApplication() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", "testServiceName", "testApplicationName"));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", "testServiceName", "testApplicationName");
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}
}
