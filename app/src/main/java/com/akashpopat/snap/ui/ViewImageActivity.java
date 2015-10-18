package com.akashpopat.snap.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.akashpopat.snap.R;
import com.akashpopat.snap.utils.ParseConstants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ViewImageActivity extends AppCompatActivity {

    protected FloatingActionButton mButton;

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024 * 1024 * 10; // 10 MB

    protected Uri mMediaUri;

    protected String mSenderObjId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        Intent intent = getIntent();
        mSenderObjId = intent.getStringExtra(ParseConstants.KEY_SENDER_ID);

        mButton = (FloatingActionButton) findViewById(R.id.fab_reply);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewImageActivity.this);
                builder.setItems(R.array.camera_choice,mDialogListner);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.pic);

        Uri imageUri = getIntent().getData();

        Picasso.with(this).load(imageUri).into(imageView);

        Picasso.with(this).load(imageUri).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 10 * 1000);
            }

            @Override
            public void onError() {
                Toast.makeText(ViewImageActivity.this, "Oops! Check network", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }


    public DialogInterface.OnClickListener mDialogListner = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i){
                case 0: // Take pic
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if (mMediaUri == null){
                        // Toast
                        Toast.makeText(ViewImageActivity.this, R.string.error_external_storage,Toast.LENGTH_LONG).show();
                    }
                    else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1: // Take video
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if (mMediaUri == null){
                        // Toast
                        Toast.makeText(ViewImageActivity.this, R.string.error_external_storage,Toast.LENGTH_LONG).show();
                    }
                    else {
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0); // 0 = lowest quality
                        startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                    }
                    break;
                case 2: // Choose pic
                    Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pickImageIntent.setType("image/*");
                    startActivityForResult(pickImageIntent,PICK_PHOTO_REQUEST);
                    break;
                case 3: // Choose video
                    Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pickVideoIntent.setType("video/*");
                    Toast.makeText(ViewImageActivity.this, R.string.video_file_size_warning,Toast.LENGTH_LONG).show();
                    startActivityForResult(pickVideoIntent, PICK_VIDEO_REQUEST);
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediaType) {
            if(isExternalStorageAvailable()){
                // return URI
                String appName = ViewImageActivity.this.getString(R.string.app_name);
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);

                if(! mediaStorageDir.exists()){
                    if(! mediaStorageDir.mkdirs()){
                        Log.e("hey", "Directory not created");
                        return null;
                    }
                }

                File mediaFile;
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now);

                String path = mediaStorageDir.getPath() + File.separator;
                if(mediaType == MEDIA_TYPE_IMAGE){
                    mediaFile = new File(path + "IMG_"+timestamp + ".jpg");
                }
                else if(mediaType == MEDIA_TYPE_VIDEO){
                    mediaFile = new File(path + "VID_"+timestamp + ".mp4");
                }
                else
                    return null;

                Log.d("hey","file: "+ Uri.fromFile(mediaFile));
                return Uri.fromFile(mediaFile);

            }
            return null;
        }

        private boolean isExternalStorageAvailable(){
            String state = Environment.getExternalStorageState();

            return state.equals(Environment.MEDIA_MOUNTED);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            // add to gallery

            if(requestCode== PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST){
                if(data == null){
                    Toast.makeText(this, R.string.general_error,Toast.LENGTH_LONG).show();
                }
                else {
                    mMediaUri = data.getData();
                    InputStream inputStream = null;
                    int fileSize = 0;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    } catch (IOException e) {
                        Toast.makeText(this, R.string.error_opening_file,Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }

                    if(fileSize >= FILE_SIZE_LIMIT){
                        Toast.makeText(this, R.string.error_file_size_too_large,Toast.LENGTH_LONG).show();
                    }
                }
            }
            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }
            Intent recipientsIntent = new Intent(this,RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);

            String filetype;
            if(requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST){
                filetype = ParseConstants.TYPE_IMAGE;
            }
            else {
                filetype = ParseConstants.TYPE_VIDEO;
            }
            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE,filetype);
            recipientsIntent.putExtra(ParseConstants.KEY_SENDER_ID,mSenderObjId);
            startActivity(recipientsIntent);
        }
        else if(resultCode != RESULT_CANCELED){
            Toast.makeText(this, R.string.general_error,Toast.LENGTH_LONG).show();
        }

    }
}