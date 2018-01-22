package org.jenkinsci.plugins.mwjpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import jenkins.model.TransientActionFactory;

/**
 * Modeled from HelloActionFactory
 *
 */
@Extension
public class JobActionFactory extends TransientActionFactory<Job> {

    @Override
    public Class<Job> type() {
        return Job.class;
    }

    /**
     * HelloAction is available after job is created
     */
    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull Job target) {
        // return Arrays.asList(new SettingAction()); // show error in Eclispe due to a package also named HelloAction in resources
        // solution - .classpath: <!--  <classpathentry kind="src" path="src/main/resources" excluding="**/*.java"/> -->
        List list = new ArrayList();
        list.add(new SettingAction());
        list.add(new JobStatusAction());
        return list;
    }
}
