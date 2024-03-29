package com.IOIO.Tech4Human;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceView;

public class Cam_thread
{
    Camera mCamera;
    
    YuvImage mYuvImage;
    
    Bitmap mBitmap;
    
    int width_ima, height_ima;
    
    private static final String TAG = "IP_cam";
    
    SurfaceView parent_context;
    
    Send_Pakge sp;
    
    private boolean STOP_THREAD;
    
    String ip_address;
    
    public Cam_thread(SurfaceView context, String ip)
    {
        parent_context = context;
        sp = new Send_Pakge(ip);
    }
    
    private void init()
    {
        try
        {
            
            mCamera = Camera.open();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(640, 480);
            parameters.setPreviewFrameRate(15);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(parent_context.getHolder());
            mCamera.setPreviewCallback(new cam_PreviewCallback());
            mCamera.startPreview();
        }
        catch (Exception exception)
        {
            Log.e(TAG, "Error: ", exception);
        }
    }
    
    public void start_thread()
    {
        init();
    }
    
    public void stop_thread()
    {
        STOP_THREAD = true;
        
    }
    
    /*
     * 사용하지 않음. public void send_data_UDP() { if (mBitmap != null) {
     * 
     * Log.d("cam", "send"); ByteArrayOutputStream byteStream = new
     * ByteArrayOutputStream();
     * 
     * mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream); // !!!!!!!
     * // change // compression // rate // to // change // packets // size
     * 
     * byte data[] = byteStream.toByteArray(); sp.send(data, data.length);
     * 
     * } }
     * 
     * static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
     * int height) { final int frameSize = width * height;
     * 
     * for (int j = 0, yp = 0; j < height; j++) { int uvp = frameSize + (j >> 1)
     * * width, u = 0, v = 0; for (int i = 0; i < width; i++, yp++) { int y =
     * (0xff & ((int) yuv420sp[yp])) - 16; if (y < 0) y = 0; if ((i & 1) == 0) {
     * v = (0xff & yuv420sp[uvp++]) - 128; u = (0xff & yuv420sp[uvp++]) - 128; }
     * 
     * int y1192 = 1192 * y; int r = (y1192 + 1634 * v); int g = (y1192 - 833 *
     * v - 400 * u); int b = (y1192 + 2066 * u);
     * 
     * if (r < 0) r = 0; else if (r > 262143) r = 262143; if (g < 0) g = 0; else
     * if (g > 262143) g = 262143; if (b < 0) b = 0; else if (b > 262143) b =
     * 262143;
     * 
     * rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b
     * >> 10) & 0xff); } } }
     */
    
    // Preview callback used whenever new frame is available...send image via
    // UDP !!!
    private class cam_PreviewCallback implements PreviewCallback
    {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera)
        {
            if (STOP_THREAD == true)
            {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                return;
            }
            
            if (mBitmap == null) // create Bitmap image first time
            {
                Camera.Parameters params = camera.getParameters();
                width_ima = params.getPreviewSize().width;
                height_ima = params.getPreviewSize().height;
            }
            
            // decodeYUV420SP(mRGBData, data, width_ima, height_ima);
            // mBitmap.setPixels(mRGBData, 0, width_ima, 0, 0, width_ima,
            // height_ima);
            
            mYuvImage = new YuvImage(data, ImageFormat.NV21, width_ima, height_ima, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Rect rect = new Rect(0, 0, width_ima, height_ima);
            mYuvImage.compressToJpeg(rect, 50, baos);
            byte[] toSendData = baos.toByteArray();
            sp.send(toSendData, toSendData.length);
            
            // send_data_UDP(); 사용하지 않음.
        }
    }
    
}