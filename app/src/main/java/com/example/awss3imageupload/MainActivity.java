package com.example.awss3imageupload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button button;
    private static final int GalleryPick = 1;
    private ProgressDialog loadingBar;
    private Bitmap photo_bitmap;
    public static final String TAG="MainActivity->";
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.image_select);
        button=findViewById(R.id.upload_btn);

        loadingBar=new ProgressDialog(this);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog =new ProgressDialog(MainActivity.this, R.style.AppCompatAlertDialogStyle);
                progressDialog.setTitle("Uploading Profile Image...");
                progressDialog.setMessage("Please wait while your profle image is being updated");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                String file_name=System.currentTimeMillis()+"";
                String  ACCESS_KEY="ACCESS_KEY",
                        SECRET_KEY= "SECRET_KEY",
                        MY_BUCKET= "eternal-sky",
                        OBJECT_KEY="images/"+file_name+".jpg";

                AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
                AmazonS3 s3 = new AmazonS3Client(credentials);
                java.security.Security.setProperty("networkaddress.cache.ttl" , "60");
                s3.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
                s3.setEndpoint("https://s3-ap-south-1.amazonaws.com/");
                TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
                File f = new File(getFilesDir(),"image.jpg");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                photo_bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , bos);
                byte[] bitmapdata = bos.toByteArray();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                TransferObserver observer = transferUtility.upload(MY_BUCKET,OBJECT_KEY,f, CannedAccessControlList.PublicRead);
                final String finalFile_name = file_name;
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if(state.name().equals("COMPLETED")){
                            String imageUrl =  "https://eternal-sky.s3.ap-south-1.amazonaws.com/images/"+ finalFile_name +".jpg";
                            progressDialog.dismiss();
                            Util.showToast(MainActivity.this,"Profile picture uploaded, check log for image URL.");
                            Log.e("image_url",imageUrl);
                        }

                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        // do something
                        Util.showToast(MainActivity.this,ex.getMessage());
                        progressDialog.dismiss();
                    }

                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            imageUri=data.getData();
            photo_bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(photo_bitmap);
    }
}