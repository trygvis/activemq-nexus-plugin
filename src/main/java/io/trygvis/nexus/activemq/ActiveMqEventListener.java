package io.trygvis.nexus.activemq;

import com.google.common.eventbus.Subscribe;
import io.trygvis.nexus.activemq.model.v1_0_0.Configuration;
import org.slf4j.Logger;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
@Named("ActiveMqEventListener")
public class ActiveMqEventListener implements EventSubscriber {

    private final Logger log = getLogger(getClass());

    private final MqClient mqClient;
    private final GavCalculator gavCalculator;
    private final ActiveMqConfig config;

    @Inject
    public ActiveMqEventListener(ActiveMqConfig config, @Named("maven2") GavCalculator gavCalculator) {
        this.config = config;
        this.gavCalculator = gavCalculator;

        Configuration configuration = config.getConfiguration();

        if (configuration.isEnabled() && isValid(configuration)) {
            String brokerUrl = configuration.getBrokerUrl();
            log.info("Connecting to {}", brokerUrl);
            this.mqClient = new MqClient(config, brokerUrl);
        } else {
            log.warn("The ActiveMq plugin is not enabled");
            this.mqClient = null;
        }
    }

    private boolean isValid(Configuration configuration) {
        String brokerUrl = trimToEmpty(configuration.getBrokerUrl());

        return brokerUrl.matches("^[a-z]+:.+");
    }

    @Subscribe
    public void onRepositoryItemEventStoreCreate(RepositoryItemEventStoreCreate event) {
        Configuration configuration = config.getConfiguration();

        if (!configuration.isEnabled()) {
            return;
        }

        Gav gav = gavCalculator.pathToGav(event.getItem().getPath());

        if (gav == null || gav.isHash() || gav.isSignature()) {
            return;
        }

        String groupId = gav.getGroupId();
        String artifactId = gav.getArtifactId();
        String version = gav.getVersion();
        String extension = gav.getExtension();
        String classifier = gav.getClassifier();
        log.info("New artifact: groupId={}, artifactId={}, version={}, classifier={}, extension={}", groupId, artifactId, version, classifier, extension);
        mqClient.newArtifact(event.getRepository().getId(), groupId, artifactId, version, classifier, extension);
    }
}
