package org.jenkinsci.plugins.mwjpi;

import hudson.model.Action;

/**
 * Plugin Development (Part 2) - Jenkins Web UI - https://youtu.be/iAJBKFe8mMo?t=3140
 */
public class JobStatusAction implements Action {

    @Override
    public String getIconFileName() {
        return "notepad";
    }

    @Override
    public String getDisplayName() {
        return "Job Status";
    }

    @Override
    public String getUrlName() {
        return "job_status";
    }

}
