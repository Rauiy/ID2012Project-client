package com.example.steve_000.clientapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {
    private boolean saved;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String image = null;
        String text = null;
        try {
            JSONObject res = new JSONObject(getIntent().getStringExtra("result"));
            image = res.getString("image");
            text = res.getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        byte[] imgbt = Base64.decode(image,Base64.DEFAULT);
        final Bitmap bmp = BitmapFactory.decodeByteArray(imgbt,0,imgbt.length);
        final ImageView iv = (ImageView) findViewById(R.id.result_image_view);
        final TextView ev = (TextView) findViewById(R.id.result_text_view);
        final CharSequence cs = text.subSequence(0,text.length());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv.setImageBitmap(bmp);
                ev.setMovementMethod(new ScrollingMovementMethod());
                ev.setText(cs);
            }
        });

        saved = false;
        FloatingActionButton save = (FloatingActionButton) findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saved) {
                    Snackbar.make(v, "Image is already saved", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    Snackbar.make(v, "Trying to save image", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    save_image(v);
                }
            }
        });
    }

    public void save_image(View view){
        switch (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            case PackageManager.PERMISSION_DENIED:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                break;
            default :
                writeToFile(view);
                break;

        }
    }

    public void writeToFile(View view){
        ImageView iv = (ImageView) findViewById(R.id.result_image_view);
        Drawable img = iv.getDrawable();
        if(img == null) {
            Snackbar.make(view, "No image to save", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        Bitmap bmp = ((BitmapDrawable)img).getBitmap();
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root);
        if(!dir.exists())
            dir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(root,timeStamp + ".jpg");

        if(file.exists()){
            Snackbar.make(view, "Image name is already in use", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            saved = true;
            Snackbar.make(view, "Image saved in " + root, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            Intent scanFileIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
            sendBroadcast(scanFileIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(view, "Permission denied " + root, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    writeToFile(findViewById(R.id.save_button));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
