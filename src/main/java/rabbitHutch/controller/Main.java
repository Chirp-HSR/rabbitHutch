package rabbitHutch.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import rabbitHutch.F;
import rabbitHutch.Exchanges;

public class Main {
	public static void main(String[] args) throws IOException, TimeoutException {
		// Setup channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();
		
		// Declare topology
		Exchanges.declareActuatorCmds(channel);
		Exchanges.declareSensorData(channel);

		String dataQueue = channel.queueDeclare().getQueue();
		channel.queueBind(dataQueue, Exchanges.sensorData, "");

		// Receiver (react to received temperatures and publish heater on/off commands)
		channel.basicConsume(dataQueue, F.autoAck, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msgReceived = new String(body);

				String[] parts = msgReceived.split(" ");
				String room = parts[0];
				int temp = Integer.parseInt(parts[1]);
				
				System.out.println("controller> received " + room + " " + temp);

				Optional<Boolean> newHeaterState = calcNewHeaterState(temp);

				if (newHeaterState.isPresent()) {
					String msg = room + " " + newHeaterState.get();

					System.out.println("controller> emit " + msg);

					channel.basicPublish(Exchanges.actuatorCmds, "", null, msg.getBytes());
				}
			}
		});
	}

	public static Optional<Boolean> calcNewHeaterState(int temp) {
		if (temp > 24000) {
			return Optional.of(false);
		} else if (temp < 18000) {
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}
}
