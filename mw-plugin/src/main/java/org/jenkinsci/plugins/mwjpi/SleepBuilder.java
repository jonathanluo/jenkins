package org.jenkinsci.plugins.mwjpi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.jenkinsci.plugins.mwjpi.model.BuildGoal;
import org.jenkinsci.plugins.mwjpi.model.OsType;
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
import hudson.util.ListBoxModel;

/**
 * https://stackoverflow.com/questions/12236909/the-package-collides-with-a-type
 * SleepBuilder is a class name in main/java, and a package name in main/resources
 * To avoid seeing the warning message "the package collides with a type", comment out the following line in .classpath:
 * <!--  <classpathentry kind="src" path="src/main/resources" excluding="** /*.java"/> -->
 */
public class SleepBuilder extends Builder {
    private long time;
    private String testName;
    private String testType;
    private String goalType;
    private String osType;

    /**
     * Match by field name, argument order is not important 
     */
    @DataBoundConstructor
    public SleepBuilder(String testName, long time, String testType) { // goes here when Save / Apply clicked during Job configuration
        this.time = time;
        this.testName = testName;
        this.testType = testType;
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

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
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

        private String goalType;
        private String osType;

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return aClass == FreeStyleProject.class;
        }

        @Override
        public String getDisplayName() {
            return Messages.SleepBuilder_DescriptorImpl_DisplayName();
        }

        /**
         * https://wiki.eclipse.org/Jelly_form_controls
         *    - how to populate other fields in jenkins plugin
         *    - client side Javascript
         *
         * https://wiki.jenkins.io/display/JENKINS/Form+Validation
         * the method gets invoked in response to the onchange event on HTML DOM
         */
        public FormValidation doCheckTime(@QueryParameter("time") final String time) // goes here when config intial page load, on change, Apply
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

        public FormValidation doCheckTestName(@QueryParameter final String testName, @AncestorInPath AbstractProject project)
                throws IOException, ServletException {
            try {
                if (testName.startsWith("Test")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Test name must start with 'Test'");
                }
            } catch (NumberFormatException e) {
            }
            return FormValidation.error("Unknown error occurred");
        }

        /**
         * https://wiki.jenkins.io/display/JENKINS/Form+Validation
         *
         * Accessing context
         * Sometimes you want to access the context. For example, you might want to access the current 
         * FreeStyleProject object while validating a field of a Builder. You do this by putting 
         * AncestorInPath annotation.
         */
        public FormValidation doCheckTestType(@QueryParameter("testType") final String testType, @AncestorInPath AbstractProject project)
                throws IOException, ServletException {
            try {
                if (testType.equals("unit") || testType.equals("regression") || testType.equals("functional")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Currently test type '" + testType + "' is not supported, pick another one");
                }
            } catch (NumberFormatException e) {
            }
            return FormValidation.error("Unknown error occurred");
        }

        /**
         * https://wiki.jenkins.io/display/JENKINS/Jelly+form+controls
         * Select (drop-down menu), Validation Button
         */
        public FormValidation doTestConnection(@QueryParameter("testName") final String testName, 
                                               @QueryParameter("testType") final String testType,
                                               @QueryParameter("time") final String time,
                                               @AncestorInPath AbstractProject project)
                throws IOException, ServletException {
            try {
                if (testType.equals("unit") || testType.equals("regression") || testType.equals("functional")) {
                    return FormValidation.ok("Success");
                } else {
                    return FormValidation.error("Currently test type '" + testType + "' is not supported, pick another one");
                }
            } catch (NumberFormatException e) {
            }
            return FormValidation.error("Unknown error occurred");
        }

        /**
         * https://wiki.jenkins.io/display/JENKINS/Jelly+form+controls
         * Select (drop-down menu) with model filled values
         */
        public ListBoxModel doFillGoalTypeItems() {
            ListBoxModel items = new ListBoxModel();
            for (BuildGoal goal : getBuildGoals()) {
                items.add(goal.getDisplayName(), goal.getId());
            }
            return items;
        }

        public ListBoxModel doFillOsTypeItems() {
            ListBoxModel items = new ListBoxModel();
            for (OsType goal : getOsTypes()) {
                items.add(goal.getDisplayName(), goal.getId());
            }
            return items;
        }

        private List<BuildGoal> getBuildGoals() {
            List<BuildGoal> list = new ArrayList<BuildGoal>();
            list.add(new BuildGoal("", "Select a build goal"));
            list.add(new BuildGoal("101", "Build"));
            list.add(new BuildGoal("201", "Package"));
            list.add(new BuildGoal("301", "Deploy"));
            list.add(new BuildGoal("401", "Install"));
            return list;
        }

        private List<OsType> getOsTypes() {
            List<OsType> list = new ArrayList<OsType>();
            if (goalType.equals("101")) {
                list.add(new OsType("", "Select an os type"));
                list.add(new OsType("ubuntu", "Ubuntu x64"));
                list.add(new OsType("centos", "CentOS x64"));
                list.add(new OsType("oracle", "Oracle x64"));
            } else {
                list.add(new OsType("", "Select an os type"));
                list.add(new OsType("ubuntu32", "Ubuntu x32"));
                list.add(new OsType("centos32", "CentOS x32"));
                list.add(new OsType("oracle32", "Oracle x32"));
            }
            return list;
        }

        public FormValidation doCheckGoalType(@QueryParameter("goalType") final String goalType, @AncestorInPath AbstractProject project)
                throws IOException, ServletException {
            try {
                if (goalType.equals("101") || goalType.equals("201")) {
                    this.goalType = goalType;
                    return FormValidation.ok();
                }
                return FormValidation.error("Currently goal type '" + goalType + "' is not supported, pick another one");
            } catch (NumberFormatException e) {
            }
            return FormValidation.error("Unknown error occurred");
        }
    }
}
