package rabbitHutch;

import java.io.IOException;

import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.Channel;


public class Exchanges {
	public final static String sensorData = "sensor_data";
	
	public final static String actuatorCmds = "actuator_cmds"; 
	
	public final static String buildingState = "building_state"; 
	
	public final static String buildingPlan = "building_plan";
	
	public static DeclareOk declareSensorData(Channel channel) throws IOException {
		return channel.exchangeDeclare(sensorData, "topic");
	}
	
	public static DeclareOk declareActuatorCmds(Channel channel) throws IOException {
		return channel.exchangeDeclare(actuatorCmds, "fanout");
	}
	
	public static DeclareOk declareBuildingState(Channel channel) throws IOException {
		return channel.exchangeDeclare(buildingState, "fanout");
	}
	
	public static DeclareOk declareBuildingPlan(Channel channel) throws IOException {
		return channel.exchangeDeclare(buildingPlan, "fanout");
	}
	
	public static String TODO(){ //TODO
		throw new UnsupportedOperationException();
	}
}
