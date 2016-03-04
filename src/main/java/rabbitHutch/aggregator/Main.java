package rabbitHutch.aggregator;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Main {
	public static void main(String[] args) throws Exception {
		// Setup channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();

		// Declare topology
		// TODO

		// Setup aggregator
		Aggregator aggregator = new Aggregator();

		// TODO: Receive messages from sensorData and actuatorCmds exchanges and pass them to 
		// aggregator.updateTemp() and aggregator.updateHeaterOn()
		// Additionally, every 3 seconds, this process should send all summaries 
		// (aggregator.buildingSummaries()) to the buildingState exchange (one summary per message).
	}
}
