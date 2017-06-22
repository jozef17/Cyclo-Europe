package sk.blazicek.cycloeurope.map;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Jozef Blazicek
 */
public class PointElement extends Element {
    private JSONObject geometry;
    private Icon[] icons;

    public PointElement(JSONObject geometry, Icon[] icons) {
        this.geometry = geometry;
        this.icons = icons;
    }

    /**
     * Displays point on map
     */
    @Override
    public void display(MapboxMap map) {
        try {
            Icon icon = icons[geometry.getJSONObject("properties").getInt("type")];
            String text = geometry.getJSONObject("properties").getString("prop");

            JSONArray coordinates = geometry.getJSONArray("coordinates");
            LatLng point = new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));

            if (text.equals("-"))
                map.addMarker(new MarkerOptions().position(point).icon(icon));
            else
                map.addMarker(new MarkerOptions().position(point).icon(icon).setTitle(text));
        } catch (Exception ignored) {
            // If error has occurred - don't display anything
        }
    }
}
