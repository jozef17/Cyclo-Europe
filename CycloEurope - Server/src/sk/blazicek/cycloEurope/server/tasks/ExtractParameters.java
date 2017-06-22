package sk.blazicek.cycloEurope.server.tasks;

/**
 * Extracts parameters from received HTTP request
 * 
 * @author Jozef Blazicek
 */
public abstract class ExtractParameters {

	/**
	 * @return array of extracted longitude (zero index) and latitude (first
	 *         index), null for incorrect input
	 */
	public static double[] getLonLat(String param) {
		int beginLon = param.indexOf("long=");
		int beginLat = param.indexOf("lat=");

		if (beginLon < 0 || beginLat < 0)
			return null;

		int endLon = -1;
		int endLat = -1;

		// Finds end of Longitude
		for (int i = beginLon + 6; i < param.length(); i++) {
			if ((((param.charAt(i) < '0') || (param.charAt(i) > '9')) && param.charAt(i) != '.')
					|| (i == param.length() - 1)) {
				endLon = i;
				break;
			}
		}

		// Finds end of Latitude
		for (int i = beginLat + 5; i < param.length(); i++) {
			if ((((param.charAt(i) < '0') || (param.charAt(i) > '9')) && param.charAt(i) != '.')
					|| (i == param.length() - 1)) {
				endLat = i;
				break;
			}
		}

		double location[] = new double[2];

		if (endLon > beginLon)
			location[0] = Double.parseDouble(param.substring(beginLon + 5, endLon));
		else
			return null;

		if (endLat > beginLat)
			location[1] = Double.parseDouble(param.substring(beginLat + 4, endLat));
		else
			return null;

		return location;
	}

}
