package com.scale.tribal.spotter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "daren";
    private GoogleMap mMap;
    private ImageButton voiceInputBtn;
    private SpeechRecognizer sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        voiceInputBtn = findViewById(R.id.voice_input_btn);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        voiceInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                sr.startListening(intent);
                Log.i("111111","11111111");
                voiceInputBtn.setEnabled(false);
                voiceInputBtn.setAlpha(0.5f);
//                scaleView(voiceInputBtn, 1.2f, 1);
            }
        });
        navigateToMyLocation();
    }

    @SuppressLint("MissingPermission")
    private void navigateToMyLocation() {
        LocationManager lm = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location gps_loc;
        Location net_loc;
        if (gps_enabled) {
            gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng();

        }

        if (network_enabled)
            net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


//        mMap.animateCamera(cameraUpdate);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(sydney);
        mMap.moveCamera(cameraUpdate);
    }

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {

            Log.d(TAG,  "error " +  error);
//            mText.setText("error " + error);
        }
        public void onResults(Bundle results)
        {
            voiceInputBtn.setEnabled(true);
            voiceInputBtn.setAlpha(1.0f);

            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for (int i = 0; i < data.size(); i++)
            {
                String text = (String) data.get(i);
                Log.d(TAG, "result " + data.get(i));
                if(isFindCommand(text)) {
                    int parkingNearIndex = text.indexOf("parking near");

                    final String address = text.substring(parkingNearIndex, text.length());
                    Log.v(TAG, "address found " + address);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                navigateToAddress(address);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                }
                return;
            }
//            mText.setText("results: "+String.valueOf(data.size()));
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    private void navigateToAddress(String address) throws IOException {
        Geocoder gc = new Geocoder(this);
        List<Address> addresses = gc.getFromLocationName(address, 1);
        Log.v(TAG, "trying address " + address);
        if(addresses.size() > 0) {
            Address firstAddress = addresses.get(0);
            Log.v(TAG, "found addresses" + addresses.size());
            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(firstAddress.getLatitude(),
                            firstAddress.getLongitude()));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            mMap.animateCamera(center);
            mMap.animateCamera(zoom);
        }

    }

    private boolean isFindCommand(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("parking near");
    }

    public void scaleView(View v, float startScale, float endScale) {

        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, startScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_PARENT, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_PARENT, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(500);
        v.startAnimation(anim);
    }

}
