package rabbitHutch.aggregator;

import java.io.IOException;
import java.util.List;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

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
		Exchanges.declareActuatorCmds(channel);
		Exchanges.declareSensorData(channel);
		Exchanges.declareBuildingState(channel);

		String dataQueue = channel.queueDeclare().getQueue();
		channel.queueBind(dataQueue, Exchanges.sensorData, "#");

		String cmdQueue = channel.queueDeclare().getQueue();
		channel.queueBind(cmdQueue, Exchanges.actuatorCmds, "");

		// Setup aggregator
		Aggregator aggregator = new Aggregator();

		// Receiver (sensor data)
		channel.basicConsume(dataQueue, F.autoAck, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msgReceived = new String(body);

				String[] parts = msgReceived.split(" ");
				String room = parts[0];
				int temp = Integer.parseInt(parts[1]);

				aggregator.updateTemp(room, temp);
			}
		});

		// Receiver (commands)
		channel.basicConsume(cmdQueue, F.autoAck, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msgReceived = new String(body);

				String[] parts = msgReceived.split(" ");
				String room = parts[0];
				boolean on = Boolean.parseBoolean(parts[1]);

				aggregator.updateHeaterOn(room, on);
			}
		});

		// Sender (publish aggregated room overviews)
		while (true) {
			List<String> msgs = aggregator.buildingSummaries();
			
			msgs.forEach(msg -> {
				try {
					channel.basicPublish(Exchanges.buildingState, "", null, msg.getBytes());
				} catch (Exception e) {
				}
			});

			Thread.sleep(3000);
		}
	}
}
