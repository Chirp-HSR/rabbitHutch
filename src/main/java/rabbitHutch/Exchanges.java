package rabbitHutch;

import java.io.IOException;

import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.Channel;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Exchanges {
	public final static String sensorData = "sensor_data";
	
	public final static String actuatorCmds = "actuator_cmds"; 
	
	public final static String buildingState = "building_state"; 
	
	public final static String buildingPlan = "building_plan";
	
	public static DeclareOk declareSensorData(Channel channel) throws IOException {
		return channel.exchangeDeclare(sensorData, "direct");
	}
	
	public static DeclareOk declareActuatorCmds(Channel channel) throws IOException {
		return channel.exchangeDeclare(actuatorCmds, "direct");
	}
	
	public static DeclareOk declareBuildingState(Channel channel) throws IOException {
		return channel.exchangeDeclare(buildingState, TODO());
	}
	
	public static DeclareOk declareBuildingPlan(Channel channel) throws IOException {
		return channel.exchangeDeclare(buildingPlan, TODO());
	}
	
	public static String TODO(){
		throw new NotImplementedException();
	}
}
