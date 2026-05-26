package com.example.localisationlab12; // Declares the Java package used by this Android application.

import android.Manifest; // Provides Android permission constants.
import android.annotation.SuppressLint; // Allows local suppression for deprecated device-id fallback code.
import android.content.Context; // Provides access to Android system services.
import android.content.Intent; // Opens another activity from this activity.
import android.content.pm.PackageManager; // Checks whether runtime permissions are granted.
import android.location.Location; // Represents one GPS location fix.
import android.location.LocationListener; // Receives GPS location update callbacks.
import android.location.LocationManager; // Requests location updates from the GPS provider.
import android.os.Bundle; // Passes activity state during lifecycle callbacks.
import android.provider.Settings; // Reads ANDROID_ID from secure settings.
import android.telephony.TelephonyManager; // Keeps the legacy IMEI fallback when permission allows it.
import android.widget.Button; // Represents the button that opens the map screen.
import android.widget.TextView; // Displays latitude and longitude values.
import android.widget.Toast; // Shows short feedback messages to the user.

import androidx.annotation.NonNull; // Marks callback parameters that cannot be null.
import androidx.appcompat.app.AppCompatActivity; // Provides the base activity with AppCompat support.
import androidx.core.app.ActivityCompat; // Requests and checks runtime permissions compatibly.
import androidx.core.content.ContextCompat; // Checks permissions compatibly across Android versions.

import com.android.volley.Request; // Provides HTTP method constants for Volley requests.
import com.android.volley.RequestQueue; // Holds and runs Volley network requests.
import com.android.volley.toolbox.StringRequest; // Sends form-encoded POST requests with Volley.
import com.android.volley.toolbox.Volley; // Creates the Volley request queue.

import java.text.SimpleDateFormat; // Formats the current date for the PHP backend.
import java.util.Date; // Provides the current date and time.
import java.util.HashMap; // Stores POST parameters for Volley.
import java.util.Locale; // Ensures date formatting is stable across device languages.
import java.util.Map; // Defines the POST parameter map type.

public class MainActivity extends AppCompatActivity { // Main screen that collects and sends GPS positions.

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100; // Identifies the location permission request result.
    private TextView latitudeTextView; // Shows the latest latitude received from GPS.
    private TextView longitudeTextView; // Shows the latest longitude received from GPS.
    private LocationManager locationManager; // Manages GPS provider access.
    private RequestQueue requestQueue; // Reuses Volley networking resources for POST requests.
    private final String insertUrl = "http://192.168.43.228/localisation/createPosition.php"; // Replace 192.168.43.228 with the real PC IPv4 address; do not use localhost on Android.

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Runs when the activity is created.
        super.onCreate(savedInstanceState); // Lets Android initialize the activity superclass.
        setContentView(R.layout.activity_main); // Loads the XML layout for the main screen.

        latitudeTextView = findViewById(R.id.text_latitude); // Finds the TextView used to display latitude.
        longitudeTextView = findViewById(R.id.text_longitude); // Finds the TextView used to display longitude.
        Button mapButton = findViewById(R.id.button_map); // Finds the button that opens MapsActivity.
        requestQueue = Volley.newRequestQueue(this); // Creates the Volley queue used to contact the PHP backend.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // Gets the Android GPS location service.

        mapButton.setOnClickListener(view -> { // Runs when the user taps the map button.
            Intent intent = new Intent(MainActivity.this, MapsActivity.class); // Creates an intent for the map screen.
            startActivity(intent); // Opens MapsActivity so saved positions can be displayed.
        });

        checkLocationPermissionAndStartGps(); // Starts the permission flow before listening for GPS updates.
    }

    private void checkLocationPermissionAndStartGps() { // Checks permission before using GPS_PROVIDER.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { // Continues immediately when fine location is already granted.
            startGpsUpdates(); // Starts receiving location updates from GPS.
        } else { // Requests permission when it has not been granted yet.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE); // Shows the runtime permission dialog.
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsUpdates() { // Subscribes to GPS updates after permission has been granted.
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // Verifies that the device GPS provider is enabled.
            Toast.makeText(this, getString(R.string.provider_disabled), Toast.LENGTH_LONG).show(); // Tells the user GPS is disabled.
            return; // Stops until the user enables GPS.
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 150, locationListener); // Requests GPS updates every 60000 ms and 150 meters.
        Toast.makeText(this, getString(R.string.provider_enabled), Toast.LENGTH_SHORT).show(); // Confirms that GPS listening has started.
    }

    private final LocationListener locationListener = new LocationListener() { // Receives GPS provider callbacks.
        @Override
        public void onLocationChanged(@NonNull Location location) { // Runs when a new GPS position is available.
            double latitude = location.getLatitude(); // Reads the latitude from the GPS fix.
            double longitude = location.getLongitude(); // Reads the longitude from the GPS fix.
            latitudeTextView.setText(String.format(Locale.US, "Latitude : %.6f", latitude)); // Displays latitude on the screen.
            longitudeTextView.setText(String.format(Locale.US, "Longitude : %.6f", longitude)); // Displays longitude on the screen.
            Toast.makeText(MainActivity.this, getString(R.string.new_location), Toast.LENGTH_SHORT).show(); // Notifies the user that a new location was received.
            sendPositionToServer(latitude, longitude); // Sends the new location to the PHP backend.
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) { // Runs when the GPS provider is enabled.
            Toast.makeText(MainActivity.this, getString(R.string.provider_enabled), Toast.LENGTH_SHORT).show(); // Shows a provider enabled message.
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) { // Runs when the GPS provider is disabled.
            Toast.makeText(MainActivity.this, getString(R.string.provider_disabled), Toast.LENGTH_SHORT).show(); // Shows a provider disabled message.
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { // Keeps compatibility with older Android status callbacks.
            Toast.makeText(MainActivity.this, getString(R.string.provider_new_status), Toast.LENGTH_SHORT).show(); // Shows a generic provider status message.
        }
    };

    private void sendPositionToServer(double latitude, double longitude) { // Builds and sends the Volley POST request.
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()); // Formats the datetime expected by MySQL.
        String deviceId = getDeviceIdentifier(); // Gets ANDROID_ID or the permitted legacy fallback identifier.

        StringRequest request = new StringRequest(Request.Method.POST, insertUrl, // Creates a POST request to createPosition.php.
                response -> Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show(), // Displays the backend JSON response for lab debugging.
                error -> Toast.makeText(MainActivity.this, "Erreur serveur : " + error.getMessage(), Toast.LENGTH_LONG).show()) { // Displays a network error if the POST fails.
            @Override
            protected Map<String, String> getParams() { // Supplies form fields sent in the POST body.
                Map<String, String> params = new HashMap<>(); // Creates the POST parameter collection.
                params.put("latitude", String.valueOf(latitude)); // Sends latitude to PHP.
                params.put("longitude", String.valueOf(longitude)); // Sends longitude to PHP.
                params.put("date", currentDate); // Sends current datetime to PHP.
                params.put("imei", deviceId); // Sends the device identifier in the lab imei column.
                return params; // Returns the POST parameters to Volley.
            }
        };

        requestQueue.add(request); // Adds the POST request to the Volley queue.
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    private String getDeviceIdentifier() { // Returns the main Android identifier with a legacy fallback.
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); // Reads ANDROID_ID as the primary device identifier.
        if (androidId != null && !androidId.isEmpty()) { // Uses ANDROID_ID when Android returns a valid value.
            return androidId; // Returns the stable Android identifier.
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) { // Uses TelephonyManager only when READ_PHONE_STATE is already granted.
            try { // Protects modern Android versions that can still block hardware identifiers.
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // Gets the phone service for the legacy fallback.
                String legacyDeviceId = telephonyManager != null ? telephonyManager.getDeviceId() : null; // Reads the deprecated device id only as a fallback.
                if (legacyDeviceId != null && !legacyDeviceId.isEmpty()) { // Uses the fallback only when a value exists.
                    return legacyDeviceId; // Returns the legacy device identifier.
                }
            } catch (SecurityException ignored) { // Continues safely if Android blocks access to the legacy identifier.
                return "unknown_device"; // Returns a safe placeholder when the fallback is unavailable.
            }
        }

        return "unknown_device"; // Returns a safe placeholder if no identifier is available.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // Receives runtime permission results.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Lets the superclass process the permission result.
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Checks that fine location was approved.
            startGpsUpdates(); // Starts GPS updates after permission approval.
        } else { // Handles denied location permission.
            Toast.makeText(this, "Permission localisation refusee", Toast.LENGTH_LONG).show(); // Tells the user the app cannot access GPS.
        }
    }

    @Override
    protected void onDestroy() { // Runs when the activity is being destroyed.
        super.onDestroy(); // Lets Android clean up the activity superclass.
        if (locationManager != null) { // Checks that the location manager exists before removing updates.
            locationManager.removeUpdates(locationListener); // Stops GPS updates to avoid leaks.
        }
    }
}
