package jcg.mini_dji_go_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


import com.scandit.datacapture.barcode.capture.BarcodeCapture;
import com.scandit.datacapture.barcode.capture.BarcodeCaptureListener;
import com.scandit.datacapture.barcode.capture.BarcodeCaptureSession;
import com.scandit.datacapture.barcode.capture.BarcodeCaptureSettings;
import com.scandit.datacapture.barcode.data.Barcode;
import com.scandit.datacapture.barcode.data.Symbology;
import com.scandit.datacapture.barcode.data.SymbologyDescription;
import com.scandit.datacapture.core.capture.DataCaptureContext;
import com.scandit.datacapture.core.data.FrameData;
import com.scandit.datacapture.core.source.CameraSettings;
import com.scandit.datacapture.core.source.FrameSourceState;
import com.scandit.datacapture.core.ui.DataCaptureView;

import org.jetbrains.annotations.NotNull;

import dji.common.camera.SettingsDefinitions.FocusMode;
import dji.sdk.accessory.spotlight.Spotlight;
import jcg.mini_dji_go_app.media.DJIVideoStreamDecoder;
import jcg.mini_dji_go_app.media.NativeHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;

import dji.common.camera.SettingsDefinitions;
import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.thirdparty.afinal.core.AsyncTask;
import jcg.mini_dji_go_app.model.Analyzer;

import static jcg.mini_dji_go_app.BluetoothConstants.*;

public class MainActivity extends Activity implements DJICodecManager.YuvDataCallback, BarcodeCaptureListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MSG_WHAT_SHOW_TOAST = 0;
    private static final int MSG_WHAT_UPDATE_TITLE = 1;
    private SurfaceHolder.Callback surfaceCallback;

    private enum DemoType { USE_TEXTURE_VIEW, USE_SURFACE_VIEW, USE_SURFACE_VIEW_DEMO_DECODER}
    private static DemoType demoType = DemoType.USE_TEXTURE_VIEW;
    private VideoFeeder.VideoFeed standardVideoFeeder;
    public static final String SCANDIT_LICENSE_KEY = "AV2g3zzMHDewJ3BYQgopAUoYipFtFY1Ms0VD10Am53f7SFRQ9U3U+YFUKxt+ajB4y2jcix1D/UYWFif8Bk9lhf4S8UlOXSksajpl/Yx/qRUfd1UXbV7k2ahhnRs8bzZk8HRHhkx/DdgEY+mgallDoJERhWlmVDNCNmhOtRp1iQtMet9M533kpwxgDoXlR/f8xFiz/vBG/9suV+8xOll2vGRWWWXRe9WqcWw2ltdYRd+6J2Y1xl2C/A9z/59PS/pQ4C0+wyBiVXSIQVRnDn3ce1NvtNzTa4xCx304FpNv1OGpQ2Mh90EKCdR3qp09e/2VISzz7oReBjkcQtAbf3XCK89y2TSMe2fOwnpKA8ZjYSzgfgAwBXw7y+d/Nru9dZbjWUfj2/9F8zT4fVUFhVrBcRdyEJq5ZVDoWyf9Mrl1+rrAdUWgeVpDrANZ54ZYVdXT+mzc5BhcQ0rgCvlsCXNvR8RoCPVsR7E/ymdOugh/UgzCWa8yQncWHl9MTxi5cUx5LyD/YgQWtL8wG3E7pzSnZ3PEzgFrlMGRLkIZvHcdXNGTx9bbqS6RauhWAk4iKqDm4xbkCb/Jzyv8kGlTp2A8ONsXQGKAcD8Giniz9ea2vEuztOkvI5aUX9Cea10o+j6B0l766hNtE8lNfc+0jsm8uO4S+3/uaXW4h8swgbJjptbIYoy7bUQHgIFB2weAuL5E1O83ZNCPbN6jZ67fZnFDdtegQWG3BJ6e6ybZDJCw2ehdHNzqjZNE3rjGMBDAX0SwlW+KuT0IleJmGlI2MCe6TFIp79qqBs9xflv/5rwJIzVbnET+YvOo07EIMJRCeH3rGYXOmZwUdQqgTyP58N/oOB5wgNlYMKMkdAiKsjeqkhd+y01vt8gQbUonAVwpAdhpM7sPXwGo0ZoE1f2aKZzGIBZ+KXgCjh1d3SwbtHsO6Z+i9hqTwDW8jZOyy1iHsUwmGnS0J0Q4esxioLdtOEK+3+8bkyxPMRIY2JV1NwoT9gegMnC7R2DvKXXeyP4jlS2dXdfzlMiTkAOthaaKSe4ImhzKMCUQxi6UHa1WNmo1bB3aaSFcfjlJkTQWsMxqgxY7dF3YHwRg0A5MaDOtctculMbUZiaTkVZGNQc1w9hEMJPR1qVo3TqAnUeoiAuzELhU08zM/1tgrZBJmr6rESO5jCT+n3XhLYz5Gic5aBKjhlnmKQPv2mDcuoCZhl5ol3XG";


    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    private TextView titleTv;
    public Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SHOW_TOAST:
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_WHAT_UPDATE_TITLE:
                    if (titleTv != null) {
                        titleTv.setText((String) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private TextureView videostreamPreviewTtView;
    private SurfaceView videostreamPreviewSf;
    private SurfaceHolder videostreamPreviewSh;
    private Camera mCamera;
    private Spotlight mSpotlight;
    private DJICodecManager mCodecManager;
    private TextView savePath;
    private StringBuilder stringBuilder;
    private int videoViewWidth;
    private int videoViewHeight;
    private int count;
    private Context context = this;
    private Bitmap frame;
    private String code;

    private DataCaptureContext dataCaptureContext;
    private BarcodeCapture barcodeCapture;
    private com.scandit.datacapture.core.source.Camera camera;
    private DataCaptureView dataCaptureView;

    @Override
    protected void onResume() {
        super.onResume();
        initSurfaceOrTextureView();
        notifyStatusChange();
    }

    private void initSurfaceOrTextureView(){
        switch (demoType) {
            case USE_SURFACE_VIEW:
                initPreviewerSurfaceView();
                break;
            case USE_SURFACE_VIEW_DEMO_DECODER:
                /**
                 * we also need init the textureView because the pre-transcoded video steam will display in the textureView
                 */
                initPreviewerTextureView();

                /**
                 * we use standardVideoFeeder to pass the transcoded video data to DJIVideoStreamDecoder, and then display it
                 * on surfaceView
                 */
                initPreviewerSurfaceView();
                break;
            case USE_TEXTURE_VIEW:
                initPreviewerTextureView();
                break;
        }
    }

    @Override
    protected void onPause() {
        if (mCamera != null) {
            if (VideoFeeder.getInstance().getPrimaryVideoFeed() != null) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().removeVideoDataListener(mReceivedVideoDataListener);
            }
            if (standardVideoFeeder != null) {
                standardVideoFeeder.removeVideoDataListener(mReceivedVideoDataListener);
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager.destroyCodec();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initUi();
        //setBluetooth();

        Analyzer analyzer = new Analyzer(context);

        //Scann
        Button buttonScann = findViewById(R.id.buttonScann);
        buttonScann.setOnClickListener(view -> {

            barcodeScann();

        });

        //Flash
        Button buttonFlash = findViewById(R.id.buttonFlash);
        buttonFlash.setOnClickListener(view -> {

            mSpotlight.setEnabled(true, djiError -> {
                if (djiError != null) {
                    showToast("can't active flash:"+djiError.getDescription());
                }
            });
        });


        //Foco
        Button buttonFoco = findViewById(R.id.buttonFoco);
        buttonFoco.setOnClickListener(view -> {

            mCamera.setFocusMode(FocusMode.AFC, djiError -> {
                if (djiError != null) {
                    showToast("can't change focus mode of camera, error:"+djiError.getDescription());
                }
            });

        });


    }

    private void showToast(String s) {
        mainHandler.sendMessage(
                mainHandler.obtainMessage(MSG_WHAT_SHOW_TOAST, s)
        );
    }

    private void updateTitle(String s) {
        mainHandler.sendMessage(
                mainHandler.obtainMessage(MSG_WHAT_UPDATE_TITLE, s)
        );
    }

    private void initUi() {
        savePath = findViewById(R.id.activity_main_save_path);

        videostreamPreviewTtView = findViewById(R.id.livestream_preview_ttv);
        videostreamPreviewSf = findViewById(R.id.livestream_preview_sf);
        videostreamPreviewSf.setClickable(true);
        videostreamPreviewSf.setOnClickListener(v -> {
            float rate = VideoFeeder.getInstance().getTranscodingDataRate();
            showToast("current rate:" + rate + "Mbps");
            if (rate < 10) {
                VideoFeeder.getInstance().setTranscodingDataRate(10.0f);
                showToast("set rate to 10Mbps");
            } else {
                VideoFeeder.getInstance().setTranscodingDataRate(3.0f);
                showToast("set rate to 3Mbps");
            }
        });
        updateUIVisibility();
    }

    private void updateUIVisibility(){
        switch (demoType) {
            case USE_SURFACE_VIEW:
                videostreamPreviewSf.setVisibility(View.VISIBLE);
                videostreamPreviewTtView.setVisibility(View.GONE);
                break;
            case USE_SURFACE_VIEW_DEMO_DECODER:
                /**
                 * we need display two video stream at the same time, so we need let them to be visible.
                 */
                videostreamPreviewSf.setVisibility(View.VISIBLE);
                videostreamPreviewTtView.setVisibility(View.VISIBLE);
                break;

            case USE_TEXTURE_VIEW:
                videostreamPreviewSf.setVisibility(View.GONE);
                videostreamPreviewTtView.setVisibility(View.VISIBLE);
                break;
        }
    }
    private long lastupdate;
    private void notifyStatusChange() {

        final BaseProduct product = VideoDecodingApplication.getProductInstance();

        Log.d(TAG, "notifyStatusChange: " + (product == null ? "Disconnect" : (product.getModel() == null ? "null model" : product.getModel().name())));
        if (product != null && product.isConnected() && product.getModel() != null) {
            updateTitle(product.getModel().name() + " Connected " + demoType.name());
        } else {
            updateTitle("Disconnected");
        }

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (System.currentTimeMillis() - lastupdate > 1000) {
                    Log.d(TAG, "camera recv video data size: " + size);
                    lastupdate = System.currentTimeMillis();
                }
                switch (demoType) {
                    case USE_SURFACE_VIEW:
                        if (mCodecManager != null) {
                            mCodecManager.sendDataToDecoder(videoBuffer, size);
                        }
                        break;
                    case USE_SURFACE_VIEW_DEMO_DECODER:
                        /**
                         we use standardVideoFeeder to pass the transcoded video data to DJIVideoStreamDecoder, and then display it
                         * on surfaceView
                         */
                        DJIVideoStreamDecoder.getInstance().parse(videoBuffer, size);
                        break;

                    case USE_TEXTURE_VIEW:
                        if (mCodecManager != null) {
                            mCodecManager.sendDataToDecoder(videoBuffer, size);
                        }
                        break;
                }

            }
        };

        if (null == product || !product.isConnected()) {
            mCamera = null;
            showToast("Disconnected");
        } else {
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                mCamera = product.getCamera();
                mCamera.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, djiError -> {
                    if (djiError != null) {
                        showToast("can't change mode of camera, error:"+djiError.getDescription());
                    }
                });

                //When calibration is needed or the fetch key frame is required by SDK, should use the provideTranscodedVideoFeed
                //to receive the transcoded video feed from main camera.
                if (demoType == DemoType.USE_SURFACE_VIEW_DEMO_DECODER && isTranscodedVideoFeedNeeded()) {
                    standardVideoFeeder = VideoFeeder.getInstance().provideTranscodedVideoFeed();
                    standardVideoFeeder.addVideoDataListener(mReceivedVideoDataListener);
                    return;
                }
                if (VideoFeeder.getInstance().getPrimaryVideoFeed() != null) {
                    VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
                }

            }
        }
    }

    /**
     * Init a fake texture view to for the codec manager, so that the video raw data can be received
     * by the camera
     */
    private void initPreviewerTextureView() {
        videostreamPreviewTtView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "real onSurfaceTextureAvailable");
                videoViewWidth = width;
                videoViewHeight = height;
                Log.d(TAG, "real onSurfaceTextureAvailable: width " + videoViewWidth + " height " + videoViewHeight);
                if (mCodecManager == null) {
                    mCodecManager = new DJICodecManager(getApplicationContext(), surface, width, height);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                videoViewWidth = width;
                videoViewHeight = height;
                Log.d(TAG, "real onSurfaceTextureAvailable2: width " + videoViewWidth + " height " + videoViewHeight);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mCodecManager != null) {
                    mCodecManager.cleanSurface();
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    /**
     * Init a surface view for the DJIVideoStreamDecoder
     */
    private void initPreviewerSurfaceView() {
        videostreamPreviewSh = videostreamPreviewSf.getHolder();
        surfaceCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "real onSurfaceTextureAvailable");
                videoViewWidth = videostreamPreviewSf.getWidth();
                videoViewHeight = videostreamPreviewSf.getHeight();
                Log.d(TAG, "real onSurfaceTextureAvailable3: width " + videoViewWidth + " height " + videoViewHeight);
                switch (demoType) {
                    case USE_SURFACE_VIEW:
                        if (mCodecManager == null) {
                            mCodecManager = new DJICodecManager(getApplicationContext(), holder, videoViewWidth,
                                                                videoViewHeight);
                        }
                        break;
                    case USE_SURFACE_VIEW_DEMO_DECODER:
                        // This demo might not work well on P3C and OSMO.
                        NativeHelper.getInstance().init();
                        DJIVideoStreamDecoder.getInstance().init(getApplicationContext(), holder.getSurface());
                        DJIVideoStreamDecoder.getInstance().resume();
                        break;
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                videoViewWidth = width;
                videoViewHeight = height;
                Log.d(TAG, "real onSurfaceTextureAvailable4: width " + videoViewWidth + " height " + videoViewHeight);
                switch (demoType) {
                    case USE_SURFACE_VIEW:
                        //mCodecManager.onSurfaceSizeChanged(videoViewWidth, videoViewHeight, 0);
                        break;
                    case USE_SURFACE_VIEW_DEMO_DECODER:
                        DJIVideoStreamDecoder.getInstance().changeSurface(holder.getSurface());
                        break;
                }

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                switch (demoType) {
                    case USE_SURFACE_VIEW:
                        if (mCodecManager != null) {
                            mCodecManager.cleanSurface();
                            mCodecManager.destroyCodec();
                            mCodecManager = null;
                        }
                        break;
                    case USE_SURFACE_VIEW_DEMO_DECODER:
                        DJIVideoStreamDecoder.getInstance().stop();
                        NativeHelper.getInstance().release();
                        break;
                }

            }
        };

        videostreamPreviewSh.addCallback(surfaceCallback);
    }


    @Override
    public void onYuvDataReceived(MediaFormat format, final ByteBuffer yuvFrame, int dataSize, final int width, final int height) {
        //In this demo, we test the YUV data by saving it into JPG files.
        //DJILog.d(TAG, "onYuvDataReceived " + dataSize);
        if (count++ % 30 == 0 && yuvFrame != null) {
            final byte[] bytes = new byte[dataSize];
            yuvFrame.get(bytes);
            //DJILog.d(TAG, "onYuvDataReceived2 " + dataSize);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // two samples here, it may has other color format.
                    int colorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
                    switch (colorFormat) {
                        case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                            //NV12
                            if (Build.VERSION.SDK_INT <= 23) {
                                oldSaveYuvDataToJPEG(bytes, width, height);
                            } else {
                                newSaveYuvDataToJPEG(bytes, width, height);
                            }
                            break;
                        case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                            //YUV420P
                            newSaveYuvDataToJPEG420P(bytes, width, height);
                            break;
                        default:
                            break;
                    }

                }
            });
        }
    }

    // For android API <= 23
    private void oldSaveYuvDataToJPEG(byte[] yuvFrame, int width, int height){
        if (yuvFrame.length < width * height) {
            //DJILog.d(TAG, "yuvFrame size is too small " + yuvFrame.length);
            return;
        }

        byte[] y = new byte[width * height];
        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];
        byte[] nu = new byte[width * height / 4]; //
        byte[] nv = new byte[width * height / 4];

        System.arraycopy(yuvFrame, 0, y, 0, y.length);
        for (int i = 0; i < u.length; i++) {
            v[i] = yuvFrame[y.length + 2 * i];
            u[i] = yuvFrame[y.length + 2 * i + 1];
        }
        int uvWidth = width / 2;
        int uvHeight = height / 2;
        for (int j = 0; j < uvWidth / 2; j++) {
            for (int i = 0; i < uvHeight / 2; i++) {
                byte uSample1 = u[i * uvWidth + j];
                byte uSample2 = u[i * uvWidth + j + uvWidth / 2];
                byte vSample1 = v[(i + uvHeight / 2) * uvWidth + j];
                byte vSample2 = v[(i + uvHeight / 2) * uvWidth + j + uvWidth / 2];
                nu[2 * (i * uvWidth + j)] = uSample1;
                nu[2 * (i * uvWidth + j) + 1] = uSample1;
                nu[2 * (i * uvWidth + j) + uvWidth] = uSample2;
                nu[2 * (i * uvWidth + j) + 1 + uvWidth] = uSample2;
                nv[2 * (i * uvWidth + j)] = vSample1;
                nv[2 * (i * uvWidth + j) + 1] = vSample1;
                nv[2 * (i * uvWidth + j) + uvWidth] = vSample2;
                nv[2 * (i * uvWidth + j) + 1 + uvWidth] = vSample2;
            }
        }
        //nv21test
        byte[] bytes = new byte[yuvFrame.length];
        System.arraycopy(y, 0, bytes, 0, y.length);
        for (int i = 0; i < u.length; i++) {
            bytes[y.length + (i * 2)] = nv[i];
            bytes[y.length + (i * 2) + 1] = nu[i];
        }
        Log.d(TAG,
              "onYuvDataReceived: frame index: "
                  + DJIVideoStreamDecoder.getInstance().frameIndex
                  + ",array length: "
                  + bytes.length);
        screenShot(bytes, Environment.getExternalStorageDirectory() + "/DJI_ScreenShot", width, height);
    }

    private void newSaveYuvDataToJPEG(byte[] yuvFrame, int width, int height){
        if (yuvFrame.length < width * height) {
            //DJILog.d(TAG, "yuvFrame size is too small " + yuvFrame.length);
            return;
        }
        int length = width * height;

        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];
        for (int i = 0; i < u.length; i++) {
            v[i] = yuvFrame[length + 2 * i];
            u[i] = yuvFrame[length + 2 * i + 1];
        }
        for (int i = 0; i < u.length; i++) {
            yuvFrame[length + 2 * i] = u[i];
            yuvFrame[length + 2 * i + 1] = v[i];
        }
        screenShot(yuvFrame,Environment.getExternalStorageDirectory() + "/DJI_ScreenShot", width, height);
    }

    private void newSaveYuvDataToJPEG420P(byte[] yuvFrame, int width, int height) {
        if (yuvFrame.length < width * height) {
            return;
        }
        int length = width * height;

        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];

        for (int i = 0; i < u.length; i ++) {
            u[i] = yuvFrame[length + i];
            v[i] = yuvFrame[length + u.length + i];
        }
        for (int i = 0; i < u.length; i++) {
            yuvFrame[length + 2 * i] = v[i];
            yuvFrame[length + 2 * i + 1] = u[i];
        }
        screenShot(yuvFrame, Environment.getExternalStorageDirectory() + "/DJI_ScreenShot", width, height);
    }

    /**
     * Save the buffered data into a JPG image file
     */

    private void screenShot(byte[] buf, String shotDir, int width, int height) {
        File dir = new File(shotDir);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        YuvImage yuvImage = new YuvImage(buf,
                ImageFormat.NV21,
                width,
                height,
                null);
        OutputStream outputFile;
        final String path = dir + "/ScreenShot_" + System.currentTimeMillis() + ".jpg";
        try {
            outputFile = new FileOutputStream(new File(path));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "test screenShot: new bitmap output file error: " + e);
            return;
        }
        if (outputFile != null) {
            yuvImage.compressToJpeg(new Rect(0,
                    0,
                    width,
                    height), 100, outputFile);
        }
        try {
            outputFile.close();
        } catch (IOException e) {
            Log.e(TAG, "test screenShot: compress yuv image error: " + e);
            e.printStackTrace();
        }
        runOnUiThread(() -> displayPath(path));
    }

    private void displayPath(String path) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }

        path = path + "\n";
        stringBuilder.append(path);
        savePath.setText(stringBuilder.toString());
    }

    private boolean isTranscodedVideoFeedNeeded() {
        if (VideoFeeder.getInstance() == null) {
            return false;
        }

        return VideoFeeder.getInstance().isFetchKeyFrameNeeded() || VideoFeeder.getInstance()
                                                                               .isLensDistortionCalibrationNeeded();
    }


    //----------------------------------- Scann Methods ----------------------------------//


    public void barcodeScann(){

        DataCaptureContext dataCaptureContext = DataCaptureContext.forLicenseKey(SCANDIT_LICENSE_KEY);

        CameraSettings cameraSettings = BarcodeCapture.createRecommendedCameraSettings();

        BarcodeCaptureSettings settings = new BarcodeCaptureSettings();
        settings.enableSymbology(Symbology.CODE128, true);
        settings.enableSymbology(Symbology.CODE39, true);
        settings.enableSymbology(Symbology.QR, true);
        settings.enableSymbology(Symbology.EAN8, true);
        settings.enableSymbology(Symbology.UPCE, true);
        settings.enableSymbology(Symbology.EAN13_UPCA, true);

        barcodeCapture = BarcodeCapture.forDataCaptureContext(dataCaptureContext, settings);
        barcodeCapture.addListener(this);

        camera = com.scandit.datacapture.core.source.Camera.getDefaultCamera();

        if (camera != null) {
            camera.applySettings(cameraSettings);
        }

        dataCaptureContext.setFrameSource(camera);

        camera.switchToDesiredState(FrameSourceState.ON);

        dataCaptureView = DataCaptureView.newInstance(this, dataCaptureContext);

        setContentView(dataCaptureView);
    }

    @Override
    public void onBarcodeScanned(@NonNull BarcodeCapture barcodeCapture,
                                 @NonNull BarcodeCaptureSession session, @NonNull FrameData frameData) {

        Barcode barcode = session.getNewlyRecognizedBarcodes().get(0);

        barcodeCapture.setEnabled(false);

        String symbology = SymbologyDescription.create(barcode.getSymbology()).getReadableName();
        final String result = "Scanned: " + barcode.getData() + " (" + symbology + ")";

        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onObservationStarted(@NotNull BarcodeCapture barcodeCapture) {

    }

    @Override
    public void onObservationStopped(@NotNull BarcodeCapture barcodeCapture) {

    }

    @Override
    public void onSessionUpdated(@NotNull BarcodeCapture barcodeCapture, @NotNull BarcodeCaptureSession barcodeCaptureSession, @NotNull FrameData frameData) {

    }


    //----------------------------------- Bluetooth Methods ----------------------------------//

    private BluetoothAdapter mBluetoothAdapter = null;

    //public void onCreate(Bundle savedInstanceState) {
    private void setBluetooth(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "Este dispositivo no se puede conectar a Bluetooth.",
                    Toast.LENGTH_LONG).show();
            finish();

        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else
            initializeThread();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initializeThread();
    }

    private void initializeThread() {

        final SendData sendData = new SendData();
        sendData.start();
    }

    class SendData extends Thread {
        private BluetoothSocket btSocket = null;
        private OutputStream outStream = null;

        public SendData(){
            String address = getDefaultDeviceAdress();
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            mBluetoothAdapter.cancelDiscovery();
            try {
                btSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            Toast.makeText(getBaseContext(), "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void run() {

            Analyzer analyzer = new Analyzer(context);

            while (true) {
                try {

                    frame = videostreamPreviewTtView.getBitmap();
                    analyzer.analyze(frame);
                    code = analyzer.getRawValue();

                    outStream.write(code.getBytes());
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }


    }

    public String getDefaultDeviceAdress(){

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if (deviceName.equals(DEFAULT_DEVICE_NAME))
                    return deviceHardwareAddress;
            }
        }
        return null;
    }

}