package com.dabsquared.gitlabjenkins.publisher;


import com.dabsquared.gitlabjenkins.gitlab.api.GitLabClient;
import com.dabsquared.gitlabjenkins.gitlab.api.model.Awardable;
import com.dabsquared.gitlabjenkins.gitlab.api.model.MergeRequest;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Robin Müller
 */
public class GitLabReleasePublisher extends MergeRequestNotifier {
    private static final Logger LOGGER = Logger.getLogger(GitLabReleasePublisher.class.getName());

    @DataBoundConstructor
    public GitLabReleasePublisher() { }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.GitLabReleasePublisher_DisplayName();
        }
    }

    @Override
    protected void perform(Run<?, ?> build, TaskListener listener, GitLabClient client, MergeRequest mergeRequest) {
        System.out.println("GitLabReleasePublisher:perform");
    }


    private boolean isSuccessful(Result result) {
        if (result == Result.SUCCESS) {
            return true;
        } else {
            return false;
        }
    }

    private String getResultIcon(Result result) {
        return getResultIcon(isSuccessful(result));
    }

    private String getResultIcon(boolean success) {
        if (success) {
            return "thumbsup";
        } else {
            return "thumbsdown";
        }
    }
}
