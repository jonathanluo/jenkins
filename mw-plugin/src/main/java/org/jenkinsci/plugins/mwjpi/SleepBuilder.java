package org.jenkinsci.plugins.mwjpi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.CheckForNull; 
import edu.umd.cs.findbugs.annotations.NonNull; 
import javax.servlet.ServletException;

import org.jenkinsci.plugins.mwjpi.model.BuildGoal;
import org.jenkinsci.plugins.mwjpi.model.OsType;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerResponse;

import hudson.EnvVars;
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

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

import org.jenkinsci.plugins.mwjpi.rest.RestClient;

/**
 * https://stackoverflow.com/questions/12236909/the-package-collides-with-a-type
 * SleepBuilder is a class name in main/java, and a package name in main/resources
 * To avoid seeing the warning message "the package collides with a type", comment out the following line in .classpath:
 * <!--  <classpathentry kind="src" path="src/main/resources" excluding="** /*.java"/> -->
 */
public class SleepBuilder extends Builder {
    private long time;
    @NonNull
    private String testName;
    private String testType;
    private String goalType;
    private String osType;

    /**
     * https://www.programcreek.com/java-api-examples/org.kohsuke.stapler.DataBoundConstructor
     * Match by field name, argument order is not important 
     */
    @DataBoundConstructor
    public SleepBuilder(@CheckForNull String testName, long time, String testType) { // goes here when Save / Apply clicked during Job configuration
        this.time = time;
        this.testName = testName;
        this.testType = testType;
    }

    public String getMyString() {
        return "Hello Jenkins!";
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
        /* https://stackoverflow.com/questions/10625259/how-to-set-environment-variables-in-jenkins
             Environment Injector Plugin
             Inject environment variables
                Properties File Path
                Properties Content
        */
        EnvVars env = build.getEnvironment(listener);
        String zipFilePath = env.get("ZIP_FILE_PATH");

        listener.getLogger().println("Test Name: " + this.testName);
        listener.getLogger().println("Going to sleep for: " + time + " ms.");
        for (int i = 0; i < time / 1000; i++) {
            listener.getLogger().println("    pause for " + (i+1) + " second.");
            listener.getLogger().println(RestClient.query("/test_type/" + (i+1)));
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

        // define some title texts, default values
        public String getTitle4Time() {
            return "Time";
        }

        public String getTitle4TestName() {
            return "Test name";
        }

        public String getDefaultTestName() {
            return "Test_";
        }

        /**
         * Get rest string on the fly
         */
        public String getMyString() {
            return RestClient.query("/test_type/5");
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
         *
         *    <f:validateButton title="Validate" progress="validating..."
         *           method="testConnection" with="testName,testType,time" />
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
         * http://jenkins-ci.361315.n4.nabble.com/Update-Dropdown-elements-via-ajax-td4699541.html
         * The following method is equivalent to the above method
         *
         *    <f:validateButton title="Validate" progress="validating..."
         *           method="testConnection2" with="testName,testType,time" />
         *
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doTestConnection2()
                throws IOException, ServletException {
            try {

                String testName = Stapler.getCurrentRequest().getParameter("testName");
                String testType = Stapler.getCurrentRequest().getParameter("testtype");
                String time = Stapler.getCurrentRequest().getParameter("time");
                StaplerResponse resp = Stapler.getCurrentResponse();

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
            items.get(0).selected = true;
            return items;
        }

        public ListBoxModel doFillOsTypeItems(@QueryParameter("goalType") final String goalType) {
            ListBoxModel items = new ListBoxModel();
            for (OsType goal : getOsTypes(goalType)) {
                items.add(goal.getDisplayName(), goal.getId());
            }
            items.get(1).selected = true;
            return items;
        }

        public ListBoxModel doFillTestTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Select one", "");
            items.add("Unit testing", "unit");
            items.add("Black-box testing", "black-box");
            items.add("White-box testing", "white-box");
            items.add("Acceptance testing", "acceptance");
            items.add("Automated testing", "automated");
            items.add("Regression testing", "regression");
            items.add("Functional testing", "functional");
            items.add("Exploratory testing", "exploratory");
            items.add("Other testing", "other");
            return items;
        }

        public ListBoxModel doFillDummyFieldToHideItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Select one", "");
            return items;
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

        private List<BuildGoal> getBuildGoals() {
            List<BuildGoal> list = new ArrayList<BuildGoal>();
            list.add(new BuildGoal("", "Select a build goal"));
            list.add(new BuildGoal("101", "Build x64"));
            list.add(new BuildGoal("201", "Package x 32"));
            list.add(new BuildGoal("301", "Deploy"));
            list.add(new BuildGoal("401", "Install"));
            return list;
        }

        private List<OsType> getOsTypes(final String goalType) {
            List<OsType> list = new ArrayList<OsType>();
            if ("101".equals(goalType)) {
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
    }
}
