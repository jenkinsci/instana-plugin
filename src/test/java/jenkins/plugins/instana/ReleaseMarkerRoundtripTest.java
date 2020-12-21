package jenkins.plugins.instana;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;
import jenkins.plugins.instana.scope.Application;
import jenkins.plugins.instana.scope.ScopedTo;
import jenkins.plugins.instana.scope.Service;

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
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", Collections.singletonList(new Service("testServiceName")), null));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", Collections.singletonList(new Service("testServiceName")), null);
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithApplication() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", null, Collections.singletonList(new Application("testApplicationName"))));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", null, Collections.singletonList(new Application("testApplicationName")));
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithServiceAndApplication() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", Collections.singletonList(new Service("testServiceName")), Collections.singletonList(new Application("testApplicationName"))));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", Collections.singletonList(new Service("testServiceName")), Collections.singletonList(new Application("testApplicationName")));
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithApplicationScopedService() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		Service service = new Service("testServiceName");
		service.setScopedTo(new ScopedTo(Collections.singletonList(new Application("testApplicationName"))));
		project.getBuildersList().add(new ReleaseMarker("testReleaseName", Collections.singletonList(service), null));
		project = jenkins.configRoundtrip(project);

		ReleaseMarker myStepBuilder = new ReleaseMarker("testReleaseName", Collections.singletonList(service), null);
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}
}
