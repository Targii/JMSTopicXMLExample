/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.JMSTopicXMLExample;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.StringWriter;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.activemq.ActiveMQConnectionFactory;


/**
 *
 * @author Adam
 */
public class JMSTopicXMLExample {
     public static void main(String[] args) throws Exception{
        // Run Producer and Consumer
        thread(new HelloWorldProducer(), false);
        thread(new HelloWorldConsumer(), false);
        thread(new HelloWorldConsumer(), false);
        thread(new HelloWorldConsumer(), false);
     }
     
      public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }
 
    public static class HelloWorldProducer implements Runnable {
        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
 
                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();
 
                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
                // Create the destination (Topic or Queue)
                Destination destination = session.createTopic("TEST.FOO");
 
                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                
                // Create a File from XML excercise
                File xmlFile = new File("src\\main\\java\\com\\mycompany\\JMSTopicXMLExample\\excercise-1.xml");
                
                
                // Create Document builder to parse xmlFile
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = docBuilder.parse(xmlFile);
                
                // Create a XMLSerializer to produce serialized file
                StringWriter writer = new StringWriter();
                OutputFormat format = new OutputFormat();
                XMLSerializer serializer = new XMLSerializer(writer, format);
                serializer.asDOMSerializer();
                serializer.serialize(doc);
                
                // Create a message
                TextMessage message = session.createTextMessage(writer.toString());
 
                // Tell the producer to send the message
                producer.send(message);
 
                // Clean up
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }
 
    public static class HelloWorldConsumer implements Runnable, ExceptionListener {
        public void run() {
            try {
 
                
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
 
                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();
 
                connection.setExceptionListener(this);
 
                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
                // Create the destination (Topic or Queue)
                Destination destination = session.createTopic("TEST.FOO");
 
                // Create a MessageConsumer from the Session to the Topic 
                MessageConsumer consumer = session.createConsumer(destination);
 
                // Wait for a message
                Message message = consumer.receive(1000);
 
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    System.out.println("Received: " + text);
                } else {
                    System.out.println("Received: " + message);
                }
 
                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
 
        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.");
        }
    }
}
