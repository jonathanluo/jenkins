package org.jenkinsci.plugins.mwjpi;

import hudson.model.Action;

/**
 * Plugin Development (Part 2) - Jenkins Web UI - https://youtu.be/iAJBKFe8mMo?t=3140
 * Modified from HelloAction
 */
public class SettingAction implements Action {

    @Override
    public String getIconFileName() {
        return "notepad";
    }

    @Override
    public String getDisplayName() {
        return "Setting";
    }

    @Override
    public String getUrlName() {
        return "setting";
    }

}
