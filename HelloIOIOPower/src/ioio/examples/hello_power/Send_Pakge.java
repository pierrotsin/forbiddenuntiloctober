package ioio.examples.hello_power;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;
public class Send_Pakge{
	
	final static int Len=64000;
	
	byte buff[];			//임시저장 버퍼
	byte Sendbuff[];	//전송버퍼
	byte sizeByte[];	  //size 버퍼
	
	InetAddress Addr;
	DatagramPacket dp;
	DatagramSocket soc;
	
	
	
	
	public Send_Pakge(String ip){
		buff =new byte[Len];
		Sendbuff =new byte[Len];
		sizeByte = new byte[4];
		
		try {
			Addr =InetAddress.getByName(ip);
			dp = new DatagramPacket(Sendbuff, 0, Sendbuff.length, Addr, 9000);
			try {
				soc = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static byte[] intToByteArray(int integer) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.putInt(integer);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.array();
	}

	public static int byteArrayToInt(byte[] bytes) {

		final int size = Integer.SIZE / 8;
		ByteBuffer buff = ByteBuffer.allocate(size);
		final byte[] newBytes = new byte[size];
		for (int i = 0; i < size; i++) {
			if (i + bytes.length < size) {
				newBytes[i] = (byte) 0x00;
			} else {
				newBytes[i] = bytes[i + bytes.length - size];
			}
		}
		buff = ByteBuffer.wrap(newBytes);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.getInt();
	}
	public void send(byte image[],int size){
		
		/*이미지 크기 측정*/
		sizeByte=intToByteArray(image.length);
		System.arraycopy(sizeByte, 0, Sendbuff, 0, 4);
		System.arraycopy(image, 0, Sendbuff, 4, size);
		
		try {
			soc.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
