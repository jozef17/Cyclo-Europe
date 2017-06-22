package sk.blazicek.cycloeurope.map;

import android.graphics.Color;

import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jozef Blazicek
 */
public class LineElement extends Element {
    private JSONObject geometry;
    private boolean first;

    public LineElement(JSONObject geometry, boolean first) {
        this.geometry = geometry;
        this.first = first;
    }

    /**
     * Displays Cycle Way on map
     */
    @Override
    public void display(MapboxMap map) {
        try {
            if (first) {
                map.addPolyline(new PolylineOptions().addAll(getLineCoordinates(geometry.getJSONArray("coordinates"))).color(Color.BLUE).width(5));
            } else {
                map.addPolyline(new PolylineOptions().addAll(getLineCoordinates(geometry.getJSONArray("coordinates"))).color(Color.RED).width(3));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * @return line as list of points extracted from geoJson
     */
    private List<LatLng> getLineCoordinates(JSONArray jsonArray) throws JSONException {
        List<LatLng> points = new ArrayList<LatLng>();

        // Add points of line into list
        for (int j = 0; j < jsonArray.length(); j++) {
            points.add(new LatLng(jsonArray.getJSONArray(j).getDouble(1), jsonArray.getJSONArray(j).getDouble(0)));
        }
        return points;
    }

}
