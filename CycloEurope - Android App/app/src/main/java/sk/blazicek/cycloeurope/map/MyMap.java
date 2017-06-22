package sk.blazicek.cycloeurope.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.List;

import sk.blazicek.cycloeurope.MainActivity;
import sk.blazicek.cycloeurope.MyLocation;
import sk.blazicek.cycloeurope.R;

/**
 * Handle map interaction
 *
 * @author Jozef Blažíček
 */
public class MyMap {
    private MainActivity activity;
    private MapboxMap map;
    private LatLng location = new LatLng(48.1485965, 17.1077477);

    private Icon[] icons = null;

    public MyMap(MainActivity activity) {
        this.activity = activity;

        IconFactory iconFactory = IconFactory.getInstance(activity);

        Bitmap iconShop = BitmapFactory.decodeResource(activity.getResources(), R.drawable.bicycle);
        Bitmap iconSleep = BitmapFactory.decodeResource(activity.getResources(), R.drawable.sleep);
        Bitmap iconGlass = BitmapFactory.decodeResource(activity.getResources(), R.drawable.bar);

        icons = new Icon[3];
        icons[0] = iconFactory.fromBitmap(iconShop);
        icons[1] = iconFactory.fromBitmap(iconSleep);
        icons[2] = iconFactory.fromBitmap(iconGlass);

        MyLocation myLocation = activity.getMyLocation();
        if (myLocation != null && myLocation.showGPS() && myLocation.getLocation() != null)
            location = myLocation.getLocation();
    }

    /**
     * Updates map after receiving response from server
     */
    public void update(List<Element> elements) {
        map.clear();
        map.addMarker(new MarkerViewOptions().position(location).title(getTitle()));

        for (Element e : elements)
            e.display(map);
    }


    /**
     * @return current location (where is red pin) on map
     */
    public LatLng getLocation() {
        return location;
    }

    /**
     * Updates location on map and requests aupdate from server
     */
    public void setLocation(LatLng location) {
        this.location = location;
        map.clear();
        map.addMarker(new MarkerViewOptions().position(location).title(getTitle()));
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        activity.getMyConnection().callUpdate();
    }

    public void setMap(MapboxMap map) {
        this.map = map;
        map.clear();
        map.addMarker(new MarkerViewOptions().position(location).title(getTitle()));
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    public Icon[] getIcons() {
        return icons;
    }

    /**
     * @return current location as text
     */
    private String getTitle() {
        return String.format("%.4f %.4f", location.getLongitude(), location.getLatitude());
    }
}
