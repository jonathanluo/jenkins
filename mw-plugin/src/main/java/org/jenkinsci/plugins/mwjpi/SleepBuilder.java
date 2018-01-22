package org.jenkinsci.plugins.mwjpi;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.AncestorInPath;
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
    private String testName;

    /**
     * Match by field name, argument order is not important 
     */
    @DataBoundConstructor
    public SleepBuilder(String testName, long time) { // goes here when Save / Apply clicked during Job configuration
        this.time = time;
        this.testName = testName;
    }

    public long getTime() { // goes here when Config clicked if previous saved Job contains SleepBuilder instance
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Test Name: " + this.testName);
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

        /**
         * https://wiki.eclipse.org/Jelly_form_controls - how to populate other fields in jenkins plugin
         */
        public FormValidation doCheckTime(@QueryParameter("time") String time, // goes here when config intial page load, on change, Apply
                                          @QueryParameter("testName") String test_name,
                                          @QueryParameter String testType
                )
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

        public FormValidation doCheckTestName(@QueryParameter String testName, @AncestorInPath AbstractProject project)
                throws IOException, ServletException {
            try {
                if (testName.startsWith("Test")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Test name must start with 'Test'");
                }
            } catch (NumberFormatException e) {
            }
            return FormValidation.error(Messages.SleepBuilder_DescriptorImpl_errors_enterNumber());
        }

        /**
         * https://wiki.jenkins.io/display/JENKINS/Form+Validation
         *
         * Accessing context
         * Sometimes you want to access the context. For example, you might want to access the current 
         * FreeStyleProject object while validating a field of a Builder. You do this by putting 
         * AncestorInPath annotation.
         */
        public FormValidation doCheckTestType(@QueryParameter("testType") String testType, @AncestorInPath AbstractProject project)
                throws IOException, ServletException {
            try {
                if (testType.equals("unit") || testType.equals("regression") || testType.equals("functional")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Currently test type '" + testType + "' is not supported, pick another one");
                }
            } catch (NumberFormatException e) {
            }
            return FormValidation.error(Messages.SleepBuilder_DescriptorImpl_errors_enterNumber());
        }
    }
}
