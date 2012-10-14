package com.RCCAR.Tech4Human;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Control_androidActivity extends Activity
{
    /** Called when the activity is first created. */
    EditText ip;
    
    TextView myIP;
    
    Button start;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ip = (EditText) findViewById(R.id.IP);
        myIP = (TextView) findViewById(R.id.IP_MY_IP);
        start = (Button) findViewById(R.id.START);
        
        myIP.setText("MY IP : " + getLocalIpAddress());
        
        start.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                Toast.makeText(Control_androidActivity.this, "start", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(Control_androidActivity.this, StartRecv.class);
                intent.putExtra("ip", ip.getText().toString());
                startActivity(intent);
                
            }
        });
        
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
    
}