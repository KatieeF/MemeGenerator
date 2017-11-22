package com.codeschool.memegenerator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int PHOTO_INTENT_REQUEST_CODE = 1;
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //button variable using the 'share' button
        Button shareButton = (Button)findViewById(R.id.share_button_view);
        //Set an on click listener to the button
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What should happen when the button is clicked
                //call method share photo
                sharePhoto();
            }
        });
    }

    private void sharePhoto() {
        createCompositeImage();
        createShareIntent();
    }

    private void createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File sharedFile = new File(getCacheDir(), "images/image.png");
        Uri imageUri = FileProvider.getUriForFile(this, "com.codeschool.memegenerator.fileprovider", sharedFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/png");
        startActivity(Intent.createChooser(shareIntent,getString(R.string.share_meme)));
    }

    private void createCompositeImage() {
        //get access to frame layout and store it in variable
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        //set the drawing cash enabled to true
        frameLayout.setDrawingCacheEnabled(true);
        //get the drawing cash and store it in a bitmap variable
        Bitmap bitmap = frameLayout.getDrawingCache();

        File sharedFile = new File(getCacheDir(), "images");
        sharedFile.mkdirs();
        //incase the file cannot be found use try catch statement
        try {
            FileOutputStream stream = new FileOutputStream(sharedFile + "/image.png");
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
            //ensure stream has been closed
            stream.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        //set the drawing cash to false now we're done with it
        frameLayout.setDrawingCacheEnabled(false);
        //destroy the cache
        frameLayout.destroyDrawingCache();
    }

    public void pickPhotoFromGallery(View view){
        requestPermission();

    }

    private void createPhotoIntent() {
    /* create an intent that will allow us to choose from the gallery
       using the action_pick*/
        Intent photoIntent = new Intent(Intent.ACTION_PICK);

        /*File allows the directory/path to be gotten
        In this case it's the gallery on a device
        Using 'Environment' gives access to the variables
        needed for top level file access*/
        File photoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //Parsing the photoDirectory path provides a string
        Uri photoUri = Uri.parse(photoDirectory.getPath());

        /*passes uri and a string type
        type will be an image with any file extension
        uri will be gallery path as gotten above */
        photoIntent.setDataAndType( photoUri, "image/*");

        /*Requires an intent and a code for the intent
        so in this instance we have set it to
         '1' then refactored > extracted > constant*/
        startActivityForResult(photoIntent, PHOTO_INTENT_REQUEST_CODE);
    }

    private void requestPermission(){
           // Here, thisActivity is the current activity
           if (ContextCompat.checkSelfPermission(this,
                   Manifest.permission.READ_EXTERNAL_STORAGE)
                   != PackageManager.PERMISSION_GRANTED) {
                   // No explanation needed, we can request the permission.
                   ActivityCompat.requestPermissions(this,
                           new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                           EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);

                   // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                   // app-defined int constant. The callback method gets the
                   // result of the request.
           } else {
               createPhotoIntent();
           }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                   /* permission was granted, yay! Call the function that
                    gives us access to the gallery*/
                    createPhotoIntent();

                } else {

                    // permission denied, boo! 
                    Toast.makeText(this, R.string.gallery_permission_denied, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if both are true get the image returned
        if(resultCode == RESULT_OK) {
            if (requestCode == PHOTO_INTENT_REQUEST_CODE);
            Uri photoUri = data.getData();
            ImageView imageView = (ImageView)findViewById(R.id.meme_image_view);
            Picasso.with(this).load(photoUri).into(imageView);
        }
    }
}

