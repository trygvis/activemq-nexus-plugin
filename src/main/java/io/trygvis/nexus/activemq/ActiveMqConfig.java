package io.trygvis.nexus.activemq;

import io.trygvis.nexus.activemq.model.v1_0_0.Configuration;
import io.trygvis.nexus.activemq.model.v1_0_0.io.xpp3.ActiveMqPluginConfigurationXpp3Reader;
import io.trygvis.nexus.activemq.model.v1_0_0.io.xpp3.ActiveMqPluginConfigurationXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.application.NexusConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class ActiveMqConfig {

    private final Logger log = getLogger(getClass());

    private final File file;
    private Configuration configuration;
    private ReentrantLock lock = new ReentrantLock();

    @Inject
    public ActiveMqConfig(NexusConfiguration applicationConfiguration) {
        file = new File(applicationConfiguration.getConfigurationDirectory(), "activemq-plugin.xml");
    }

    public Configuration getConfiguration() {
        if (configuration == null) {
            lock.lock();

            try {
                if (!file.canRead()) {
                    log.info("Creating default configuration file: {}", file);
                    configuration = defaultConfiguration();
                    write(configuration);
                } else {
                    configuration = read();
                }
            } finally {
                lock.unlock();
            }
        }
        return configuration;
    }

    public static Configuration defaultConfiguration() {
        Configuration c = new Configuration();
        c.setEnabled(false);
        c.setBrokerUrl("tcp://127.0.0.1:61616");
        c.setTopicName("nexus.new-artifact");
        return c;
    }

    private void write(Configuration configuration) {
        log.info("Writing configuration to {}", file);
        try {
            ActiveMqPluginConfigurationXpp3Writer writer = new ActiveMqPluginConfigurationXpp3Writer();
            FileOutputStream os = new FileOutputStream(file);
            writer.write(os, configuration);
            os.close();
        } catch (IOException e) {
            log.error("Could not write configuration.", e);
        }
    }

    private Configuration read() {
        log.info("Reading configuration from {}", file);
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            ActiveMqPluginConfigurationXpp3Reader reader = new ActiveMqPluginConfigurationXpp3Reader();
            return reader.read(is);
        } catch (IOException e) {
            log.error("Could not read file, using default configuration", e);
        } catch (XmlPullParserException e) {
            log.error("Invalid XML Configuration, using default configuration", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }

        return defaultConfiguration();
    }
}
