package org.jenkinsci.plugins.mwjpi;

import hudson.Extension;
import hudson.model.RootAction;

/**
 * https://wiki.jenkins.io/display/JENKINS/Action+and+its+family+of+subtypes
 *
 * RootAction - https://youtu.be/azyv183Ua6U?t=742
 * https://youtu.be/iAJBKFe8mMo?t=3074
 * 
 * MwJpiRootAction is available before any job is created
 */
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
        return "https://wiki.jenkins.io/display/JENKINS/Action+and+its+family+of+subtypes";
    }

}
