package source.open.capturephoto.util;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;

public class CameraHelper extends SurfaceView implements SurfaceHolder.Callback {

    //Class API camera for smartphone camera control
    public Camera camera;
    public final FrameLayout cameraPreview;

    private Context context;
    //Interface for control size and format Surface
    private SurfaceHolder surfaceHolder;
    private static final String TAG = CameraHelper.class.getCanonicalName();

    /**
     * @param context
     * @param cameraPreview init class camera and add at Surface
     */
    public CameraHelper(Context context, FrameLayout cameraPreview) {
        super(context);
        this.context = context;
        this.cameraPreview = cameraPreview;
        // Verify if the camera is up and return instance of them
        this.camera = getCameraInstance();
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
        }  catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e(TAG, e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Do nothing
    }
}