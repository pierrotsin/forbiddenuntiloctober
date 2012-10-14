package com.IOIO.Tech4Human;

import android.hardware.SensorManager;
import android.view.SurfaceView;

public class Main_thread extends Thread
{
    SurfaceView parent_context;
    
    SensorManager mSensorManager = null;
    
    Cam_thread the_cam;
    
    String ip_address;
    
    MainActivity the_app;
    
    public Main_thread(MainActivity app, SurfaceView v, SensorManager m, String ip)
    {
        super();
        parent_context = v;
        mSensorManager = m;
        ip_address = ip;
        the_app = app;
        the_cam = new Cam_thread(parent_context, ip_address);
        // the_sensors = new Sensors_thread(mSensorManager,ip_address);
        
    }
    
    public void run()
    {
        
        the_cam.start_thread();
    }
    
    public void stop_simu()
    {
        the_cam.stop_thread();
        // the_sensors.stop_thread();
        
    }
}
