package io.trygvis.nexus.activemq;

import org.slf4j.Logger;
import org.sonatype.nexus.plugin.PluginIdentity;

import javax.inject.Inject;
import javax.inject.Named;

import static org.slf4j.LoggerFactory.getLogger;

@Named
public class ActiveMqNexusPlugin extends PluginIdentity {

    private final Logger log = getLogger(getClass());

    @Inject
    public ActiveMqNexusPlugin() throws Exception {
        super("io.trygvis.calamus", "calamus-nexus-plugin");

        System.out.println("ActiveMqNexusPlugin.ActiveMqNexusPlugin");
    }
}
