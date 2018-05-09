package cn.zyc;


import cn.zyc.service.TestService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private String  brokerURL= "tcp://192.168.1.129:61616";
    @Test
    public void shouldAnswerWithTrue()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String("application-context.xml"));
        TestService testService = (TestService) context.getBean("testService");
        testService.run();
    }

    @Test
    public void sendMsg()throws Exception{
        // 第一步：创建ConnectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        // 第二步：通过工厂，创建Connection
        Connection connection = connectionFactory.createConnection();
        // 第三步：连接启动
        connection.start();
        // 第四步：通过连接获取session会话
        // 第一个参数：是否启用ActiveMQ的事务，如果为true，第二个参数失效。
        // 第二个参数：应答模式,AUTO_ACKNOWLEDGE为自动应答
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 第五步：通过session创建destination，两种方式：Queue、Topic
        // 参数：消息队列的名称，在后台管理系统中可以看到
        Queue queue = session.createQueue("test-activemq");
        // 第六步：通过session创建MessageProducer
        MessageProducer producer = session.createProducer(queue);
        // 第七步：创建Message
        // 方式1：
        TextMessage message = new ActiveMQTextMessage();
        message.setText("queue test");
        // 方式2：
        // TextMessage message = session.createTextMessage("queue test");
        // 第八步：通过producer发送消息
        producer.send(message);
        // 第九步：关闭资源
        producer.close();
        session.close();
        connection.close();// 第一步：创建ConnectionFactory
    }

    @Test
    public void testQueueConsumer()throws Exception{
        // 第一步：创建ConnectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        // 第二步：通过工厂，创建Connection
        Connection connection = connectionFactory.createConnection();
        // 第三步：打开连接
        connection.start();
        // 第四步：通过Connection创建session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 第五步：通过session创建Consumer
        Queue queue = session.createQueue("test-activemq");
        MessageConsumer consumer = session.createConsumer(queue);
        // 第六步：通过consumer接收信息（两种方式：一种是receive方法接收，一种通过监听器接收）
        // 方式1：receive方法接收信息
		/*
		 * while (true) { Message message = consumer.receive(100000); if
		 * (message == null) break; // 第七步：处理信息 if (message instanceof
		 * TextMessage) { TextMessage tm = (TextMessage) message;
		 * System.out.println(tm.getText()); } }
		 */
        // 方式2：监听器接收信息
        consumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message message) {
                // 第七步：处理信息
                if (message instanceof TextMessage) {
                    TextMessage tm = (TextMessage) message;
                    try {
                        String text = tm.getText();
                        System.out.println(text);
                    } catch (JMSException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        // 第八步：关闭资源
        consumer.close();
        session.close();
        connection.close();
    }


    @Test
    public void testTopicProducer() throws Exception {
        // 第一步：创建ConnectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        // 第二步：通过工厂，创建Connection
        Connection connection = connectionFactory.createConnection();
        // 第三步：连接启动
        connection.start();
        // 第四步：通过连接获取session会话
        // 第一个参数：是否启用ActiveMQ的事务，如果为true，第二个参数失效。
        // 第二个参数：应答模式,AUTO_ACKNOWLEDGE为自动应答
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 第五步：通过session创建destination，两种方式：Queue、Topic
        // 参数：主题的名称，在后台管理系统中可以看到
        Topic topic = session.createTopic("test-activemq");
        // 第六步：通过session创建MessageProducer
        MessageProducer producer = session.createProducer(topic);
        // 第七步：创建Message
        // 方式1：
        TextMessage message = new ActiveMQTextMessage();
        message.setText("topic test");
        // 方式2：
        // TextMessage message = session.createTextMessage("queue test");
        // 第八步：通过producer发送消息
        producer.send(message);
        // 第九步：关闭资源
        producer.close();
        session.close();
        connection.close();
    }

    @Test
    public void testTopicConsumer() throws Exception {
        // 第一步：创建ConnectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        // 第二步：通过工厂，创建Connection
        Connection connection = connectionFactory.createConnection();
        // 第三步：打开连接
        connection.start();
        // 第四步：通过Connection创建session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 第五步：通过session创建Consumer
        Topic topic = session.createTopic("test-activemq");
        MessageConsumer consumer = session.createConsumer(topic);
        // 第六步：通过consumer接收信息:通过监听器接收
        consumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message message) {
                // 第七步：处理信息
                if (message instanceof TextMessage) {
                    TextMessage tm = (TextMessage) message;
                    try {
                        String text = tm.getText();
                        System.out.println(text);
                    } catch (JMSException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        System.in.read();
        // 第八步：关闭资源
        consumer.close();
        session.close();
        connection.close();
    }



    @Test
    public void testSpringProducer() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String("application-context.xml"));
        // 通过applicationContext获取JmsTemplate
        JmsTemplate jmsTemplate = (JmsTemplate) ctx.getBean("jmsTemplate");
        // 通过applicationContext获取destination
        Destination destination = (Destination) ctx.getBean("queueDestination");
        // 通过jmstemplate发送消息
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("hello spring jmstemplate send message");
            }
        });
    }


    @Test
    public void testSpringProducer2() throws Exception{
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String("application-context.xml"));
        // 通过applicationContext获取JmsTemplate
        JmsTemplate jmsTemplate = (JmsTemplate) ctx.getBean("jmsTemplate");
        // 通过applicationContext获取destination
        Topic destination = (Topic) ctx.getBean("topicDestination");
        // 通过jmstemplate发送消息
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("hello spring jmstemplate send message*************");
            }
        });
    }

    @Test
    public void testSpringConsumer() throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String("application-context.xml"));
        System.in.read();
    }
}
