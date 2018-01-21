package org.jenkinsci.plugins.mwjpi;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public class MwJpiRootAction implements RootAction {

    @Override
    public String getIconFileName() {
        return "clipboard.png";
    }

    @Override
    public String getDisplayName() {
        return "MW JPI";
    }

    @Override
    public String getUrlName() {
        return "https://wiki.jenkins.io/display/JENKINS/Extend+Jenkins";
    }

}
