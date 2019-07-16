package jenkins.plugins.instana;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.XStream2;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import jenkins.plugins.instana.auth.Authenticator;
import jenkins.plugins.instana.auth.BasicDigestAuthentication;
import jenkins.plugins.instana.auth.FormAuthentication;
import jenkins.plugins.instana.util.HttpRequestNameValuePair;

/**
 * @author Martin d'Anjou
 */
@Extension
public class InstanaPluginGlobalConfig extends GlobalConfiguration {

	private @Nonnull String instanaUrl;
	private @Nonnull String token;
	private @Nonnull String proxy;
	private @Nonnull HttpMode httpMode = HttpMode.POST;
	private List<BasicDigestAuthentication> basicDigestAuthentications = new ArrayList<BasicDigestAuthentication>();
    private List<FormAuthentication> formAuthentications = new ArrayList<FormAuthentication>();

    private static final XStream2 XSTREAM2 = new XStream2();

    public InstanaPluginGlobalConfig() {
        load();
    }

    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void xStreamCompatibility() {
        XSTREAM2.addCompatibilityAlias("ReleaseEvent$DescriptorImpl", InstanaPluginGlobalConfig.class);
        XSTREAM2.addCompatibilityAlias("jenkins.plugins.instana.util.NameValuePair", HttpRequestNameValuePair.class);
    }

    @Override
    protected XmlFile getConfigFile() {
        Jenkins j = Jenkins.getInstance();
        if (j == null) return null;
        File rootDir = j.getRootDir();
        File xmlFile = new File(rootDir, "ReleaseEvent.xml");
        return new XmlFile(XSTREAM2, xmlFile);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json)
    throws FormException
    {
        req.bindJSON(this, json);
        save();
        return true;
    }

	public static FormValidation validateKeyName(String value) {
		List<Authenticator> list = InstanaPluginGlobalConfig.get().getAuthentications();

		int count = 0;
		for (Authenticator basicAuthentication : list) {
			if (basicAuthentication.getKeyName().equals(value)) {
				count++;
			}
		}

		if (count > 1) {
			return FormValidation.error("The Key Name must be unique");
		}

		return FormValidation.validateRequired(value);
	}

    public static InstanaPluginGlobalConfig get() {
        return GlobalConfiguration.all().get(InstanaPluginGlobalConfig.class);
    }

    public List<BasicDigestAuthentication> getBasicDigestAuthentications() {
        return basicDigestAuthentications;
    }

    public void setBasicDigestAuthentications(
            List<BasicDigestAuthentication> basicDigestAuthentications) {
        this.basicDigestAuthentications = basicDigestAuthentications;
    }

    public List<FormAuthentication> getFormAuthentications() {
        return formAuthentications;
    }

    public void setFormAuthentications(
            List<FormAuthentication> formAuthentications) {
        this.formAuthentications = formAuthentications;
    }

	@Nonnull
	public String getInstanaUrl() {
		return instanaUrl;
	}

	public void setInstanaUrl(@Nonnull String instanaUrl) {
		this.instanaUrl = instanaUrl;
	}

	@Nonnull
	public String getToken() {
		return token;
	}

	public void setToken(@Nonnull String token) {
		this.token = token;
	}

	@Nonnull
	public HttpMode getHttpMode() {
		return httpMode;
	}

	public void setHttpMode(@Nonnull HttpMode httpMode) {
		this.httpMode = httpMode;
	}

	@Nonnull
	public String getProxy() {
		return proxy;
	}

	public void setProxy(@Nonnull String proxy) {
		this.proxy = proxy;
	}

	public List<Authenticator> getAuthentications() {
        List<Authenticator> list = new ArrayList<Authenticator>();
        list.addAll(basicDigestAuthentications);
        list.addAll(formAuthentications);
        return list;
    }

    public Authenticator getAuthentication(String keyName) {
        for (Authenticator authenticator : getAuthentications()) {
            if (authenticator.getKeyName().equals(keyName)) {
                return authenticator;
            }
        }
        return null;
    }

	public ListBoxModel doFillHttpModeItems() {
		return HttpMode.getFillItems();
	}
}
