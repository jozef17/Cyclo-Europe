package sk.blazicek.cycloeurope;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.nio.ByteOrder;

import sk.blazicek.cycloeurope.map.Element;

/**
 * Handle network communication
 *
 * @author Jozef Blazicek
 */
public class MyConnection implements Response.Listener<String>, Response.ErrorListener {
    private MainActivity activity;

    private String serverIp = null;
    private int port = 1234;

    public MyConnection(MainActivity activity) {
        this.activity = activity;

        if (serverIp == null) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 0);
            }
            discoverServer();
        }
    }

    /**
     * Local network server discovery
     * Generates every possible IP address and sends request to :1234/Identify
     */
    private void discoverServer() {
        DhcpInfo dhcpInfo = ((WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getDhcpInfo();

        int minIp, maxIp;
        int deviceIp = dhcpInfo.ipAddress;
        int mask = dhcpInfo.netmask;
        int maskCode = 1;

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            deviceIp = Integer.reverseBytes(deviceIp);
            mask = Integer.reverseBytes(mask);
        }

        for (; maskCode <= 4 * 8; maskCode++) {
            if (((mask >> maskCode) & 1) == 1)
                break;
        }

        minIp = deviceIp & mask;
        maxIp = (Integer.MAX_VALUE >> (8 * 4 - maskCode - 1)) | minIp;

        RequestQueue queue = Volley.newRequestQueue(activity);

        for (int i = minIp; i < maxIp && serverIp == null; i++) {
            StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, "http://" + getIp(i) + ":" + port + "/identify", this, this);
            queue.add(stringRequest);
        }
    }

    /**
     * Convert number into IPv4 string
     */
    private String getIp(int ip) {
        int first = (ip & 0xFF000000) >>> 24;
        int second = (ip & 0xFF0000) >>> 16;
        int third = (ip & 0xFF00) >>> 8;
        int fourth = ip & 0xFF;

        return first + "." + second + "." + third + "." + fourth;
    }

    /**
     * Sends HTTP request
     */
    public void send(String uri) {
        if (serverIp == null) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(activity);
        StringRequest request = new StringRequest(com.android.volley.Request.Method.GET, "http://" + serverIp + ":" + port + "/" + uri, this, this);
        request.setRetryPolicy(new DefaultRetryPolicy(1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /**
     * Sending request for update to a server
     */
    public void callUpdate() {
        Menu menu = activity.getMenu();
        LatLng location = activity.getMyMap().getLocation();

        if (menu == null)
            return;

        // Setting parameters of request
        StringBuilder param = new StringBuilder();
        if (menu.findItem(R.id.glass).isChecked()) {
            param.append("&glass=true");
        }
        if (menu.findItem(R.id.sleep).isChecked()) {
            param.append("&sleep=true");
        }
        if (menu.findItem(R.id.shop).isChecked()) {
            param.append("&shop=true");
        }

        if (menu.findItem(R.id.single_way).isChecked()) {
            send("closest?long=" + location.getLongitude() + "&lat=" + location.getLatitude() + param.toString());
        } else if (menu.findItem(R.id.connected_ways).isChecked()) {
            send("connected?long=" + location.getLongitude() + "&lat=" + location.getLatitude() + param.toString());
        }
    }

    /**
     * Process Response
     */
    @Override
    public void onResponse(String response) {
        if (response.contains("{")) {
            activity.getMyMap().update(Element.processGeoJson(response, activity.getMyMap().getIcons()));
        } else {
            serverIp = response;

            // Enable options if server found
            Menu menu = activity.getMenu();
            menu.findItem(R.id.shop).setEnabled(true);
            menu.findItem(R.id.glass).setEnabled(true);
            menu.findItem(R.id.sleep).setEnabled(true);
            menu.findItem(R.id.connected_ways).setEnabled(true);
            menu.findItem(R.id.single_way).setEnabled(true);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
    }

    public boolean serverFound() {
        return serverIp != null;
    }
}
