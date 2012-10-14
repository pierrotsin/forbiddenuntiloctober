package com.RCCAR.Tech4Human;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class CamView extends View
{
    
    Bitmap mBitmap;
    
    Bitmap srcBitmap;
    
    Button btn_forward;
    
    Button btn_back;
    
    Button btn_forwad_right;
    
    Button btn_forwad_left;
    
    Button btn_back_right;
    
    Button btn_back_left;
    
    Button btn_right;
    
    Button btn_left;
    
    int displayWidth, displayHeight;
    
    static final int Button_size = 120;
    
    InetAddress Addr;
    
    DatagramPacket dp;
    
    DatagramSocket soc;
    
    byte buff[];
    
    boolean Tr_switch; // 스레드 스위치
    
    boolean isSend;
    
    Touch tr;
    
    CamView cv;
    
    TextSender sender;
    
    Vibrator vibrator;
    
    public CamView(Context context, String ip)
    {
        super(context);
        this.setClickable(true);
        
        // TODO Auto-generated constructor stub
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        displayWidth = display.getWidth();
        displayHeight = display.getHeight();
        
        btn_back = new Button(displayWidth - Button_size * 2, displayHeight - Button_size * 2, Button_size, Button_size);
        
        btn_back_right = new Button(displayWidth - Button_size, displayHeight - Button_size * 2, Button_size,
                Button_size);
        btn_back_left = new Button(displayWidth - Button_size * 3, displayHeight - Button_size * 2, Button_size,
                Button_size);
        
        btn_forward = new Button(displayWidth - Button_size * 2, displayHeight - Button_size * 4, Button_size,
                Button_size);
        
        btn_forwad_right = new Button(displayWidth - Button_size, displayHeight - Button_size * 4, Button_size,
                Button_size);
        btn_forwad_left = new Button(displayWidth - Button_size * 3, displayHeight - Button_size * 4, Button_size,
                Button_size);
        
        btn_right = new Button(displayWidth - Button_size, displayHeight - Button_size * 3, Button_size, Button_size);
        btn_left = new Button(displayWidth - Button_size * 3, displayHeight - Button_size * 3, Button_size, Button_size);
        
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nowloading);
        mBitmap = Bitmap.createScaledBitmap(srcBitmap, 640, 480, true);
        
        Tr_switch = true;
        isSend = false;
        
        buff = new byte[100];
        cv = this;
        
        sender = new TextSender(ip);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        try
        {
            Addr = InetAddress.getByName(ip);
            dp = new DatagramPacket(buff, 0, buff.length, Addr, 7000);
        }
        catch (UnknownHostException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try
        {
            soc = new DatagramSocket();
        }
        catch (SocketException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d("touch", "create");
        tr = new Touch();
        tr.start();
        
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawBitmap(mBitmap, 0, 50, null);// 영상 위치조정
        btn_right.Draw(canvas);
        btn_back.Draw(canvas);
        
        btn_back_right.Draw(canvas);
        btn_back_left.Draw(canvas);
        
        btn_forward.Draw(canvas);
        
        btn_forwad_right.Draw(canvas);
        btn_forwad_left.Draw(canvas);
        
        btn_left.Draw(canvas);
        // Button bu;
        // bu.
        // canvas.drawBitmap(Bitmap.createScaledBitmap(mBitmap,
        // mBitmap.getDensity()*2, mBitmap.getHeight()*2, false), 0, 0, null);
        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // 터치시 좌표값만 전달
        // touchX=event.getX();
        // touchY=event.getY();
        
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                Log.d("touch", "toch");
                touchPoint(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                buff[0] = 0x00;
                buff[1] = 0x00;
                break;
            
            default:
                // buff[0] = 0x00;
                // buff[1] = 0x00;
                break;
        }
        
        return true;
    }
    
    public void touchPoint(float X, float Y)
    {
        
        if (btn_back.IsClicked(X, Y))
        {
            buff[0] = 0x10;
        }
        if (btn_back_left.IsClicked(X, Y))
        {
            buff[0] = 0x10;
            buff[1] = 0x01;
        }
        if (btn_back_right.IsClicked(X, Y))
        {
            buff[0] = 0x10;
            buff[1] = 0x10;
        }
        
        if (btn_forward.IsClicked(X, Y))
        {
            buff[0] = 0x01;
        }
        
        if (btn_forwad_left.IsClicked(X, Y))
        {
            buff[0] = 0x01;
            buff[1] = 0x01;
        }
        if (btn_forwad_right.IsClicked(X, Y))
        {
            buff[0] = 0x01;
            buff[1] = 0x10;
        }
        if (btn_left.IsClicked(X, Y))
        {
            buff[1] = 0x01;
        }
        
        if (btn_right.IsClicked(X, Y))
        {
            buff[1] = 0x10;
        }
        
        if (X <= 80 && Y >= 400)
        {
            SendText(getContext());
        }
        Log.d("touch", "buff[0]: " + buff[0] + "\t" + "buff[1]: " + buff[1]);
        
    }
    
    class Touch extends Thread
    {
        /*
         * 연속적인 udp의 송신에 0x00을 전송하기 위한 스레드
         */
        boolean isSend = true;
        
        @Override
        public void run()
        {
            
            while (isSend)
            {
                // 눌렀을때에 좌표연산
                
                try
                {
                    soc.send(dp);
                    Log.d("touch", "buff[0]: " + buff[0] + "\t" + "buff[1]: " + buff[1]);
                    
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                try
                {
                    Thread.sleep(30);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // Log.d("is");
                
            }// end while
        }// end run
        
        @Override
        public void destroy()
        {
            // TODO Auto-generated method stub
            Log.d(VIEW_LOG_TAG, "destroy");
            
            super.destroy();
        }
        
        @Override
        public void interrupt()
        {
            // TODO Auto-generated method stub
            Log.d(VIEW_LOG_TAG, "destroy");
            isSend = false;
            try
            {
                Thread.sleep(30);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            soc.disconnect();
            soc.close();
            super.interrupt();
        }
    }
    
    public void SendText(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog, null);
        new AlertDialog.Builder(context).setTitle("텍스트 전송").setView(view)
                .setNegativeButton("전송", new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        EditText editText = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText1);
                        String str = new String(editText.getText().toString());
                        if (str != "") sender.run(str);
                    }
                }).show();
    }
}
