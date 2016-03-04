package rabbitHutch.visualizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import rabbitHutch.F;
import rabbitHutch.Exchanges;

public class Main {
	public final static String visWorkQueue = "vis_work"; 
	
	public static void main(String[] args) throws Exception {
		// Setup channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();

		// Declare topology
		Exchanges.declareBuildingState(channel);
		Exchanges.declareBuildingPlan(channel);

		Map<String, Object> queueArgs = new HashMap<>();
		queueArgs.put("x-message-ttl", 1000);
		channel.queueDeclare(visWorkQueue, F.durable, F.nonExclusive, F.autoDelete, queueArgs);
		channel.queueBind(visWorkQueue, Exchanges.buildingState, "");

		// Init Visualizer
		Visualizer vis = new Visualizer();
		
		// Receiver (receive building plan, visualize, publish)
		channel.basicQos(1);
		channel.basicConsume(visWorkQueue, F.noAutoAck, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String plan = new String(body);

				String visualized = vis.visualize(plan);
				
				channel.basicAck(envelope.getDeliveryTag(), false);
				channel.basicPublish(Exchanges.buildingPlan, "", null, visualized.getBytes());
			}
		});
	}
}
