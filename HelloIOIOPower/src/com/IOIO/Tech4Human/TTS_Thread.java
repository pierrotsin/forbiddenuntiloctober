package com.IOIO.Tech4Human;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class TTS_Thread extends Thread
{
    
    private final TextToSpeech tts;
    
    private final Context context;
    
    private static final String TAG = "Android UDPReceive";
    
    private final int port = 8500;
    
    private DatagramPacket receivePacket;
    
    private DatagramSocket socket;
    
    public boolean isConn;
    
    byte[] buf;
    
    Thread thread = null;
    
    public TTS_Thread(Context context)
    {
        this.context = context;
        tts = new TextToSpeech(context, new OnInitListener() {
            
            @Override
            public void onInit(int status)
            {
                // TODO Auto-generated method stub
                if (status != TextToSpeech.ERROR)
                {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        Log.d(TAG, "ttsCreate");
    }
    
    // Udp 소켓으로 부터 수신받은 메시지를 액티비티에 전달하기 위한 핸들러
    
    @Override
    public void run()
    {
        try
        {
            socket = new DatagramSocket(port); //
            Log.d(TAG, "S: Connecting...");
            while (isConn)
            { // Client에서 요청을 기다려야 하기 때문에 while문을 사용합니다.
                buf = new byte[128]; // byte를 선언 합니다.
                receivePacket = new DatagramPacket(buf, buf.length);
                Log.d(TAG, "S: Receiving...");
                // 소켓을 수신 받습니다.
                socket.receive(receivePacket);
                buf = receivePacket.getData();
                String sToSpeak = new String(buf, 2, buf[1], "MS949");
                Log.d(TAG, "S: Received : buf[0]:" + buf[0] + ", buf[1]: " + buf[1] + ", 문자열:" + sToSpeak);
                Log.d(TAG, "S: Done.");
                tts.speak(sToSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (socket != null) socket.close();
        }
    }
    
    public void ttsResume()
    {
        isConn = true;
        thread = new Thread(this);
        thread.start();
        Log.d(TAG, "ttsResume");
    }
    
    public void ttsPause()
    {
        isConn = false;
        Log.d(TAG, "ttsPause");
    }
    
    public void ttsStop()
    {
        if (tts != null)
        {
            tts.stop();
            tts.shutdown();
        }
        Log.d(TAG, "ttsStop");
    }
}
