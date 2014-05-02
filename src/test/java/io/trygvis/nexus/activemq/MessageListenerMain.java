package io.trygvis.nexus.activemq;

import io.trygvis.nexus.activemq.model.v1_0_0.Configuration;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.io.IOException;

public class MessageListenerMain {
    public static void main(String[] args) throws JMSException, IOException {
        Configuration configuration = ActiveMqConfig.defaultConfiguration();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");

        Connection connection = connectionFactory.createConnection();
        connection.start();
        System.out.println("Connected");
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic(configuration.getTopicName());
        MessageConsumer consumer = session.createConsumer(topic);

        while (true) {
            System.out.println("Waiting for messages");
            Message m = consumer.receive();

            if (m instanceof TextMessage) {
                TextMessage message = (TextMessage) m;

                System.out.println("--------------------------------");
                System.out.println(message.getText().trim());
                System.out.println("--------------------------------");
            } else {
                System.out.println("Unknown message: " + m);
            }
        }
    }
}
