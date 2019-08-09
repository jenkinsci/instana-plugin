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
}
