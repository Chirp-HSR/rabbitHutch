package rabbitHutch.monitor;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import rabbitHutch.F;
import rabbitHutch.Exchanges;

public class Main {
	public static void main(String[] args) throws Exception {
		// Setup channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		// Declare topology
		Exchanges.declareBuildingPlan(channel);

		String stateQueue = channel.queueDeclare().getQueue();
		channel.queueBind(stateQueue, Exchanges.buildingPlan, "");

		// Receiver (just print anything received)
		channel.basicConsume(stateQueue, F.autoAck, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msgReceived = new String(body);

				System.out.println(msgReceived);
			}
		});
	}
}
