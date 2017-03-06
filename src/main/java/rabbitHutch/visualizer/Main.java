package rabbitHutch.visualizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

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

		// Init Visualizer
		Visualizer vis = new Visualizer();

		Map<String, Object> queueArgs = new HashMap<>();
		queueArgs.put("x-message-ttl", 1000);
		String visWorkQueue = channel.queueDeclare("VisWork", false, false, false, queueArgs).getQueue();
		
		channel.queueBind(visWorkQueue, Exchanges.buildingState, "");
		
		channel.basicQos(1);
		channel.basicConsume(visWorkQueue, false, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msg = new String(body);

				System.out.println("visualizer> received " + msg);
				String buildingPlan = vis.visualize(msg);
				channel.basicAck(envelope.getDeliveryTag(), false);
				channel.basicPublish(Exchanges.buildingPlan, "", null, buildingPlan.getBytes());
			}
		});
		
	
		
		// TODO Receive messages from the buildingState exchange, transform them with
		// vis.visualize() and emit the result to the buildingPlan exchange.
	}
}
