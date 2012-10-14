package com.RCCAR.Tech4Human;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

/**
 * @author Administrator UDP에서 전송하는 부분
 * 
 */
public class TextSender
{
    private final int port = 8500; // 서버에서 설정한 UDP 포트번호를 추가합니다.
    
    private final String serverIP;
    
    private DatagramSocket socket;
    
    // edittext에서 문자열을 받아서 쓰기위한 생성자 부분
    public TextSender(String serverIP)
    {
        this.serverIP = serverIP;
    }
    
    // Udp 소켓을 전송하기 위한 메소드
    public void run(String msg)
    {
        try
        {
            // 소켓을 만듭니다.
            socket = new DatagramSocket();
            if (serverIP != null)
            {
                InetAddress serverAddr = InetAddress.getByName(serverIP);
                Log.d("UDP", serverIP);
                // TCP와 다르게 UDP는 byte단위로 데이터를 전송합니다. 그래서 byte를 생성해줍니다.
                byte[] buf = new byte[128];
                
                // 받아온 msg를 바이트 단위로 변경합니다.
                // DatagramPacket를 이용하여 서버에 접속합니다.
                DatagramPacket Packet = new DatagramPacket(buf, buf.length, serverAddr, port);
                // 헤더부분을 심습니다.
                buf[0] = 0x22;
                buf[1] = (byte) msg.getBytes("MS949").length;
                System.arraycopy(msg.getBytes("MS949"), 0, buf, 2, buf[1]);
                Log.d("UDP", "sendpacket.... " + new String(buf));
                // 소켓을 전송합니다.
                socket.send(Packet);
                Log.d("UDP", "send....");
                Log.d("UDP", "Done.");
            }
        }
        catch (Exception ex)
        {
            Log.d("UDP", "C: Error", ex);
        }
        finally
        {
            if (socket != null)
            {
                socket.close();
            }
        }
    }
}