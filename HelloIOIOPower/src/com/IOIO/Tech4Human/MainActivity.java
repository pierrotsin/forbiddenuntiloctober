package com.IOIO.Tech4Human;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity
{

    TextView title_;
    
    ToggleButton button_;
    
    IOIOThread ioio_thread_;
    
    OnKeyListener ok;
    
    final int _forward = 1, _reverse = 2, _stop = 0;
    
    int direction;
    
    /* 카메라 */
    Cam_thread cam;
    
    SurfaceView view;
    
    MainActivity the_app;
    
    ToggleButton togglebutton;
    
    TextView myIP;
    
    TextView IOIOstate;
    
    EditText ip_text;
    
    EditText gpspoint;
    
    String IP_address;
    
    byte[] ReadBuf;
    
    byte[] controlBuf;
    
    boolean isRecv;
    
    boolean Recflag = false;
    
    Thread re;
    
    DigitalOutput LED;
    
    DigitalOutput Ven1;
    
    DigitalOutput C1, D1;
    
    DigitalOutput Ven2, C2, D2;
    
    PwmOutput pwmout_11;
    
    PulseInput pulsein_10;
    
    IOIO ioio_, ioio_2;
    
    Handler mHandler;
    
    MainActivity ma;
    
    Point Destny_point;
    
    AutoThead auto;
    
    InterruptThead it;
    
    TTS_Thread tts;
    
    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        title_ = (TextView) findViewById(R.id.title);
        myIP = (TextView) findViewById(R.id.IP_MY_IP);
        
        String IP_ADDR = "";
        IP_ADDR = getLocalIpAddress();
        if (IP_ADDR != null)
        {
            
            myIP.setText(IP_ADDR);
        }
        
        button_ = (ToggleButton) findViewById(R.id.button);
        ip_text = (EditText) findViewById(R.id.IP_edit_txt);
        
        IOIOstate = (TextView) findViewById(R.id.title);
        
        ma = this;
        view = new SurfaceView(this);
        
        direction = _stop;
        the_app = this;
        
        isRecv = false;
        ReadBuf = new byte[100];
        ReadBuf[0] = 0x00;
        ReadBuf[1] = 0x00;
        
        controlBuf = new byte[20];
        controlBuf[0] = 0x00;
        controlBuf[1] = 0x00;
        
        re = new RecCon(ReadBuf, (TextView) findViewById(R.id.state));
        re.start();
        Destny_point = new Point();
        AutoThead.Auto_isRun = false;
        
        tts = new TTS_Thread(this);
        
        button_.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                
                if (button_.isChecked())
                {
                    IP_address = ip_text.getText().toString();
                    cam = new Cam_thread(view, IP_address);
                    cam.start_thread();
                    
                    Toast.makeText(MainActivity.this, "Start streaming " + "ip: " + IP_address, Toast.LENGTH_SHORT)
                            .show();
                }
                else
                {
                    cam.stop_thread();
                    Toast.makeText(MainActivity.this, "Stop streaming", Toast.LENGTH_SHORT).show();
                }
                
            }
        });
        
        ioio_thread_ = new IOIOThread();
        ioio_thread_.start();
        
    }// END onCreate
    
    public String getLocalIpAddress()
    {
        final String IP_NONE = "N/A";
        final String WIFI_DEVICE_PREFIX = "eth";
        
        String LocalIP = IP_NONE;
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        if (LocalIP.equals(IP_NONE))
                            LocalIP = inetAddress.getHostAddress().toString();
                        else if (intf.getName().startsWith(WIFI_DEVICE_PREFIX))
                            LocalIP = inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException e)
        {
            Log.e("getLocalIpAddress()", "getLocalIpAddress Exception:" + e.toString());
        }
        return LocalIP;
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        tts.ttsResume();
    }
    
    /**
     * Called when the application is paused. We want to disconnect with the
     * IOIO at this point, as the user is no longer interacting with our
     * application.
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        ioio_thread_.abort();
        re.stop();
        tts.ttsPause();
        try
        {
            ioio_thread_.join();
            cam.stop_thread();
        }
        catch (Exception e)
        {
            
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
        tts.ttsStop();
    }
    
    public void runAuto()
    {
        
        auto = new AutoThead(this, ip_text.getText().toString());
        it = new InterruptThead(auto);
        auto.init();
        it.init();
    }
    
    public void stopAuto()
    {
        /**
         * 가장 중요한 부분중에 하나이다. 스레드는 데몬에 등록시키면 해당 루프가 정지되었을때에 프로세서가 해제되는 기능이 있다.
         * 이것으로 자동조종의 스레드를 종료 시킬수 있다.
         */
        auto.stop_thread();
        it.stop_thread();
        
    }
    
    class RecCon extends Thread
    {
        DatagramPacket dp;
        
        DatagramSocket soc;
        
        byte Buff[];
        
        byte tempBuff[];
        
        TextView tv;
        
        int index;
        
        public RecCon(byte Buff[], TextView tv)
        {
            // TODO Auto-generated constructor stub
            this.Buff = Buff;
            this.tv = tv;
            tempBuff = new byte[8];
            index = 0;
            Log.d("rec", "rec Len" + ReadBuf.length);
            dp = new DatagramPacket(ReadBuf, 0, ReadBuf.length);
            
            try
            {
                soc = new DatagramSocket(7000);
            }
            catch (SocketException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        @Override
        public void run()
        {
            Log.d("recive", "rec start");
            while (true)
            {
                // for(int i=0;i<10;i++)
                Log.d("rec", ReadBuf[0] + " " + ReadBuf[1] + " " + ReadBuf[2] + " " + ReadBuf[3] + " " + ReadBuf[4]);
                try
                {
                    soc.receive(dp);
                    if (ReadBuf[0] == 0x10 || ReadBuf[0] == 0x01 || ReadBuf[1] == 0x10 || ReadBuf[1] == 0x01
                            || (ReadBuf[0] == 0x00 && ReadBuf[1] == 0x00))
                    {
                        controlBuf[0] = ReadBuf[0];
                        controlBuf[1] = ReadBuf[1];
                        
                    }
                    else if (ReadBuf[0] == 0x55 && ReadBuf[1] == 0x55)
                    {// gps
                     // point
                     // 받을때
                        Log.d("rec", "rec");
                        
                        Destny_point.index.add(((int) ReadBuf[18]));
                        // Destny_point.clear();
                        
                        System.arraycopy(ReadBuf, 2, tempBuff, 0, tempBuff.length);
                        Destny_point.lat.add(BytetoDouble(tempBuff));
                        System.arraycopy(ReadBuf, 10, tempBuff, 0, tempBuff.length);
                        Destny_point.lon.add(BytetoDouble(tempBuff));
                        
                        for (int i = 0; i < Destny_point.index.size(); i++)
                        {
                            // Log.d("GPS_auto",);
                            Log.d("Auto_rec",
                                  "lat: " + Destny_point.lat.get(i) + " " + "lon: " + Destny_point.lon.get(i)
                                          + " index: " + Destny_point.index.get(i) + " : " + i);
                        }
                        if (ReadBuf[18] == 0x00)
                        {
                            Log.d("Auto_rec", "auto start");
                            // if (cam != null)
                            // cam.stop_thread(); // 화면보내지않음
                            
                            ReadBuf[0] = 0x00;
                            ReadBuf[1] = 0x00;
                        }
                        
                    }
                    else if (ReadBuf[0] == 0x23 && ReadBuf[1] == 0x23)
                    {// 자동모드에서
                     // 전환
                        Log.d("rec", "init auto");
                        // ma.mHandler.sendEmptyMessage((int)
                        // (Math.random()*100000));
                        // ma.runAuto();
                        
                    }
                    else if (ReadBuf[0] == 0x44 && ReadBuf[1] == 0x44)
                    {// 자동모드로
                        Log.d("rec", "cam_stop cmd");
                        if (cam != null) cam.stop_thread(); // 화면보내지않음
                            
                        Destny_point.index.add(0);
                        Destny_point.index.add(1);
                        
                        Destny_point.lat.add(37.339937);
                        Destny_point.lon.add(126.733547);
                        
                        Destny_point.lat.add(37.34041134);
                        Destny_point.lon.add(126.73409767);
                        mHandler.sendEmptyMessage((int) (Math.random() * 100000));
                        // auto.init();
                        // it.start();
                        // it.isRun = true;
                        
                        ReadBuf[0] = 0x00;
                        ReadBuf[1] = 0x00;
                        
                    }
                    else if (ReadBuf[0] == 0x24 && ReadBuf[1] == 0x24)
                    {
                        auto.Stop_Auto();
                        ReadBuf[0] = 0x00;
                        ReadBuf[1] = 0x00;
                    }
                    
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        public double BytetoDouble(byte buf[])
        {
            double temp;
            ByteBuffer bb = ByteBuffer.wrap(buf);
            return bb.getDouble();
            
        }
    }
    
    class IOIOThread extends Thread
    {
        
        byte temp[];
        
        int echoSeconds;
        
        int echoDistanceCm = 100;
        
        float fTemp;
        
        private boolean abort_ = false;
        
        /** Thread body. */
        @Override
        public void run()
        {
            super.run();
            while (true)
            {
                Log.d("HelloIOIOPower", "IOIO start");
                synchronized (this)
                {
                    if (abort_)
                    {
                        break;
                    }
                    ioio_ = IOIOFactory.create();
                }
                try
                {
                    setText("wait");
                    
                    ioio_.waitForConnect();
                    setText("ioio_connected");
                    
                    setText("ioio_start_Thread");
                    
                    temp = new byte[2];
                    temp[0] = 0x00;
                    temp[1] = 0x00;
                    
                    // Ven1 = ioio_.openPwmOutput(3, 100);
                    Ven1 = ioio_.openDigitalOutput(3);
                    C1 = ioio_.openDigitalOutput(2);
                    D1 = ioio_.openDigitalOutput(1);
                    
                    Ven2 = ioio_.openDigitalOutput(4);
                    C2 = ioio_.openDigitalOutput(5);
                    D2 = ioio_.openDigitalOutput(6);
                    
                    LED = ioio_.openDigitalOutput(0);
                    LED.write(true);
                    
                    // trigger 11
                    pwmout_11 = ioio_.openPwmOutput(11, 100);// 11번 핀으로 100hz의
                                                             // pwmoutput
                                                             // 발사(트리거)
                    pulsein_10 = ioio_.openPulseInput(10, PulseMode.POSITIVE);
                    pwmout_11.setPulseWidth(13);
                    
                    while (true)
                    {
                        if (AutoThead.Auto_isRun) getMeter();
                        
                        setMoter();
                        
                        sleep(10);
                    }
                    
                }
                catch (ConnectionLostException e)
                {
                }
                catch (Exception e)
                {
                    Log.e("HelloIOIOPower", "Unexpected exception caught", e);
                    ioio_.disconnect();
                    break;
                }
                finally
                {
                    try
                    {
                        ioio_.waitForDisconnect();
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        }// end run
        
        void setMoter() throws ConnectionLostException
        {
            /**
             * 루프안에서 드라이버의 핀을 제어합니다.
             */
            
            if (controlBuf[0] == 0x01)
            {
                setState("left");
                Ven1.write(true);
                C1.write(true);
                D1.write(false);
            }
            else if (controlBuf[0] == 0x10)
            {
                // Ven1.write(true);
                Ven1.write(true);
                C1.write(false);
                D1.write(true);
            }
            else
            {
                // Ven1.write(false);
                Ven1.write(false);
                C1.write(false);
                D1.write(false);
                
            }
            
            if (controlBuf[1] == 0x01)
            {
                Ven2.write(true);
                C2.write(true);
                D2.write(false);
            }
            else if (controlBuf[1] == 0x10)
            {
                Ven2.write(true);
                C2.write(false);
                D2.write(true);
            }
            else
            {
                Ven2.write(false);
                
            }
            
            // 버퍼초기화
            temp[0] = ReadBuf[0];
            temp[1] = ReadBuf[1];
            
        }
        
        void getMeter() throws InterruptedException, ConnectionLostException
        {
            
            fTemp = pulsein_10.getDuration();
            echoSeconds = (int) (fTemp * 1000 * 1000);
            echoDistanceCm = echoSeconds / 29 / 2;
            
            setText(Float.toString(fTemp));// 듀레이션
            setText(Integer.toString(echoSeconds));// 1차가공
            
            if (echoSeconds > 30000)
            { // 에코사운드가 3만이상일시 ->
              // NO_Detected
                setText("No_Detected");
            }
            else
            {
                
                setText(Integer.toString(echoDistanceCm));// 2차가공
            }
        }
        
        /**
         * Abort the connection.
         * 
         * This is a little tricky synchronization-wise: we need to be handle
         * the case of abortion happening before the IOIO instance is created or
         * during its creation.
         */
        synchronized public void abort()
        {
            abort_ = true;
            if (ioio_ != null)
            {
                ioio_.disconnect();
            }
        }
        
        /**
         * Set the text line on top of the screen.
         * 
         * @param id
         *            The string ID of the message to present.
         */
        
        public void setText(final String id)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    title_.setText(id);
                    
                }
            });
            
        }
        
        public void setState(final String id)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    IOIOstate.setText(id);
                    
                }
            });
            
        }
        
    }
}