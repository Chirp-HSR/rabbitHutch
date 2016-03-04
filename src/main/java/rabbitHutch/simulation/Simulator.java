package rabbitHutch.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Simulator {
	private final Random random = new Random();
	private final int outsideTemp = 15000;
	private final int heaterTemp = 30000;
	private final Map<String, Integer> tempPerRoom = new ConcurrentHashMap<>();
	private final Map<String, Boolean> heaterOnPerRoom = new ConcurrentHashMap<>();

	public Simulator(Set<String> rooms) {
		for (String r : rooms) {
			tempPerRoom.put(r, random.nextInt(8000) + 17000);
			heaterOnPerRoom.put(r, random.nextBoolean());
		}
	}

	public void step() {
		for (Entry<String, Integer> entry : tempPerRoom.entrySet()) {
			String room = entry.getKey();
			int temp = entry.getValue();

			double coefficient;
			int refTemp;
			if (heaterOnPerRoom.get(room)) {
				coefficient = -0.001 * (random.nextGaussian() / 10 + 1);
				refTemp = heaterTemp;
			} else {
				coefficient = -0.003 * (random.nextGaussian() / 10 + 1);
				refTemp = outsideTemp;
			}

			int newTemp = (int) (refTemp + (temp - refTemp) * Math.exp(coefficient));

			tempPerRoom.put(room, newTemp);
		}
	}
	
	public List<String> getRooms(){
		return new ArrayList<>(tempPerRoom.keySet());
	}

	public int getTemp(String room) {
		return tempPerRoom.get(room);
	}

	public void setHeaterOn(String room, boolean on) {
		heaterOnPerRoom.put(room, on);
	}
	
	public Entry<String, Integer> getTempInRandomRoom(){
		ArrayList<Entry<String, Integer>> rooms = new ArrayList<>(tempPerRoom.entrySet());
		return rooms.get(random.nextInt(rooms.size()));
	}
}
