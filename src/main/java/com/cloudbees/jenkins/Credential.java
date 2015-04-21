package com.cloudbees.jenkins;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

/**
 * Credential to access GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
public class Credential extends AbstractDescribableImpl<Credential> {
    public final String username;
    public final String apiUrl;
    public final String oauthAccessToken;

    @DataBoundConstructor
    public Credential(String username, String apiUrl, String oauthAccessToken) {
        this.username = username;
        this.apiUrl = apiUrl;
        this.oauthAccessToken = oauthAccessToken;
    }

    public GitHub login() throws IOException {
        if (Util.fixEmpty(apiUrl) != null) {
            return GitHub.connectToEnterprise(apiUrl,oauthAccessToken);
        }
        return GitHub.connect(username,oauthAccessToken);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Credential> {
        @Override
        public String getDisplayName() {
            return ""; // unused
        }

        public FormValidation doValidate(@QueryParameter String apiUrl, @QueryParameter String username, @QueryParameter String oauthAccessToken) throws IOException {
            try {
                GitHub gitHub;
                if (Util.fixEmpty(apiUrl) != null) {
                    gitHub = GitHub.connectToEnterprise(apiUrl,oauthAccessToken);
                } else {
                    gitHub = GitHub.connect(username,oauthAccessToken);
                }

                if (gitHub.isCredentialValid())
                    return FormValidation.ok("Verified");
                else
                    return FormValidation.error("Failed to validate the account");
            } catch (IOException e) {
                return FormValidation.error(e,"Failed to validate the account");
            }
        }
    }
}
