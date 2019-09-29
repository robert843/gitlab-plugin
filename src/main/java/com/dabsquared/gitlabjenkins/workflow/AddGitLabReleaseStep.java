package com.dabsquared.gitlabjenkins.workflow;

import com.dabsquared.gitlabjenkins.cause.CauseData;
import com.dabsquared.gitlabjenkins.cause.GitLabWebHookCause;
import com.dabsquared.gitlabjenkins.gitlab.api.GitLabClient;
import com.dabsquared.gitlabjenkins.gitlab.api.model.MergeRequest;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.export.ExportedBean;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty.getClient;

/**
 * @author <a href="mailto:robert.japelski@gmail.com">Robert Japełski</a>
 */
@ExportedBean
public class AddGitLabReleaseStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(AddGitLabReleaseStep.class.getName());


    private String name;
    private String tag_name;
    private String description;

    @DataBoundConstructor
    public AddGitLabReleaseStep(String name,String tag_name, String description) {
        this.name = StringUtils.isEmpty(name) ? null : name;
        this.tag_name = StringUtils.isEmpty(tag_name) ? null : tag_name;
        this.description = StringUtils.isEmpty(description) ? null : description;
    }

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new AddGitLabReleaseStepExecution(context, this);
	}

    @DataBoundSetter
    public void setName(String name) {
        this.name = StringUtils.isEmpty(name) ? null : name;
    }
    @DataBoundSetter
    public void setTag_name(String tag_name) {
        this.tag_name = StringUtils.isEmpty(tag_name) ? null : tag_name;
    }
    @DataBoundSetter
    public void setDescription(String description) {
        this.description = StringUtils.isEmpty(description) ? null : description;
    }

    public String getName() {
        return name;
    }

    public String getTag_name() {
        return tag_name;
    }

    public String getDescription() {
        return description;
    }

    public static class AddGitLabReleaseStepExecution extends AbstractSynchronousStepExecution<Void> {
        private static final long serialVersionUID = 1;

        private final transient Run<?, ?> run;

        private final transient AddGitLabReleaseStep step;

        AddGitLabReleaseStepExecution(StepContext context, AddGitLabReleaseStep step) throws Exception {
            super(context);
            this.step = step;
            run = context.get(Run.class);
        }
        
        @Override
        protected Void run() throws Exception {
            GitLabWebHookCause cause = run.getCause(GitLabWebHookCause.class);
            if (cause != null) {
                CauseData data = cause.getData();
                if (data != null) {
                    GitLabClient client = getClient(run);
                    if (client == null) {
                        println("No GitLab connection configured");
                    } else {
                        Integer projectId = data.getTargetProjectId();
                        client.addRelease(projectId, step.name, step.tag_name, step.description); }
                }
            }
            return null;
        }

        private void println(String message) {
            TaskListener listener = getTaskListener();
            if (listener == null) {
                LOGGER.log(Level.FINE, "failed to print message {0} due to null TaskListener", message);
            } else {
                listener.getLogger().println(message);
            }
        }

        private void printf(String message, Object... args) {
            TaskListener listener = getTaskListener();
            if (listener == null) {
                LOGGER.log(Level.FINE, "failed to print message {0} due to null TaskListener", String.format(message, args));
            } else {
                listener.getLogger().printf(message, args);
            }
        }

        private TaskListener getTaskListener() {
            StepContext context = getContext();
            if (!context.isReady()) {
                return null;
            }
            try {
                return context.get(TaskListener.class);
            } catch (Exception x) {
                return null;
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return "Add release in GitLab Project";
        }

        @Override
        public String getFunctionName() {
            return "addGitLabRelease";
        }
        
		@Override
		public Set<Class<?>> getRequiredContext() {
			return ImmutableSet.of(TaskListener.class, Run.class);
		}
    }
}
