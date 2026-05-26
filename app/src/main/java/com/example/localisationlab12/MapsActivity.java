package com.example.localisationlab12; // Declares the Java package used by this Android application.

import android.os.Bundle; // Passes activity state during lifecycle callbacks.
import android.widget.Toast; // Shows short messages when loading positions fails.

import androidx.annotation.NonNull; // Marks callback parameters that cannot be null.
import androidx.fragment.app.FragmentActivity; // Provides fragment support for SupportMapFragment.

import com.android.volley.Request; // Provides HTTP method constants for Volley.
import com.android.volley.RequestQueue; // Holds and runs Volley network requests.
import com.android.volley.toolbox.JsonObjectRequest; // Loads JSON objects from the PHP backend.
import com.android.volley.toolbox.Volley; // Creates the Volley request queue.
import com.google.android.gms.maps.CameraUpdateFactory; // Moves and zooms the Google Map camera.
import com.google.android.gms.maps.GoogleMap; // Represents the Google Map object.
import com.google.android.gms.maps.OnMapReadyCallback; // Receives the map when it is ready.
import com.google.android.gms.maps.SupportMapFragment; // Displays Google Maps inside this activity.
import com.google.android.gms.maps.model.LatLng; // Stores latitude and longitude for markers.
import com.google.android.gms.maps.model.MarkerOptions; // Configures Google Maps markers.

import org.json.JSONArray; // Reads the positions array from the backend JSON.
import org.json.JSONException; // Handles JSON parsing errors.
import org.json.JSONObject; // Reads each JSON position object.

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback { // Activity that displays saved positions on Google Maps.

    private GoogleMap googleMap; // Stores the map instance after Google Maps is ready.
    private RequestQueue requestQueue; // Reuses Volley networking resources for the JSON request.
    private final String showUrl = "http://192.168.43.228/localisation/showPositions.php"; // Replace 192.168.43.228 with the real PC IPv4 address; do not use localhost on Android.

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Runs when the activity is created.
        super.onCreate(savedInstanceState); // Lets Android initialize the activity superclass.
        setContentView(R.layout.activity_maps); // Loads the XML layout that contains the map fragment.

        requestQueue = Volley.newRequestQueue(this); // Creates the Volley queue used to load saved positions.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // Finds the SupportMapFragment declared in XML.
        if (mapFragment != null) { // Checks that the map fragment exists before using it.
            mapFragment.getMapAsync(this); // Asks Google Maps to notify this activity when the map is ready.
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) { // Runs when Google Maps has finished loading.
        googleMap = map; // Saves the ready map instance for marker operations.
        loadPositionsFromServer(); // Loads saved positions from the PHP backend.
    }

    private void loadPositionsFromServer() { // Sends the Volley POST request to showPositions.php.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, showUrl, null, // Creates a POST request that expects a JSON object response.
                response -> parsePositions(response), // Parses markers when the backend returns JSON.
                error -> Toast.makeText(MapsActivity.this, "Erreur chargement positions : " + error.getMessage(), Toast.LENGTH_LONG).show()); // Shows a message when the request fails.

        requestQueue.add(request); // Adds the JSON request to the Volley queue.
    }

    private void parsePositions(JSONObject response) { // Converts backend JSON into map markers.
        try { // Protects parsing code from malformed JSON responses.
            JSONArray positions = response.getJSONArray("positions"); // Reads the required positions array.
            LatLng firstPosition = null; // Remembers the first marker location for camera movement.

            for (int i = 0; i < positions.length(); i++) { // Loops over every saved position returned by PHP.
                JSONObject position = positions.getJSONObject(i); // Reads one position object from the array.
                double latitude = position.getDouble("latitude"); // Parses the latitude string or number.
                double longitude = position.getDouble("longitude"); // Parses the longitude string or number.
                String date = position.optString("date"); // Reads the saved datetime for the marker title.
                String imei = position.optString("imei"); // Reads the device identifier for marker details.
                LatLng markerPosition = new LatLng(latitude, longitude); // Creates the Google Maps coordinate object.
                String markerTitle = !date.isEmpty() ? date : imei; // Uses the date as title, with device id as fallback.

                googleMap.addMarker(new MarkerOptions().position(markerPosition).title(markerTitle).snippet(imei)); // Adds one marker for this saved position.

                if (firstPosition == null) { // Captures the first marker only once.
                    firstPosition = markerPosition; // Stores the first position for the camera.
                }
            }

            if (firstPosition != null) { // Moves the camera only when at least one position exists.
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 14f)); // Centers and zooms the map on the first saved marker.
            }
        } catch (JSONException exception) { // Handles missing or invalid JSON fields.
            Toast.makeText(this, "JSON invalide : " + exception.getMessage(), Toast.LENGTH_LONG).show(); // Shows a parsing error for lab debugging.
        }
    }
}
