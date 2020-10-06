package jenkins.plugins.instana;

import java.util.Collections;

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
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", Collections.singletonList("testServiceName"), null));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", Collections.singletonList("testServiceName"), null);
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithApplication() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", null, Collections.singletonList("testApplicationName")));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", null, Collections.singletonList("testApplicationName"));
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithServiceAndApplication() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", Collections.singletonList("testServiceName"), Collections.singletonList("testApplicationName")));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", Collections.singletonList("testServiceName"), Collections.singletonList("testApplicationName"));
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}
}
