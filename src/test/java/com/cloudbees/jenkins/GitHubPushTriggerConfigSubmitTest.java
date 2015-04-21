package com.cloudbees.jenkins;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.util.Secret;
import java.net.URL;
import java.util.List;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.Stapler;

/**
 * Test Class for {@link GitHubPushTrigger}.
 *
 * @author Seiji Sogabe
 */
public class GitHubPushTriggerConfigSubmitTest extends HudsonTestCase {

    private static final String WEBHOOK_URL = "http://jenkinsci.example.com/jenkins/github-webhook/";

    public void testConfigSubmit_AutoManageHook() throws Exception {

        WebClient client = configureWebClient();
        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByValue("auto").setChecked(true);
        f.getInputByName("_.hasHookUrl").setChecked(true);
        f.getInputByName("_.hookUrl").setValueAttribute(WEBHOOK_URL);
        f.getInputByName("_.username").setValueAttribute("jenkins");
        submit(f);

        GitHubPushTrigger.DescriptorImpl d = getDescriptor();
        assertTrue(d.isManageHook());
        assertEquals(new URL(WEBHOOK_URL), d.getHookUrl());

        List<Credential> credentials = d.getCredentials();
        assertNotNull(credentials);
        assertEquals(1, credentials.size());
        Credential credential = credentials.get(0);
        assertEquals("jenkins", credential.username);
    }

    public void testConfigSubmit_ManuallyManageHook() throws Exception {

        WebClient client = configureWebClient();
        HtmlPage p = client.goTo("configure");
        HtmlForm f = p.getFormByName("config");
        f.getInputByValue("none").setChecked(true);
        submit(f);

        GitHubPushTrigger.DescriptorImpl d = getDescriptor();
        assertFalse(d.isManageHook());
    }

    private GitHubPushTrigger.DescriptorImpl getDescriptor() {
        return (GitHubPushTrigger.DescriptorImpl) GitHubPushTrigger.DescriptorImpl.get();
    }

    private WebClient configureWebClient() {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);
        client.setCssEnabled(false);
        client.setJavaScriptEnabled(true);
        return client;
    }

    // workaround
    static {
        Stapler.CONVERT_UTILS.register(new org.apache.commons.beanutils.Converter() {

            public Secret convert(Class type, Object value) {
                return Secret.fromString(value.toString());
            }
        }, Secret.class);
    }
}
