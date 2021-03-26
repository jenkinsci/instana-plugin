package jenkins.plugins.instana.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

	private final Properties properties;

	public PropertiesReader() {
		this.properties = new Properties();
		try {
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream("plugin.properties");
			this.properties.load(is);
		} catch (IOException e) {
			System.err.println("Error getting properties. " + e);
		}
	}

	public String getProperty(String propertyName, String defaultValue) {
		return this.properties.getProperty(propertyName, defaultValue);
	}
}
