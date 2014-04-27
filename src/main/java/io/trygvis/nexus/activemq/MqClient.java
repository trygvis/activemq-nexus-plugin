package io.trygvis.nexus.activemq;

import com.sun.jndi.ldap.pool.PooledConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Properties;

import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class MqClient {

    private final Logger log = getLogger(getClass());

    private final ConnectionFactory connectionFactory;

    public MqClient(String brokerUrl) {
        PooledConnectionFactory cf = new PooledConnectionFactory(brokerUrl);
        cf.setMaxConnections(3);
        this.connectionFactory = cf;
    }

    public void newArtifact(String repository, String groupId, String artifactId, String version, String classifier, String extension) {
        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);

            Destination destination = session.createTopic("nexus.new-artifact");

            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(NON_PERSISTENT);

            Properties p = new Properties();
            p.setProperty("repository", repository);
            p.setProperty("groupId", groupId);
            p.setProperty("artifactId", artifactId);
            p.setProperty("version", version);
            p.setProperty("classifier", trimToEmpty(classifier));
            p.setProperty("extension", extension);

            CharArrayWriter buf = new CharArrayWriter();
            p.store(buf, null);
            producer.send(session.createTextMessage(buf.toString()));

            session.close();
            connection.close();
        } catch (JMSException | IOException e) {
            log.warn("Unable to send message to broker", e);
        }
    }
}
