package org.jenkinsci.plugins.mwjpi;

import static java.util.Arrays.asList;

import java.io.IOException;
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
import hudson.util.ListBoxModel;

/**
 * https://stackoverflow.com/questions/12236909/the-package-collides-with-a-type
 * SleepBuilder is a class name in main/java, and a package name in main/resources
 * To avoid seeing the warning message "the package collides with a type", comment out the following line in .classpath:
 * <!--  <classpathentry kind="src" path="src/main/resources" excluding="** /*.java"/> -->
 */
public class DynamicDropDownListBox extends Builder {

    private String country;
    private String state;
    private String city;

    @DataBoundConstructor
    public DynamicDropDownListBox(String country, String state, String city) {
        this.country = country;
        this.state = state;
        this.city = city;
    }

    /**
     * Dynamic drop down list 
     */
    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("country: " + country);
        listener.getLogger().println("state: " + state);
        listener.getLogger().println("city: " + city);
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public String getCountry() {
            return "USA";
        }

        public String getState() {
            return "USA:B";
        }

        public String getCity() {
            return "USA:B:Z";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return aClass == FreeStyleProject.class;
        }

        @Override
        public String getDisplayName() {
            return "Dynamic Drop Down ListBox";
        }

        public ListBoxModel doFillStateItems(@QueryParameter String country) {
            ListBoxModel m = new ListBoxModel();
            if (!country.isEmpty()) {
                for (String s : asList("A","B","C"))
                    m.add(String.format("State %s in country '%s'", s, country),
                            country+':'+s);
            }
            return m;
        }

        public ListBoxModel doFillCityItems(@QueryParameter String country, @QueryParameter String state) {
            ListBoxModel m = new ListBoxModel();
            if (!country.isEmpty()) {
                for (String s : asList("W","X","Y","Z"))
                    m.add(String.format("City %s in state '%s', country '%s'", s, state, country),
                            state+':'+s);
            }
            return m;
        }

    }
}
