package rabbitHutch.visualizer;

import java.util.Random;

public class Visualizer {

	private final char decoration = (char) (new Random().nextInt(92) + 33);

	public String visualize(String buildingPlan) {
		// Expensive, blocking calculation...
		synchronized (this) {
			try {
				Thread.sleep(new Random().nextInt(3500));
			} catch (InterruptedException e) {
			}
			return "=" + decoration + "= " + buildingPlan;
		}
	}
}
