package rabbitHutch.aggregator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import rabbitHutch.Exchanges;
import rabbitHutch.F;

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
		
		String inputQueue = channel.queueDeclare().getQueue();
		channel.queueBind(inputQueue, Exchanges.sensorData, "#");
		channel.queueBind(inputQueue, Exchanges.actuatorCmds, "");

		// Setup aggregator
		Aggregator aggregator = new Aggregator();
		Map<String, Consumer<String>> handlerActions = new HashMap<>();
		handlerActions.put(Exchanges.sensorData, (String msg) -> updateSensorData(aggregator, msg));
		handlerActions.put(Exchanges.actuatorCmds, (String msg) -> updateActuatorCmd(aggregator, msg));

		
		// TODO: Receive messages from sensorData and actuatorCmds exchanges and pass them to 
		// aggregator.updateTemp() and aggregator.updateHeaterOn()
		// Additionally, every 3 seconds, this process should send all summaries 
		// (aggregator.buildingSummaries()) to the buildingState exchange (one summary per message).
		channel.basicConsume(inputQueue, F.autoAck, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msgReceived = new String(body);
				System.out.println("aggregator> received " + msgReceived);
				
				String exchange = envelope.getExchange();
				Consumer<String> action = handlerActions.get(exchange);
				if (action != null) {
					action.accept(msgReceived);
				}
			}

		});
		
		while (true) {
			List<String> summary = aggregator.buildingSummaries();
			String summaryString = summary.stream().collect(Collectors.joining("\n"));

			System.out.println("aggregator> emit " + summaryString);

			channel.basicPublish(Exchanges.buildingState, "", null, summaryString.getBytes());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private static void updateActuatorCmd(Aggregator aggregator, String msgReceived) {
		String[] parts = msgReceived.split(" ");
		String room = parts[0];
		boolean heaterOn = Boolean.parseBoolean(parts[1]);
		aggregator.updateHeaterOn(room, heaterOn);
	}
	
	private static void updateSensorData(Aggregator aggregator, String msgReceived) {
		String[] parts = msgReceived.split(" ");
		String room = parts[0];
		int temp = Integer.parseInt(parts[1]);
		aggregator.updateTemp(room, temp);
	}
}
