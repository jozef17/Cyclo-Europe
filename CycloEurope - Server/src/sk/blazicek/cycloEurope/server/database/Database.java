package sk.blazicek.cycloEurope.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Connect to database and perform selects
 * 
 * @author Jozef Blazicek
 */
public class Database {
	private Connection connection;

	/**
	 * Connects to database
	 */
	public boolean connect() {
		try {
			Properties properties = new Properties();
			properties.setProperty("user", /*insert username*/);
			properties.setProperty("password", /*insert password*/);
			connection = DriverManager.getConnection("jdbc:postgresql://localhost/gis", properties);
			connection.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return (connection != null);
	}

	/**
	 * Close Connection to database
	 */
	public boolean close() {
		try {
			connection.close();
			return connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Processes result into list of geoJson strings
	 */
	public List<String> select(String query) {
		List<String> result = null;

		if (connection == null)
			return null;

		try {
			if (connection.isClosed())
				return null;

			// Get data from database
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			result = new ArrayList<String>();

			// For every result do
			while (resultSet.next()) {
				String line = resultSet.getString("geojson").replaceAll(" ", "");
				String prop = resultSet.getString("prop");

				if (prop == null)
					prop = "-";
				else
					prop = prop.replace("\"", "\\\"");

				// If point, append with prop and type
				if (line.contains("Point")) {
					if (resultSet.getString("shop") != null) {
						result.add(line.substring(0, line.length() - 1) + ",\"properties\":{\"type\":0,\"prop\":\""
								+ prop + "\"}}");
					} else if (resultSet.getString("tourism") != null) {
						result.add(line.substring(0, line.length() - 1) + ",\"properties\":{\"type\":1,\"prop\":\""
								+ prop + "\"}}");
					} else {
						result.add(line.substring(0, line.length() - 1) + ",\"properties\":{\"type\":2,\"prop\":\""
								+ prop + "\"}}");
					}
				} else if (line.contains("LineString")) {
					result.add(line.substring(0, line.length() - 1) + ",\"properties\":{\"prop\":\"" + prop + "\"}}");
				}
			}
			statement.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
