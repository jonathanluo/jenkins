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
 * https://github.com/jenkinsci/xshell-plugin
 */
public final class SleepBuilder2Descriptor extends BuildStepDescriptor<Builder> {

    private String goalType;
    private String osType;

    public SleepBuilder2Descriptor() {
        super(SleepBuilder2.class);
        load();
    }
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return aClass == FreeStyleProject.class;
    }

    @Override
    public String getDisplayName() {
        return Messages.SleepBuilder2_DescriptorImpl_DisplayName();
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
        list.add(new BuildGoal("201", "Package x32"));
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
