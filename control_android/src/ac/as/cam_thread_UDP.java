/*******************************************************************************************************
Copyright (c) 2011 Regents of the University of California.
All rights reserved.

This software was developed at the University of California, Irvine.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. All advertising materials mentioning features or use of this
   software must display the following acknowledgment:
   "This product includes software developed at the University of
   California, Irvine by Nicolas Oros, Ph.D.
   (http://www.cogsci.uci.edu/~noros/)."

4. The name of the University may not be used to endorse or promote
   products derived from this software without specific prior written
   permission.

5. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed at the University of
   California, Irvine by Nicolas Oros, Ph.D.
   (http://www.cogsci.uci.edu/~noros/)."

THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
IN NO EVENT SHALL THE UNIVERSITY OR THE PROGRAM CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package ac.as;

import java.io.IOException;
import java.lang.Thread;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class cam_thread_UDP extends Thread {
	int nb = 0;
	CamView cv;
	byte[] ReadBuf;// 수신버퍼
	byte[] size; // 크기버퍼
	byte[] mRGB; // 비트맵버
	DatagramPacket dp;
	DatagramSocket soc;
	int RGB_Size;
	StartRecv st;
	Message ReadHandMsg;	//핸들러 
	Handler mHandler;
	Options BitMap_option;
	
	Bitmap Bitmap_Buff;
	final static int SIZEBUF = 12000;
	final static int SizeINT = 4;

	private static int byteArrayToInt(byte[] bytes) {

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

	public cam_thread_UDP(CamView cv, Handler mHander) {
		this.cv = cv;
		this.mHandler = mHander;
		ReadHandMsg = mHander.obtainMessage();

		ReadBuf = new byte[SIZEBUF]; // 수신버퍼
		size = new byte[SizeINT]; // 크기버퍼
		mRGB = new byte[SIZEBUF]; // 비트맵버
		
		/*비트맵 출력을 조절해줍니다*/
		BitMap_option =new Options();
		BitMap_option.inSampleSize=2;
		

		
		/*통신설정*/
		dp = new DatagramPacket(ReadBuf, ReadBuf.length);
		try {
			soc = new DatagramSocket(9000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() {
		Log.d("tel", "redy");
		while (true) {
			try {
				soc.receive(dp);
				size[3] = ReadBuf[3];
				size[2] = ReadBuf[2];
				size[1] = ReadBuf[1];
				size[0] = ReadBuf[0];

				RGB_Size = byteArrayToInt(size);
				Log.d("tel", "recv size:" + RGB_Size);
				System.arraycopy(ReadBuf, 4, mRGB, 0, SIZEBUF - SizeINT);
				cv.mBitmap = BitmapFactory.decodeByteArray(mRGB, 0, RGB_Size,BitMap_option);
				cv.mBitmap=Bitmap.createScaledBitmap(cv.mBitmap, cv.mBitmap.getWidth()*2,cv.mBitmap.getHeight()*2 , false);
			
				
				
				 Bundle b = new Bundle();
				 Message msg = mHandler.obtainMessage();
				 msg.setData(b);
	            // mHandler.sendEmptyMessage((int)(Math.random()));
				
				
				mHandler.sendMessage(msg);
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}// end while
	}

	public void Recive_UDP() {
		

	}
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		soc.disconnect();
		soc.close();
		
		super.destroy();
	}
	

}
