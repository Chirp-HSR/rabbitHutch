package rabbitHutch.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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

		String cmdQueue = channel.queueDeclare().getQueue();
		channel.queueBind(cmdQueue, Exchanges.actuatorCmds, "");

		// Setup simulation
		List<String> rooms = new ArrayList<>();
		rooms.add("B1.R1");
		rooms.add("B1.R2");
		rooms.add("B1.R3");
		rooms.add("B2.R1");
		rooms.add("B2.R2");
		rooms.add("B3.R1");
		rooms.add("B3.R2");
		rooms.add("B3.R3");
		Simulator simulator = new Simulator(new HashSet<String>(rooms));

		// Receiver (handle heater on/off messages)
		channel.basicConsume(cmdQueue, F.autoAck, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msg = new String(body);

				System.out.println("simulator> received " + msg);

				String[] parts = msg.split(" ");
				String room = parts[0];
				boolean heaterOn = Boolean.parseBoolean(parts[1]);

				simulator.setHeaterOn(room, heaterOn);
			}
		});

		// Sender (produce room temperature messages)
		while (true) {
			simulator.step();

			Entry<String, Integer> roomWithTemp = simulator.getTempInRandomRoom();

			String msg = roomWithTemp.getKey() + " " + roomWithTemp.getValue();

			System.out.println("simulator> emit " + msg);

			channel.basicPublish(Exchanges.sensorData, "", null, msg.getBytes());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
}
