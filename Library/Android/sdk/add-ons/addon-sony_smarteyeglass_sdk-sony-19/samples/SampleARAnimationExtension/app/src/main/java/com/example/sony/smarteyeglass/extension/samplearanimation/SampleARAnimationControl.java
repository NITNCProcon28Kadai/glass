package com.example.sony.smarteyeglass.extension.samplearanimation;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.sony.smarteyeglass.SmartEyeglassControl;
import com.sony.smarteyeglass.SmartEyeglassControl.Intents;
import com.sony.smarteyeglass.extension.sampleARAnimation.R;
import com.sony.smarteyeglass.extension.util.SmartEyeglassControlUtils;
import com.sony.smarteyeglass.extension.util.SmartEyeglassEventListener;
import com.sony.smarteyeglass.extension.util.ar.CylindricalRenderObject;
import com.sony.smarteyeglass.extension.util.ar.RenderObject;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

public final class SampleARAnimationControl extends ControlExtension {

    private final Context context;
    private final SmartEyeglassControlUtils utils;
    private static final int SMARTEYEGLASS_API_VERSION = 2;
    private static final int OBJECT_ID = 1;
    private CylindricalRenderObject renderObj;
    private static final int ANIMATION_INTERVAL_TIME = 66;
    private SparseArray<Bitmap> imageMap = new SparseArray<Bitmap>();
    private boolean isAnimation = false;
    private int frameCount = 0;
    private Timer timer;
    private float latitude,longtitude,glatitude,glongtitude;

    private final SmartEyeglassEventListener listener =
            new SmartEyeglassEventListener() {
                @Override
                public void onARRegistrationResult(
                        final int result, final int objectId) {
                    if (result != SmartEyeglassControl.Intents.AR_RESULT_OK) {
                        return;
                    }
                }

                @Override
                public void onARObjectRequest(final int objectId) {
                    utils.sendARObjectResponse(renderObj, 0);
                }
            };

    /**
     * Creates an instance of this control class.
     *
     * @param context            The context.
     * @param hostAppPackageName Package name of host application.
     */
    public SampleARAnimationControl(
            final Context context, final String hostAppPackageName) {
        super(context, hostAppPackageName);
        this.context = context;
        utils = new SmartEyeglassControlUtils(hostAppPackageName, listener);
        utils.setRequiredApiVersion(SMARTEYEGLASS_API_VERSION);
        utils.activate(context);
        setAnimationResource();
    }

    @Override
    public void onDestroy() {
        utils.deactivate();
    }

    private void renderStart() {
        utils.setRenderMode(Intents.MODE_AR);
        loadResource();
        startARAnimation();
    }

    private void loadResource() {
        renderObj = new CylindricalRenderObject(OBJECT_ID,
                getBitmapResource(R.drawable.running1), 0,
                SmartEyeglassControl.Intents.AR_OBJECT_TYPE_ANIMATED_IMAGE,
                0, 0.0f);

        SmartEyeglassControlUtils.PointInWorldCoordinate myLocation =
                createCoodinate(latitude,longtitude);
        SmartEyeglassControlUtils.PointInWorldCoordinate ghostLocation=
                createCoodinate(glatitude,glongtitude);
        PointF point = SmartEyeglassControlUtils.convertCoordinateSystemFromWorldToCylindrical(
                ghostLocation,myLocation);
        renderObj.setPosition(point);
        utils.moveARObject(renderObj);
        registerObject(renderObj);
    }

    private void setAnimationResource() {
        int[] id = RunningAnimationResources.ID_RESOURCE_LIST;
        for (int frame = 0; frame < RunningAnimationResources.MAX_FRAME; frame++) {
            imageMap.put(frame, getBitmapResource(id[frame]));
        }
    }

    /**
     * Register a render object with the AR engine.
     *
     * @param obj The render object.
     */
    private void registerObject(final RenderObject obj) {
        this.utils.registerARObject(obj);
    }

    /**
     * Retrieve a registered bitmap by its resource ID.
     *
     * @param id The resource ID.
     * @return The bitmap.
     */
    private Bitmap getBitmapResource(final int id) {
        Bitmap b = BitmapFactory.decodeResource(context.getResources(), id);
        b.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return b;
    }

    @Override
    public void onResume() {
        renderStart();
    }

    @Override
    public void onPause() {
        stopARAnimation();
    }

    @Override
    public void onTouch(final ControlTouchEvent ev) {
        super.onTouch(ev);
    }

    @Override
    public void onStop() {
        super.onStop();
        imageMap.clear();
    }

    private void updateAnimationimage() {
        if (!isAnimation) {
            return;
        }

        Bitmap bitmap = imageMap.get(frameCount);
        if (bitmap != null) {
            utils.sendARAnimationObject(OBJECT_ID, bitmap);
            frameCount++;
        }
        if (frameCount >= RunningAnimationResources.MAX_FRAME) {
            frameCount = 0;
        }
    }

    private void startARAnimation() {
        utils.enableARAnimationRequest();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            private Handler mHandler = new Handler();

            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        updateAnimationimage();
                    }
                });
            }
        }, 0, ANIMATION_INTERVAL_TIME);

        isAnimation = true;
    }

    private void stopARAnimation() {
        // stop timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        utils.disableARAnimationRequest();
        isAnimation = true;
    }

    private SmartEyeglassControlUtils.PointInWorldCoordinate createCoodinate(
            final float latitude,final float longtitude){
        SmartEyeglassControlUtils.PointInWorldCoordinate c = utils.new PointInWorldCoordinate();
        c.latitude = latitude;
        c.longitude = longtitude;
        c.altitude = 0;
        return c;
    }

    public void getCurrentLocation(float Latitude,float Longitude,float ghostLatitude,float ghostLongtitude){
        latitude = Latitude;
        longtitude = Longitude;
        glatitude = ghostLatitude;
        glongtitude = ghostLongtitude;
    }
}