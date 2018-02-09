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
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;

import org.jenkinsci.plugins.mwjpi.rest.RestClient;

/**
 * https://stackoverflow.com/questions/12236909/the-package-collides-with-a-type
 * SleepBuilder is a class name in main/java, and a package name in main/resources
 * To avoid seeing the warning message "the package collides with a type", comment out the following line in .classpath:
 * <!--  <classpathentry kind="src" path="src/main/resources" excluding="** /*.java"/> -->
 */
public class SleepBuilder2 extends Builder {
    private long time;
    @NonNull
    private String testName;
    private String testType;
    private String goalType;
    private String osType;

    // https://github.com/jenkinsci/xshell-plugin
    @Extension
    public static final SleepBuilder2Descriptor DESCRIPTOR = new SleepBuilder2Descriptor();

    /**
     * https://www.programcreek.com/java-api-examples/org.kohsuke.stapler.DataBoundConstructor
     * Match by field name, argument order is not important 
     */
    @DataBoundConstructor
    public SleepBuilder2(@CheckForNull String testName, long time, String testType) { // goes here when Save / Apply clicked during Job configuration
        this.time = time;
        this.testName = testName;
        this.testType = testType;
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
      return DESCRIPTOR;
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
}
