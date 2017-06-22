package sk.blazicek.cycloeurope;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import sk.blazicek.cycloeurope.map.MyMap;

/**
 * @author Jozef Blazicek
 */
public class MainActivity extends AppCompatActivity {
    private Menu menu = null;
    private MapView mapView;

    private MyConnection myConnection;
    private MyLocation myLocation;
    private MyMap myMap;

    private boolean options[] = {false, false, false, false, false};

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            options = savedInstanceState.getBooleanArray("selection");
        }

        // Initialize local variables
        if (myLocation == null)
            myLocation = new MyLocation(this);
        if (myMap == null)
            myMap = new MyMap(this);
        if (myConnection == null)
            myConnection = new MyConnection(this);

        // Create a mapView
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Add a MapboxMap
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap map) {
                myMap.setMap(map);

                map.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        myMap.setLocation(point);
                    }
                });
            }
        });

        // Create Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (myLocation != null)
            menu.findItem(R.id.locate).setVisible(myLocation.showGPS());
        else
            menu.findItem(R.id.locate).setVisible(false);

        // Disable options if server wasn't found
        if (myConnection != null) {
            boolean serverFound = myConnection.serverFound();

            menu.findItem(R.id.shop).setEnabled(serverFound);
            menu.findItem(R.id.glass).setEnabled(serverFound);
            menu.findItem(R.id.sleep).setEnabled(serverFound);
            menu.findItem(R.id.connected_ways).setEnabled(serverFound);
            menu.findItem(R.id.single_way).setEnabled(serverFound);
        } else {
            menu.findItem(R.id.shop).setEnabled(false);
            menu.findItem(R.id.glass).setEnabled(false);
            menu.findItem(R.id.sleep).setEnabled(false);
            menu.findItem(R.id.connected_ways).setEnabled(false);
            menu.findItem(R.id.single_way).setEnabled(false);
        }

        // Set menu items state
        menu.findItem(R.id.shop).setChecked(options[0]);
        menu.findItem(R.id.glass).setChecked(options[1]);
        menu.findItem(R.id.sleep).setChecked(options[2]);

        if (options[3] || options[4]) {
            menu.findItem(R.id.connected_ways).setChecked(options[3]);
            menu.findItem(R.id.single_way).setChecked(options[4]);

            if (myConnection != null)
                myConnection.callUpdate();
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.glass:
            case R.id.sleep:
            case R.id.shop:
                item.setChecked(!item.isChecked());
                break;
            case R.id.single_way:
            case R.id.connected_ways:
                item.setChecked(true);
                break;
            case R.id.locate:
                LatLng location = myLocation.getLocation();
                if (location != null)
                    myMap.setLocation(location);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        myConnection.callUpdate();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        myLocation.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        myLocation.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

        if (menu != null) {
            options[0] = menu.findItem(R.id.shop).isChecked();
            options[1] = menu.findItem(R.id.glass).isChecked();
            options[2] = menu.findItem(R.id.sleep).isChecked();
            options[3] = menu.findItem(R.id.connected_ways).isChecked();
            options[4] = menu.findItem(R.id.single_way).isChecked();
        }

        outState.putBooleanArray("selection", options);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    public MyLocation getMyLocation() {
        return myLocation;
    }

    public MyMap getMyMap() {
        return myMap;
    }

    public MyConnection getMyConnection() {
        return myConnection;
    }

    /**
     * Enables from other classes to change menu
     */
    public Menu getMenu() {
        return menu;
    }
}
