package rabbitHutch.aggregator;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Aggregator {

	private final Map<String, RoomState> entries = new ConcurrentHashMap<>();

	public void updateTemp(String roomName, int temp) {
		RoomState oldEntry = entries.getOrDefault(roomName, RoomState.unknown);
		RoomState newEntry = new RoomState(Optional.of(temp), oldEntry.heaterOn);
		entries.put(roomName, newEntry);
	}

	public void updateHeaterOn(String roomName, boolean heaterOn) {
		RoomState oldEntry = entries.getOrDefault(roomName, RoomState.unknown);
		RoomState newEntry = new RoomState(oldEntry.temp, Optional.of(heaterOn));
		entries.put(roomName, newEntry);
	}

	public List<String> buildingSummaries() {
		Map<String, List<Map.Entry<String, RoomState>>> roomsWithStatePerBuilding = entries
				.entrySet()
				.stream()
				.sorted(Comparator.comparing(e -> e.getKey()))
				.collect(Collectors.groupingBy(e -> e.getKey().split("\\.")[0]));

		return roomsWithStatePerBuilding
				.entrySet()
				.stream()
				.map(buildingWithRoomsAndStates -> {
					String roomSummary = buildingWithRoomsAndStates.getValue()
							.stream()
							.map(roomWithState -> "(room: " + roomWithState.getKey() + " " + roomWithState.getValue() + ")")
							.collect(Collectors.joining(" "));
		
					return buildingWithRoomsAndStates.getKey() + " " + new Date() + " " + roomSummary;
				})
				.collect(Collectors.toList());
	}

	static class RoomState {
		public final Optional<Integer> temp;
		public final Optional<Boolean> heaterOn;

		public RoomState(Optional<Integer> temp, Optional<Boolean> heaterOn) {
			this.temp = temp;
			this.heaterOn = heaterOn;
		}

		public static RoomState unknown = new RoomState(Optional.empty(), Optional.empty());

		@Override
		public String toString() {
			String tempStr = temp.map(Object::toString).orElse("-");
			String onStr = heaterOn.map(Object::toString).orElse("-");
			return "t: " + tempStr + " on: " + onStr;
		}
	}
}
