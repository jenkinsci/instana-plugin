package jenkins.plugins.instana.util;

import java.net.URI;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpBodyDelete extends HttpEntityEnclosingRequestBase {
	public HttpBodyDelete(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	@Override
	public String getMethod() {
		return HttpDelete.METHOD_NAME;
	}
}
