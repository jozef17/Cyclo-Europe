package sk.blazicek.cycloEurope.server.tasks;

import java.util.List;

/**
 * Finds closest cycleway and connecting cycleways
 * 
 * @author Jozef Blazicek
 */
public class Connected extends HTTPRequestProcessing {

	public Connected() {
		super("connected");
	}
	
	@Override
	protected List<String> aditionalData(String param, double[] lonLat) {
		String query = createQuery(param, lonLat);
		return db.select(query);
	}
	
	private String createQuery(String param, double[] lonLat) {
		return
			"WITH " +
			"closest AS ( " +
			"    SELECT ref, ST_Distance(ST_GeomFromText('POINT(31.0 31.0)',4326), ST_Transform(way,4326) ) AS distance " +
			"    FROM planet_osm_line " +
			"    WHERE (((bicycle IS NOT NULL ) AND (bicycle <> 'no')) OR (highway = 'cycleway') OR ((route = 'mtb') OR (route='bicycle'))) AND REF IS NOT NULL " +
			"    ORDER BY distance ASC LIMIT 1 ), " +
			"road AS( " +
			"    SELECT line.way, line.ref as prop " +
			"    FROM planet_osm_line AS line,  closest " +
			"    WHERE line.ref = closest.ref ), " + 
			"roads AS( " +
			"    SELECT line.way, line.ref as prop " +
			"    FROM  planet_osm_line AS line, road " +
			"    WHERE line.ref <> road.prop AND ST_DWithin(line.way,road.way,0)" +
		    "    AND (((bicycle IS NOT NULL ) AND (bicycle <> 'no')) OR (highway = 'cycleway') OR ((route = 'mtb') OR (route='bicycle'))) AND REF IS NOT NULL )" +
			"SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop from roads;";
	}
}
