package org.jenkinsci.plugins.mwjpi;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PostBuildAction extends Recorder {

    private final String name;
    private boolean useFrench;

    @DataBoundConstructor
    public PostBuildAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isUseFrench() {
        return useFrench;
    }

    @DataBoundSetter
    public void setUseFrench(boolean useFrench) {
        this.useFrench = useFrench;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        EnvVars env = build.getEnvironment(listener);
        String zipFilePath = env.get("ZIP_FILE_PATH");
        if (useFrench) {
            listener.getLogger().println("Bonjour, " + name + "!");
        } else {
            listener.getLogger().println("Hello, " + name + "!");
        }

        // use parameterized trigger plugin to trigger downsteam job
        FileOutputStream fileOut = null;
        try {
            Properties properties = new Properties();
            properties.setProperty("PROJECT_ID", "1865");
            properties.setProperty("DESCRIPTION", "Release version 3.1.0 for Linux");

            Path path = Files.createTempDirectory("mw_");
            File file = new File(env.get("WORKSPACE") + "/mw-env/");
            file.mkdirs();
            file = new File(env.get("WORKSPACE") + "/mw-env/mw.properties");
            fileOut = new FileOutputStream(file);
            properties.store(fileOut, null);
            listener.getLogger().println("created property file at " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            listener.getLogger().println("error while creating propertye file " + e.getMessage());
            return false;
        } catch (IOException e) {
            listener.getLogger().println("error while creating propertye file " + e.getMessage());
            return false;
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }
        // Warning: you have no plugins providing access control for builds, so falling back to legacy behavior of 
        // permitting any downstream builds to be triggered
        return true;
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * <input checkdependson="useFrench" 
         *        checkurl="/jenkins/job/2nd%20Test/descriptorByName/org.jenkinsci.plugins.mwjpi.PostBuildAction/checkName" 
         *        name="_.name"
         *        type="text"
         *        class="setting-input validated  "
         *        value="">
         * 
         * http://localhost:8080/jenkins/job/2nd%20Test/descriptorByName/org.jenkinsci.plugins.mwjpi.PostBuildAction/checkName?value=test&useFrench=false
         */
        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.PostBuildAction_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.PostBuildAction_DescriptorImpl_warnings_tooShort());
            if (!useFrench && value.matches(".*[éáàç].*")) {
                return FormValidation.warning(Messages.PostBuildAction_DescriptorImpl_warnings_reallyFrench());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.PostBuildAction_DescriptorImpl_DisplayName();
        }

    }

    public static void main(String [] argvs) throws IOException {
    	Path path = Files.createTempDirectory("mw_");
    	String tmpdir = System.getProperty("java.io.tmpdir");
    	System.out.println(tmpdir);
    	System.out.println(path.toString());
    }
}
