package org.jenkinsci.plugins.mwjpi;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

/**
 * https://stackoverflow.com/questions/12236909/the-package-collides-with-a-type
 *
 */
public class SleepBuilder extends Builder {
    private long time;

    @DataBoundConstructor
    public SleepBuilder(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Going to sleep for: " + time + " ms.");
        for (int i = 0; i < time / 1000; i++) {
            listener.getLogger().println("    pause for " + (i+1) + " second.");
            Thread.sleep(1000);
        }
        long remainder = time % 1000;
        if (remainder > 0) {
            listener.getLogger().println("    finally pause for " + remainder + " ms.");
            Thread.sleep(remainder);
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return aClass == FreeStyleProject.class;
        }

        @Override
        public String getDisplayName() {
            return Messages.SleepBuilder_DescriptorImpl_DisplayName();
        }

        public FormValidation doCheckTime(@QueryParameter String time)
                throws IOException, ServletException {
            try {
                if (Long.valueOf(time) < 0) {
                    return FormValidation.error(Messages.SleepBuilder_DescriptorImpl_errors_positiveNumber());
                } else if (Long.valueOf(time) < 1000) {
                    return FormValidation.error(Messages.SleepBuilder_DescriptorImpl_errors_greaterThan1000());
                } else if (Long.valueOf(time) > 9999) {
                    return FormValidation.error(Messages.SleepBuilder_DescriptorImpl_errors_lessThan9999());
                }
                return FormValidation.ok();
            } catch (NumberFormatException e) {
            }
            return FormValidation.error(Messages.SleepBuilder_DescriptorImpl_errors_enterNumber());
        }

    }
}
