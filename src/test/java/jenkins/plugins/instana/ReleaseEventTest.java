package jenkins.plugins.instana;

import static jenkins.plugins.instana.Registers.registerFileUpload;

import static jenkins.plugins.instana.Registers.registerAcceptedTypeRequestChecker;
import static jenkins.plugins.instana.Registers.registerBasicAuth;
import static jenkins.plugins.instana.Registers.registerCheckBuildParameters;
import static jenkins.plugins.instana.Registers.registerCheckRequestBody;
import static jenkins.plugins.instana.Registers.registerCheckRequestBodyWithTag;
import static jenkins.plugins.instana.Registers.registerContentTypeRequestChecker;
import static jenkins.plugins.instana.Registers.registerCustomHeaders;
import static jenkins.plugins.instana.Registers.registerCustomHeadersResolved;
import static jenkins.plugins.instana.Registers.registerFormAuth;
import static jenkins.plugins.instana.Registers.registerFormAuthBad;
import static jenkins.plugins.instana.Registers.registerInvalidStatusCode;
import static jenkins.plugins.instana.Registers.registerReqAction;
import static jenkins.plugins.instana.Registers.registerRequestChecker;
import static jenkins.plugins.instana.Registers.registerTimeout;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.eclipse.jetty.server.Request;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hudson.model.Cause.UserIdCause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.StringParameterValue;

import jenkins.plugins.instana.auth.FormAuthentication;
import jenkins.plugins.instana.util.HttpRequestNameValuePair;
import jenkins.plugins.instana.util.RequestAction;

/**
 * @author Martin d'Anjou
 */
public class ReleaseEventTest extends ReleaseEventTestBase {

	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void simpleGetTest() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void quietTest() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setQuiet(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogNotContains("HttpMethod:", build);
		this.j.assertLogNotContains("URL:", build);
		this.j.assertLogNotContains("Sending request to url:", build);
		this.j.assertLogNotContains("Response Code:", build);
	}

	@Test
	public void canDetectActualContent() throws Exception {
		// Setup the expected pattern
		String findMe = ALL_IS_WELL;

		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);
		releaseEvent.setValidResponseContent(findMe);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(findMe, build);
	}

	@Test
	public void badContentFailsTheBuild() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);
		releaseEvent.setValidResponseContent("bad content");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
		String s = FileUtils.readFileToString(build.getLogFile());
		assertTrue(s.contains("Fail: Response doesn't contain expected content 'bad content'"));
	}

	@Test
	public void responseMatchAcceptedMimeType() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);

		// Expect a mime type that matches the response
		releaseEvent.setAcceptType(MimeType.TEXT_PLAIN);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void responseDoesNotMatchAcceptedMimeTypeDoesNotFailTheBuild() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);

		// Expect a mime type that does not match the response
		releaseEvent.setAcceptType(MimeType.TEXT_HTML);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void passBuildParametersWhenAskedAndParamtersArePresent() throws Exception {
		// Prepare the server
		registerCheckBuildParameters();

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/checkBuildParameters");
		releaseEvent.setConsoleLogResponseBody(true);

		// Activate passBuildParameters
		releaseEvent.setPassBuildParameters(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0, new UserIdCause(), new ParametersAction(new StringParameterValue("foo", "value"))).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void replaceParametersInRequestBody() throws Exception {

		// Prepare the server
		registerCheckRequestBodyWithTag();

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/checkRequestBodyWithTag");
		releaseEvent.setConsoleLogResponseBody(true);

		// Activate requsetBody
		releaseEvent.setHttpMode(HttpMode.POST);

		// Use some random body content that contains a parameter
		releaseEvent.setRequestBody("cleanupDir=D:/continuousIntegration/deployments/Daimler/${Tag}/standalone");

		// Build parameters have to be passed
		releaseEvent.setPassBuildParameters(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);

		FreeStyleBuild build = project.scheduleBuild2(0, new UserIdCause(), new ParametersAction(new StringParameterValue("Tag", "trunk"))).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void silentlyIgnoreNonExistentBuildParameters() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);

		// Activate passBuildParameters without parameters present
		releaseEvent.setPassBuildParameters(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void doNotPassBuildParametersWithBuildParameters() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);

		// Activate passBuildParameters
		releaseEvent.setPassBuildParameters(false);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0, new UserIdCause(), new ParametersAction(new StringParameterValue("foo", "value"))).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void passRequestBodyWhenRequestIsPostAndBodyIsPresent() throws Exception {
		// Prepare the server
		registerCheckRequestBody();

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/checkRequestBody");
		releaseEvent.setConsoleLogResponseBody(true);

		// Activate requsetBody
		releaseEvent.setHttpMode(HttpMode.POST);
		releaseEvent.setRequestBody("TestRequestBody");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void doNotPassRequestBodyWhenMethodIsGet() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setConsoleLogResponseBody(true);

		// Activate passBuildParameters
		releaseEvent.setRequestBody("TestRequestBody");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void doAllRequestTypes() throws Exception {
		for (HttpMode method : HttpMode.values()) {
			// Prepare the server
			registerRequestChecker(method);
			doRequest(method);

			cleanHandlers();
		}
	}

	public void doRequest(final HttpMode method) throws Exception {
		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/do" + method.toString());
		releaseEvent.setHttpMode(method);
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);

		if (method == HttpMode.HEAD) {
			return;
		}

		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void invalidResponseCodeFailsTheBuild() throws Exception {
		// Prepare the server
		registerInvalidStatusCode();

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/invalidStatusCode");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
		this.j.assertLogContains("Throwing status 400 for test", build);
	}

	@Test
	public void invalidResponseCodeIsAccepted() throws Exception {
		// Prepare the server
		registerInvalidStatusCode();

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/invalidStatusCode");
		releaseEvent.setValidResponseCodes("100:599");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains("Throwing status 400 for test", build);
	}

	@Test
	public void reverseRangeFailsTheBuild() throws Exception {
		// Prepare the server


		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doesNotMatter");
		releaseEvent.setValidResponseCodes("599:100");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

	@Test
	public void notANumberRangeValueFailsTheBuild() throws Exception {
		// Prepare the server


		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doesNotMatter");
		releaseEvent.setValidResponseCodes("text");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

	@Test
	public void rangeWithTextFailsTheBuild() throws Exception {
		// Prepare the server


		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doesNotMatter");
		releaseEvent.setValidResponseCodes("1:text");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

	@Test
	public void invalidRangeFailsTheBuild() throws Exception {
		// Prepare the server


		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doesNotMatter");
		releaseEvent.setValidResponseCodes("1:2:3");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

	@Test
	public void sendAllContentTypes() throws Exception {
		for (MimeType mimeType : MimeType.values()) {
			sendContentType(mimeType);
		}
	}

	public void sendContentType(final MimeType mimeType) throws Exception {
		registerContentTypeRequestChecker(mimeType, HttpMode.GET, ALL_IS_WELL);
	}

	public void sendContentType(final MimeType mimeType, String checkMessage, String body) throws Exception {
		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/incoming_" + mimeType.toString());
		releaseEvent.setConsoleLogResponseBody(true);
		releaseEvent.setContentType(mimeType);
        if (body != null) {
            releaseEvent.setHttpMode(HttpMode.POST);
            releaseEvent.setRequestBody(body);
        }

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(checkMessage, build);
	}

    @Test
    public void sendNonAsciiRequestBody() throws Exception {
		registerContentTypeRequestChecker(MimeType.APPLICATION_JSON, HttpMode.POST, null);
        sendContentType(MimeType.APPLICATION_JSON, ALL_IS_WELL, ALL_IS_WELL);
    }

    @Test
    public void sendUTF8RequestBody() throws Exception {
        String notAsciiUTF8Message = "ἱερογλύφος";
		registerContentTypeRequestChecker(MimeType.APPLICATION_JSON_UTF8, HttpMode.POST, null);
        sendContentType(MimeType.APPLICATION_JSON_UTF8, notAsciiUTF8Message, notAsciiUTF8Message);
    }

	@Test
	public void sendAllAcceptTypes() throws Exception {
		for (MimeType mimeType : MimeType.values()) {
			// Prepare the server
			registerAcceptedTypeRequestChecker(mimeType);
			sendAcceptType(mimeType);

			cleanHandlers();
		}
	}

	public void sendAcceptType(final MimeType mimeType) throws Exception {
		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/accept_" + mimeType.toString());
		releaseEvent.setConsoleLogResponseBody(true);
		releaseEvent.setAcceptType(mimeType);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void canPutResponseInOutputFile() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setOutputFile("file.txt");
		releaseEvent.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);

		// By default, the response is printed to the console even if an outputFile is used
		this.j.assertLogContains(ALL_IS_WELL, build);

		// The response is in the output file as well
		String outputFile = build.getWorkspace().child("file.txt").readToString();
		Pattern p = Pattern.compile(ALL_IS_WELL);
		Matcher m = p.matcher(outputFile);
		assertTrue(m.find());
	}

	@Test
	public void canPutResponseInOutputFileWhenNotSetToGoToConsole() throws Exception {
		// Prepare the server
		registerRequestChecker(HttpMode.GET);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/doGET");
		releaseEvent.setOutputFile("file.txt");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);

		// Check that the console does NOT have the response body
		this.j.assertLogNotContains(ALL_IS_WELL, build);

		// The response is in the output file
		String outputFile = build.getWorkspace().child("file.txt").readToString();
		Pattern p = Pattern.compile(ALL_IS_WELL);
		Matcher m = p.matcher(outputFile);
		assertTrue(m.find());
	}

	@Test
	public void timeoutFailsTheBuild() throws Exception {
		// Prepare the server
		registerTimeout();


		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/timeout");
		releaseEvent.setTimeout(2);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

	@Test
	public void canDoCustomHeaders() throws Exception {
		// Prepare the server
		registerCustomHeaders();

		List<HttpRequestNameValuePair> customHeaders = new ArrayList<HttpRequestNameValuePair>();
		customHeaders.add(new HttpRequestNameValuePair("customHeader", "value1"));
		customHeaders.add(new HttpRequestNameValuePair("customHeader", "value2"));
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/customHeaders");
		releaseEvent.setCustomHeaders(customHeaders);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.SUCCESS, build);
	}

	@Test
	public void replaceParametesInCustomHeaders() throws Exception {
		// Prepare the server
		registerCustomHeadersResolved();

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/customHeadersResolved");
		releaseEvent.setConsoleLogResponseBody(true);

		// Activate requsetBody
		releaseEvent.setHttpMode(HttpMode.POST);

		// Add some custom headers
		List<HttpRequestNameValuePair> customHeaders = new ArrayList<HttpRequestNameValuePair>();
		customHeaders.add(new HttpRequestNameValuePair("resolveCustomParam", "${Tag}"));
		customHeaders.add(new HttpRequestNameValuePair("resolveEnvParam", "${WORKSPACE}"));
		releaseEvent.setCustomHeaders(customHeaders);

		// Activate passBuildParameters
		releaseEvent.setPassBuildParameters(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);

		FreeStyleBuild build = project.scheduleBuild2(0, new UserIdCause(),
				new ParametersAction(new StringParameterValue("Tag", "trunk"), new StringParameterValue("WORKSPACE", "C:/path/to/my/workspace"))).get();

		// Check expectations
		this.j.assertBuildStatus(Result.SUCCESS, build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

	@Test
	public void nonExistentBasicAuthFailsTheBuild() throws Exception {
		// Prepare the server
		registerBasicAuth();


		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/basicAuth");
		releaseEvent.setAuthentication("non-existent-key");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
	}

	@Test
	public void canDoBasicDigestAuthentication() throws Exception {
		// Prepare the server
		registerBasicAuth();

		// Prepare the authentication
		registerBasicCredential("keyname1", "username1", "password1");
		registerBasicCredential("keyname2", "username2", "password2");

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/basicAuth");
		releaseEvent.setAuthentication("keyname1");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.SUCCESS, build);
	}

	@Test
	public void testFormAuthentication() throws Exception {
		final String paramUsername = "username";
		final String valueUsername = "user";
		final String paramPassword = "password";
		final String valuePassword = "pass";
		final String sessionName = "VALID_SESSIONID";

		registerHandler("/form-auth", HttpMode.POST, new SimpleHandler() {
			@Override
			void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				String username = request.getParameter(paramUsername);
				String password = request.getParameter(paramPassword);
				if (!username.equals(valueUsername) || !password.equals(valuePassword)) {
					response.setStatus(401);
					return;
				}
				response.addCookie(new Cookie(sessionName, "ok"));
				okAllIsWell(response);
			}
		});
		registerHandler("/test-auth", HttpMode.GET, new SimpleHandler() {
			@Override
			void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				String jsessionValue = "";
				Cookie[] cookies = request.getCookies();
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(sessionName)) {
						jsessionValue = cookie.getValue();
						break;
					}
				}

				if (!jsessionValue.equals("ok")) {
					response.setStatus(401);
					return;
				}
				okAllIsWell(response);
			}
		});


		// Prepare the authentication
		List<HttpRequestNameValuePair> params = new ArrayList<>();
		params.add(new HttpRequestNameValuePair(paramUsername, valueUsername));
		params.add(new HttpRequestNameValuePair(paramPassword, valuePassword));

		RequestAction action = new RequestAction(new URL(baseURL() + "/form-auth"), HttpMode.POST, null, params);
		List<RequestAction> actions = new ArrayList<RequestAction>();
		actions.add(action);

		FormAuthentication formAuth = new FormAuthentication("Form", actions);
		List<FormAuthentication> formAuthList = new ArrayList<FormAuthentication>();
		formAuthList.add(formAuth);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/test-auth");
		InstanaPluginGlobalConfig.get().setFormAuthentications(formAuthList);
		releaseEvent.setAuthentication("Form");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.SUCCESS, build);
	}

	@Test
	public void canDoFormAuthentication() throws Exception {
		// Prepare the server
		registerFormAuth();
		registerReqAction();

		// Prepare the authentication
		List<HttpRequestNameValuePair> params = new ArrayList<HttpRequestNameValuePair>();
		params.add(new HttpRequestNameValuePair("param1", "value1"));
		params.add(new HttpRequestNameValuePair("param2", "value2"));

		RequestAction action = new RequestAction(new URL(baseURL() + "/reqAction"), HttpMode.GET, null, params);
		List<RequestAction> actions = new ArrayList<RequestAction>();
		actions.add(action);

		FormAuthentication formAuth = new FormAuthentication("keyname", actions);
		List<FormAuthentication> formAuthList = new ArrayList<FormAuthentication>();
		formAuthList.add(formAuth);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/formAuth");
		InstanaPluginGlobalConfig.get().setFormAuthentications(formAuthList);
		releaseEvent.setAuthentication("keyname");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.SUCCESS, build);
	}

	@Test
	public void rejectedFormCredentialsFailTheBuild() throws Exception {
		// Prepare the server
		registerFormAuthBad();

		// Prepare the authentication
		List<HttpRequestNameValuePair> params = new ArrayList<HttpRequestNameValuePair>();
		params.add(new HttpRequestNameValuePair("param1", "value1"));
		params.add(new HttpRequestNameValuePair("param2", "value2"));

		RequestAction action = new RequestAction(new URL(baseURL() + "/formAuthBad"), HttpMode.GET, null, params);
		List<RequestAction> actions = new ArrayList<RequestAction>();
		actions.add(action);

		FormAuthentication formAuth = new FormAuthentication("keyname", actions);
		List<FormAuthentication> formAuthList = new ArrayList<FormAuthentication>();
		formAuthList.add(formAuth);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/formAuthBad");
		releaseEvent.setConsoleLogResponseBody(true);
		InstanaPluginGlobalConfig.get().setFormAuthentications(formAuthList);
		releaseEvent.setAuthentication("keyname");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
		this.j.assertLogContains("Error doing authentication", build);
	}

	@Test
	public void invalidKeyFormAuthenticationFailsTheBuild() throws Exception {
		// Prepare the server


		// Prepare the authentication
		List<HttpRequestNameValuePair> params = new ArrayList<HttpRequestNameValuePair>();
		params.add(new HttpRequestNameValuePair("param1", "value1"));
		params.add(new HttpRequestNameValuePair("param2", "value2"));

		// The request action won't be sent but we need to prepare it
		RequestAction action = new RequestAction(new URL(baseURL() + "/non-existent"), HttpMode.GET, null, params);
		List<RequestAction> actions = new ArrayList<RequestAction>();
		actions.add(action);

		FormAuthentication formAuth = new FormAuthentication("keyname", actions);
		List<FormAuthentication> formAuthList = new ArrayList<FormAuthentication>();
		formAuthList.add(formAuth);

		// Prepare ReleaseEvent - the actual request won't be sent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/non-existent");
		releaseEvent.setConsoleLogResponseBody(true);
		InstanaPluginGlobalConfig.get().setFormAuthentications(formAuthList);

		// Select a non-existent form authentication, this will error the build before any request is made
		releaseEvent.setAuthentication("non-existent");

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatus(Result.FAILURE, build);
		this.j.assertLogContains("Authentication 'non-existent' doesn't exist anymore", build);
	}

	@Test
	public void responseContentSupplierHeadersFilling() throws Exception {
		// Prepare test context
		HttpResponse response = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
		response.setEntity(new StringEntity("TEST"));
		response.setHeader("Server", "Jenkins");
		response.setHeader("Set-Cookie", "JSESSIONID=123456789");
		response.addHeader("Set-Cookie", "JSESSIONID=abcdefghijk");
		// Run test
		ResponseContentSupplier respSupplier = new ResponseContentSupplier(ResponseHandle.STRING, response);
		// Check expectations
		Assert.assertEquals(2, respSupplier.getHeaders().size());
		Assert.assertTrue(respSupplier.getHeaders().containsKey("Server"));
		Assert.assertTrue(respSupplier.getHeaders().containsKey("Set-Cookie"));
		Assert.assertEquals(1, respSupplier.getHeaders().get("Server").size());
		Assert.assertEquals(2, respSupplier.getHeaders().get("Set-Cookie").size());
		Assert.assertEquals("Jenkins", respSupplier.getHeaders().get("Server").get(0));
		int valuesFoundCounter = 0;
		for (String s : respSupplier.getHeaders().get("Set-Cookie")) {
			if ("JSESSIONID=123456789".equals(s)) {
				valuesFoundCounter++;
			} else if ("JSESSIONID=abcdefghijk".equals(s)) {
				valuesFoundCounter++;
			}
		}
		Assert.assertEquals(2, valuesFoundCounter);
		respSupplier.close();
	}

	@Test
	public void testFileUpload() throws Exception {
		// Prepare the server
		final File testFolder = folder.newFolder();
		File uploadFile = File.createTempFile("upload", ".zip", testFolder);
		String responseText = "File upload successful!";
		registerFileUpload(testFolder, uploadFile, responseText);

		// Prepare ReleaseEvent
		ReleaseEvent releaseEvent = new ReleaseEvent(baseURL() + "/uploadFile");
		releaseEvent.setHttpMode(HttpMode.POST);
		releaseEvent.setValidResponseCodes("201");
		releaseEvent.setConsoleLogResponseBody(true);
		releaseEvent.setUploadFile(uploadFile.getAbsolutePath());
		releaseEvent.setMultipartName("file-name");
		releaseEvent.setContentType(MimeType.APPLICATION_ZIP);
		releaseEvent.setAcceptType(MimeType.TEXT_PLAIN);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject();
		project.getBuildersList().add(releaseEvent);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(responseText, build);
	}
}
