package rabbitHutch.visualizer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Main {
	public final static String visWorkQueue = "vis_work"; 
	
	public static void main(String[] args) throws Exception {
		// Setup channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();

		// Declare topology
		// TODO

		// Init Visualizer
		Visualizer vis = new Visualizer();
		
		// TODO Receive messages from the buildingState exchange, transform them with
		// vis.visualize() and emit the result to the buildingPlan exchange.
	}
}
