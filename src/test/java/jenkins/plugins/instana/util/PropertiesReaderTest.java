package jenkins.plugins.instana.util;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class PropertiesReaderTest {

	@Test
	public void shouldReadPluginVersion() {
		PropertiesReader propertiesReader = new PropertiesReader();
		String pluginVersion = propertiesReader.getProperty("plugin.version", "fallback");
		assertNotEquals("fallback", pluginVersion);
	}

}