package jenkins.plugins.instana;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;

public class ReleaseEventRoundtripTest {

	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	@Test
	public void testConfigRoundtrip() throws Exception
	{
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new ReleaseEvent("testReleaseName"));
		project = jenkins.configRoundtrip(project);

		ReleaseEvent myStepBuilder = new ReleaseEvent("testReleaseName");
		jenkins.assertEqualDataBoundBeans(myStepBuilder, project.getBuildersList().get(0));
	}
}
