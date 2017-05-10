package com.example.steve_000.clientapplication;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_IMAGE_GALLERY = 1;
    private GoogleApiClient mGoogleApiClient;
    private static final String[] COUNTRIES = new String[] {
            "Sverige", "Danmark", "Finland", "Norge"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton send = (FloatingActionButton) findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText)findViewById(R.id.host);
                String host = et.getText().toString();
                send(view, host);
            }
        });

        final LinearLayout ll = (LinearLayout) findViewById(R.id.popup_menu);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogle(ll);
            }
        });

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toogle(ll);
            }
        });

        FloatingActionButton position = (FloatingActionButton) findViewById(R.id.position_button);
        position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPosition(view);
            }
        });


        ImageButton gallery = (ImageButton) findViewById(R.id.image_gallery_button);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toogle(ll);
                gallery();
            }
        });

        ImageButton camera = (ImageButton) findViewById(R.id.image_camera_button);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toogle(ll);
                camera();
            }
        });

        Button connect = (Button) findViewById(R.id.connect_button);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogle(findViewById(R.id.connect_window));
            }
        });

        ImageView iv = (ImageView) findViewById(R.id.imageView);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallery();
            }
        });


        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.location_text);
        textView.setAdapter(adapter);

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        EditText et = (EditText) findViewById(R.id.location_text);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(et.isFocused()){
            et.clearFocus();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            toogle(findViewById(R.id.connect_window));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()) {
            case R.id.nav_camera:
                camera();
                break;
            case R.id.nav_gallery:
                gallery();
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void send(final View view, final String host) {
        final ImageView iv = (ImageView) this.findViewById(R.id.imageView);
        final EditText tv = (EditText) this.findViewById(R.id.location_text);
        Drawable img = iv.getDrawable();
        if (img == null) {
            Snackbar.make(view, "No image to send", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        final Bitmap bmp = ((BitmapDrawable) img).getBitmap();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectionHandler ch;
                if(host.equals(""))
                    ch = new ConnectionHandler();
                else
                    ch = new ConnectionHandler(host);

                JSONObject jo = new JSONObject();
                Bitmap scaled = bmp;
                if (bmp.getWidth() > 600) {
                    int nh = (int) (bmp.getHeight() * (512.0 / bmp.getWidth()));
                    scaled = Bitmap.createScaledBitmap(bmp, 512, nh, true);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
                byte[] imgbt = outputStream.toByteArray();
                StringBuilder sb = new StringBuilder();
                int size = 6;

                for (int offset = 0; offset + size < imgbt.length; offset += size) {
                    String prt = Base64.encodeToString(imgbt, offset, size, Base64.DEFAULT);
                    sb.append(prt);
                }

                String img = sb.toString();

                //OutputStream out = new Base64OutputStream(outputStream, Base64.DEFAULT);

                try {
                    jo.put("user", "tester");
                    jo.put("password", "");
                    jo.put("type", "request");
                    jo.put("key", "analyze");
                    jo.put("image", img);
                    if(tv.getText().equals(""))
                        jo.put("location", "Unkown location");
                    else
                        jo.put("location", tv.getText().toString());

                    jo = ch.request(jo.toString());

                    Log.d("ConnectionHandlerThread", "response=" + jo.toString());
                    if (jo.getString("status").equals("success")) {
                        Snackbar.make(view, "Response received", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        try {
                            handleResponse(new JSONObject(jo.getString("result")), view);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ConnectionHandlerThread", "JSON failure");
                        }
                    } else {
                        Snackbar.make(view, "No response", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }).start();

        Snackbar.make(view, "Image Sent", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void gallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Complete action using"),
                REQUEST_IMAGE_GALLERY);

    }

    public void camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

    }

    public void getPosition(View view) {
        if (!connected) {
            Snackbar.make(view, "Not connected to google services", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        switch (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            case PackageManager.PERMISSION_DENIED:
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
                break;
            default:
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if(mLastLocation == null){
                    Snackbar.make(findViewById(R.id.position_button), "No last known location", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                getGeocode(mLastLocation);
                break;
        }
    }

    public void getGeocode(Location mLastLocation) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses == null)
            return;

         String state = addresses.get(0).getAdminArea();
         String country = addresses.get(0).getCountryName();

        StringBuilder sb = new StringBuilder();
        sb.append(state);
        sb.append(", " + country);

        final String outputtxt = sb.toString();

        final AutoCompleteTextView tv = (AutoCompleteTextView)findViewById(R.id.location_text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(outputtxt);
            }
        });
    }


    public void handleResponse(JSONObject res, View view) throws JSONException {
        switch (res.getString("status")) {
            case "none":
                Snackbar.make(view, "No birds found", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case "fail":
                Snackbar.make(view, "Detecting failed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case "success":
                Snackbar.make(view, "Detecting successful", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("result", res.toString());
                startActivity(intent);

                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            final ImageView iv = (ImageView) this.findViewById(R.id.imageView);
            final Uri uri;
            switch (requestCode) {
                case REQUEST_IMAGE_GALLERY:

                    uri = data.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageURI(uri);
                            iv.setBackgroundResource(0);
                        }
                    });
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    final Bitmap img = (Bitmap) extras.get("data");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(img);
                            iv.setBackgroundResource(0);
                        }
                    });
                    break;
                default:
                    return;
            }

        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        String root = Environment.getExternalStorageDirectory().toString();

        File myDir = new File(root);
        myDir.mkdirs();

        File image = new File(myDir, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void toogle(View view) {

        switch (view.getVisibility()) {
            case View.VISIBLE:
                view.setVisibility(View.INVISIBLE);
                break;
            case View.INVISIBLE:
                view.setVisibility(View.VISIBLE);
                break;
        }
    }

    private boolean connected = false;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("WTF", "WTF");
                        return;
                    }
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if(mLastLocation == null) {
                        Snackbar.make(findViewById(R.id.position_button), "No last known location", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    getGeocode(mLastLocation);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

