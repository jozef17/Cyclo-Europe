package sk.blazicek.cycloeurope.map;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Element to be displayed on map
 *
 * @author Jozef Blazicek
 */
public abstract class Element {

    /***
     * Process message from server (GeoJson) into list of Element objects
     */
    public static List<Element> processGeoJson(String message, Icon[] icons) {
        List<Element> elements = new ArrayList<Element>();
        List<Element> line = new ArrayList<Element>();
        String firstLine = null;

        try {
            JSONObject json = new JSONObject(message);
            JSONArray features = json.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject geometry = features.getJSONObject(i);
                String type = geometry.getString("type");

                switch (type) {
                    case "LineString":
                        String l = geometry.getJSONObject("properties").getString("prop");

                        if (firstLine == null)
                            firstLine = l;

                        if (l.equals(firstLine)) {
                            line.add(new LineElement(geometry, true));
                        } else {
                            elements.add(new LineElement(geometry, false));
                        }

                        break;
                    case "Point":
                        elements.add(new PointElement(geometry, icons));
                        break;
                }
            }
            elements.addAll(line);
        } catch (Exception e) {
            // If error has occurred - return empty list
            return new ArrayList<Element>();
        }
        return elements;
    }

    public abstract void display(MapboxMap map);
}
