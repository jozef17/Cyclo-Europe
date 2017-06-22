package sk.blazicek.cycloEurope.server.tasks;

import static sk.blazicek.cycloEurope.server.tasks.ExtractParameters.getLonLat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import sk.blazicek.cycloEurope.server.database.Database;

/**
 * @author Jozef Blazicek
 * */
public abstract class HTTPRequestProcessing implements HttpHandler {
	protected static final Logger LOG = initLogger();
	protected Database db = new Database();
	String log;

	protected static Logger initLogger(){
		return Logger.getLogger(HTTPRequestProcessing.class.getName());
	}
	
	public HTTPRequestProcessing(String log) {
		this.log = log;
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		LOG.log(Level.INFO, log + " : " + httpExchange.getRemoteAddress().getAddress().toString() + "/" + log + "?"
				+ httpExchange.getRequestURI().getQuery());

		String param = httpExchange.getRequestURI().getQuery();
		double[] lonLat = getLonLat(param);
		int code = 400;
		String message;

		if (param == null) {
			code = 400;
			message = "Error Bad Request";
		} else if (lonLat == null) {
			code = 406;
			message = "Error Not Acceptable";
		} else {
			code = 200;
			message = processRequest(param, lonLat);
		}

		httpExchange.sendResponseHeaders(code, message.length());
		OutputStream outputStream = httpExchange.getResponseBody();
		outputStream.write(message.getBytes());
		outputStream.close();
	}

	protected String processRequest(String param, double[] lonLat) {
		String query = createQuery(param, lonLat);

		db.connect();
		List<String> fromDb = db.select(query);
		List<String> aditionalData = aditionalData(param, lonLat);
		db.close();

		if (aditionalData != null)
			fromDb.addAll(aditionalData);
		return merge(fromDb);
	}

	private String createQuery(String param, double[] lonLat) {
		List<String> restrictions = new ArrayList<String>();

		String query = 
				"WITH closest AS ( " + 
				"    SELECT ref, ST_Distance(ST_GeomFromText('POINT(" + lonLat[0] + " " + lonLat[1] + ")',4326), ST_Transform(way,4326) ) AS distance " +
				"    FROM planet_osm_line " + 
				"    WHERE (((bicycle IS NOT NULL ) AND (bicycle <> 'no')) OR (highway = 'cycleway') OR ((route = 'mtb') OR (route='bicycle'))) AND REF IS NOT NULL " +
				"    ORDER BY distance ASC LIMIT 1 ), " + 
				"road AS(" + 
				"    SELECT line.way, line.ref as prop " +
				"    FROM planet_osm_line AS line, closest " + "WHERE line.ref = closest.ref )";

		// Extending restrictions if has been chosen
		if (param.contains("shop=true"))
			restrictions.add("(shop = 'bicycle')");
		if (param.contains("glass=true"))
			restrictions.add(
					"(((amenity like '%bar%') OR (amenity like '%caffe%')  OR (amenity like '%food%') OR (amenity = 'pub')))");
		if (param.contains("sleep=true"))
			restrictions.add(
					"(((tourism = 'hotel') OR (tourism = 'motel') OR (tourism = 'hostel') OR (tourism = 'apartment')))");

		// If no restriction chosen - return select
		if (restrictions.size() == 0) {
			return query + " SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop FROM road;";
		}
		// Else extend query
		StringBuilder sb = new StringBuilder();
		sb.append(", point AS( SELECT * FROM planet_osm_point WHERE " + restrictions.get(0));
		for (int j = 1; j < restrictions.size(); j++)
			sb.append(" OR " + restrictions.get(j));
		sb.append(") ");

		sb.append(
				"SELECT ST_AsGeoJson(ST_Transform(p.way,4326)) AS geojson, p.name AS prop, p.shop, p.amenity, p.tourism FROM point AS p, road AS r " + 
				"WHERE ST_DWithin(r.way,p.way,1000) " +
				"UNION ALL " + 
				"SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop, null, null, null " +
				"FROM road;");
		return query + sb.toString();
	}

	private String merge(List<String> fromDb) {
		StringBuilder json = new StringBuilder();
		json.append("{\"type\": \"FeatureCollection\", \"features\": [" + fromDb.get(0));

		for (int i = 1; i < fromDb.size(); i++) {
			json.append("," + fromDb.get(i));
		}
		json.append("]}");
		return json.toString();
	}

	protected abstract List<String> aditionalData(String param, double[] lonLat);
}
