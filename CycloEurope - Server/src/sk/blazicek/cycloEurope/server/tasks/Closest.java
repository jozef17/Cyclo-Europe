package sk.blazicek.cycloEurope.server.tasks;

import java.util.List;

/**
 * Finds closest cycleway
 * 
 * @author Jozef Blazicek
 */
public class Closest extends HTTPRequestProcessing {

	public Closest() {
		super("closest");
	}

	@Override
	protected List<String> aditionalData(String param, double[] lonLat) {
		return null;
	}
}
