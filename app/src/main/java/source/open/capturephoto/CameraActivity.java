package source.open.capturephoto;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import source.open.capturephoto.util.CameraHelper;
import source.open.capturephoto.util.DialogView;
import source.open.capturephoto.util.LocationHelper;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG                                     =   CameraActivity.class.getCanonicalName();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT_SLASH      =   new SimpleDateFormat("dd_MM_yyyy");
    private static final String CAPTURED_IMAGE_IS_EMPTY                 =   "Captured image is empty";
    // util for manage location
    private LocationHelper location;
    // util for manage camera
    private CameraHelper cameraHelper;
    // variable for use tag view
    private LinearLayout linearLayout;
    private ImageButton btnCapCamera;
    private byte[] image;
    private FrameLayout cameraPreview;
    // file for save image
    private File file;
    private FileOutputStream fileoutputstream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //btnCamera = (ImageButton) findViewById(R.id.button_camera);
        btnCapCamera = (ImageButton) findViewById(R.id.button_cap_camera);
        // Hide buttons LinearLayout
        linearLayout = (LinearLayout) findViewById(R.id.btn_accetp_and_dismiss);
        linearLayout.setVisibility(LinearLayout.INVISIBLE);

        // hide toolbar
        getSupportActionBar().hide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (location != null && location.mGoogleApiClient != null) {
            location.mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (location != null && location.mGoogleApiClient.isConnected()) {
            location.mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(location == null && cameraHelper == null) {
            validatePermission();
        }else {
            // Resuming the periodic location updates
            if (location != null && location.mGoogleApiClient.isConnected()) {
                location.startLocationUpdates();
            }

            resetPhoto();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseCameraAndPreview();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean result = true;
        for (int i = 0; i < grantResults.length; i++) {
            if(grantResults[i] != 0){
                result = false;
                validatePermission();
                break;
            }
        }
        if (result) {
            setupCamera();
        }
    }

    public void backDetail(View view) {
        this.finish();
    }

    private void validatePermission() {
        // validate if permission camera and localization is enable
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location.showMessageGPSNoEnabled(this, getString(R.string.text_alert_gps_enable));
        } else {
            // Assume thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialogPermissions();
                } else {
                    showEnablePermission();
                }
            } else {
                setupCamera();
            }
        }
    }

    public void showDialogPermissions() {
        DialogView.showMessage(this, getString(R.string.title_toast), getString(R.string.enable_permissions_message_alert));
        DialogView.acceptMessage(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnablePermission();
                DialogView.dismiss();
            }
        });
    }

    private void showEnablePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0);
    }

    private void setupCamera() {
        localizationInit();
        cameraInit();
    }

    private void localizationInit() {
        location = new LocationHelper(this);

        if (location.mGoogleApiClient != null) {
            location.mGoogleApiClient.connect();
        }
    }

    private void cameraInit() {
        cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);

        cameraHelper = new CameraHelper(this, cameraPreview);
        cameraPreview.addView(cameraHelper);

        btnCapCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take photo
                cameraHelper.camera.takePicture(null, null, getPictureCallback());
            }
        });
    }

    public void takePhotoCamera(View view) {

        // create new file with name date + latitude + longitude
        file = new File( Environment.getExternalStorageDirectory() +
                "/" +
                "date_" + SIMPLE_DATE_FORMAT_SLASH.format(new Date()) +
                "longitude_" + location.longitude +
                "latitude_" + location.latitude +
                ".png");

        try {
            file.createNewFile();
            fileoutputstream = new FileOutputStream(file);
            fileoutputstream.write(image);
            fileoutputstream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        // show button for take photo
        linearLayout.setVisibility(LinearLayout.INVISIBLE);
        btnCapCamera.setVisibility(Button.VISIBLE);

        // show message photo taken
        Toast.makeText(this, getString(R.string.photo_taken), Toast.LENGTH_LONG).show();

        resetPhoto();
    }

    public void backTakePhotoCamera(View view) {
        resetPhoto();
    }

    private void resetPhoto() {
        linearLayout.setVisibility(LinearLayout.INVISIBLE);
        btnCapCamera.setVisibility(Button.VISIBLE);
        if(image != null) {
            cameraPreview.removeAllViews();
            cameraPreview.addView(cameraHelper);
        }
        image = null;
    }

    private void releaseCameraAndPreview() {
        cameraPreview.removeAllViews();
        if (cameraHelper.camera != null) {
            cameraHelper.camera.release();
            cameraHelper.camera = null;
        }
    }

    public Camera.PictureCallback getPictureCallback() {
        // Take photo and set in FrameLayout, It is FrameLayout visor
        return  new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                // save image token
                image = data;

                // Create bitMap with the image tooken
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (bitmap == null) {
                    Log.e(TAG, CAPTURED_IMAGE_IS_EMPTY);
                    return;
                }
                // show button for choose photo
                linearLayout.setVisibility(LinearLayout.VISIBLE);
                btnCapCamera.setVisibility(Button.INVISIBLE);

                // init location
                if(location == null) {
                    localizationInit();
                }

                // set image in view
                cameraHelper.cameraPreview.setBackground(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
            }
        };
    }
}
