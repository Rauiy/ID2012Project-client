package com.example.steve_000.clientapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.common.api.GoogleApiClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_IMAGE_GALLERY = 1;
    private GoogleApiClient mGoogleApiClient;

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
                send(view);
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


        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create an instance of GoogleAPIClient.
        /*if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()){
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

    public void send(final View view){
        final ImageView iv = (ImageView) this.findViewById(R.id.imageView);
        Drawable img = iv.getDrawable();
        if(img == null) {
            Snackbar.make(view, "No image to send", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        final Bitmap bmp = ((BitmapDrawable)img).getBitmap();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectionHandler ch = new ConnectionHandler();
                JSONObject jo = new JSONObject();
                Bitmap scaled = bmp;
                if(bmp.getWidth() > 600){
                    int nh = (int) ( bmp.getHeight() * (512.0 / bmp.getWidth()) );
                    scaled = Bitmap.createScaledBitmap(bmp, 512, nh, true);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
                byte[] imgbt = outputStream.toByteArray();
                StringBuilder sb = new StringBuilder();
                int size = 6;

                for(int offset = 0; offset + size < imgbt.length; offset+= size){
                    String prt = Base64.encodeToString(imgbt, offset, size, Base64.DEFAULT);
                    sb.append(prt);
                }

                String img = sb.toString();

                //OutputStream out = new Base64OutputStream(outputStream, Base64.DEFAULT);

                try {
                    jo.put("user","tester");
                    jo.put("password","");
                    jo.put("type", "request");
                    jo.put("key", "analyze");
                    jo.put("image", img);

                    jo = ch.request(jo.toString());

                    Log.d("ConnectionHandlerThread", "response="+jo.toString());
                    if(jo.getString("status").equals("success")){
                        Snackbar.make(view, "Response received", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        try {
                            handleResponse(new JSONObject(jo.getString("result")), view);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ConnectionHandlerThread", "JSON failure");
                        }
                    }else{
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

    public void camera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

    }

    public void getPosition(View view){

    }

    public void handleResponse(JSONObject res, View view) throws JSONException{
        switch (res.getString("status")){
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
            switch (requestCode){
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
                default: return;
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

    private void toogle(LinearLayout view){

        switch (view.getVisibility()){
            case View.VISIBLE:
                view.setVisibility(View.INVISIBLE);
                break;
            case View.INVISIBLE:
                view.setVisibility(View.VISIBLE);
                break;
        }
    }

}

