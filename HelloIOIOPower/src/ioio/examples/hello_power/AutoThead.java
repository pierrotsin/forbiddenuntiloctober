package ioio.examples.hello_power;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Config;
import android.util.Log;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

/*노트
 * 1.학교는 처음에 수신되는 gps신호의 오차가 상당히 큰 수준이다. 그래서 충분히 많은수의 신호를 수신받아서
 * 오차의 범위를 줄인다.
 * 
 * 2. 또한 도착되는 거리의 오차를 5m로 설정한다.
 * 
 * 3. 나침반은 차량에 설치될때에 한차례 기울어 짐으로서 90도의 차이가 난다.
 * 
 * 4.이상의 상황에 맞게 코드를 수정하였다.
 * 
 * 
 */
interface Interrupt {
	final int stop = -1;
	final int wait = 0;
	final int avoid = 1;
	final int reach = 2;
	final int dgree = 3;
	final int move = 4;
	final String state = "state";
	final byte right = 0x10;
	final byte left = 0x01;
	final byte forward = 0x01;
}

public class AutoThead extends Thread implements Interrupt {
	/**
	 * Main에 있는 스레드에서 제어권을 가져오는 스레드입니다. 컨트롤을 받아오는 버퍼에 따라서 정지함수와 다시시작해주는 함수가
	 * 필요합니다
	 */
	boolean gps_interrup;
	boolean com_interrup;
	boolean range_interrup;
	static boolean Auto_isRun;
	Thread com, gps, range;

	int presentState;
	int interruptStatae;
	InterruptThead it;
	// ////////////////////////////
	LocationManager locationManager = null;
	LocationListener mLocationListener;
	double Car_lat, Car_lon;
	double Desteny_lat, Desteny_lon;
	Point Destny_point;
	int Destny_point_reach;
	// /////////// GPS/////////////////////

	
	float axis_X;
	double rad,rad_temp,angle;

	
	float longR[];
	float longS[];
	float distan[];
	
	
	float longR1[];
	float longS1[];
	float distan1[];
	
	
	float[] valuesAccelerometer;
	float[] valuesMagneticField;
	float[] matrixR;
	float[] matrixI;
	float[] matrixValues;
	
	
	
	
	double pre_point[];
	double next_point[];
	byte flagPoint_Direct;
	
	// ///////////Com//////////////////////

	int echoDistanceCm;
	// ///////////Range//////////////////////

	String best = null;
	String Strmsg;

	Message ReadHandMsg; // 핸들러
	Handler mHandler;
	MainActivity autoAct;
	// //////////Debug/////////////////////
	Sensor mAccSenosr,mMaginSensor;
	SensorManager mAccManager,mMaSensorManager;
	SensorEventListener mAccLister,mMaginLister;
	///////////Dgree////////////////
	 double azimuth;

	
	String ip;
	InetAddress Addr;
	DatagramSocket dataSoc;
	DatagramPacket dataPack;
	byte SendBuf[];
	byte memBuf[];
	byte IntmemBuf[];
	//////////UDP//////////////////////

	public AutoThead(final MainActivity autoAct,String ip) {
		gps_interrup = false;
		com_interrup = false;
		range_interrup = false;
		Auto_isRun = false;
		this.ip=ip;
		Car_lat = 100;
		Car_lon = 100;

		
		
		
		longR = new float[3];
		longS = new float[3];
		distan = new float[3];
		
		longR1 = new float[3];
		longS1 = new float[3];
		distan1 = new float[3];
		
		
		valuesAccelerometer=new float[3];
		valuesMagneticField=new float[3];
		matrixR = new float[9];
		matrixI = new float[9];
		matrixValues = new float[3];
		
	
		
		pre_point=new double[2];
		next_point=new double[2];
		flagPoint_Direct=-1;
		

		Destny_point_reach = 0;
		presentState = 0;
		rad = 0;
		angle=0;
		rad_temp=0;

		presentState = 0;

		this.autoAct = autoAct;


		autoAct.controlBuf[0] = 0x00;
		autoAct.controlBuf[1] = 0x00;

		mLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if (location != null) {

					Car_lat = location.getLatitude();
					Car_lon = location.getLongitude();
					
					
					Log.d("state_gps", "" + Car_lat + ":" + Car_lon);
					Location.distanceBetween(Car_lat, Car_lon, Desteny_lat,Desteny_lon, longR); // 빗변
					Location.distanceBetween(Car_lat, Car_lon, Car_lat,Desteny_lon, longS); // 높이
					
					
					if(presentState != stop){
						presentState = 4;
					}
					reach();
					
					
					

				}
			}

			public void onProviderDisabled(String arg0) {
				Log.d("state_gps", "ProviderDisabled");

			}

			public void onProviderEnabled(String arg0) {
				Log.d("state_gps", "onProviderEnabled");

			}

			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

			}
		};
		 
		locationManager = (LocationManager) autoAct.getSystemService(Context.LOCATION_SERVICE);
		
		
		/////Acc SensorSet///
		mAccManager= (SensorManager)autoAct.getSystemService(autoAct.SENSOR_SERVICE);
		mAccSenosr=mAccManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccLister =new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				{
					//Log.d("acc", event.values[0]+" "+event.values[1]+" "+event.values[2]);
					for(int i =0; i < 3; i++)
						valuesAccelerometer[i] = event.values[i];
				
					 
					 
					
				}	    
					
				
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		}; 
		
		
		
		
		////Maginet Set/////
		mMaSensorManager= (SensorManager)autoAct.getSystemService(autoAct.SENSOR_SERVICE);
		mMaginSensor= mMaSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mMaginLister=new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				{
					//Log.d("mac", event.values[0]+" "+event.values[1]+" "+event.values[2]);
					for(int i =0; i < 3; i++)
						valuesMagneticField[i] = event.values[i];
					
					if(SensorManager.getRotationMatrix(matrixR,matrixI,valuesAccelerometer,valuesMagneticField))
					 {
						 SensorManager.getOrientation(matrixR, matrixValues);
						 azimuth = Math.toDegrees(matrixValues[0]);
						 int cal=0;
						 
						angle = Math.asin(((double) longS[0]) / ((double) longR[0]))* 180 / Math.PI;// 각도
						 
						if(Car_lat>Desteny_lat && Car_lon > Desteny_lon){
							//Log.d("MakerID","no 3");
							cal=3;
							angle=270-angle;
						}else if(Car_lat<Desteny_lat && Car_lon > Desteny_lon){
							//Log.d("MakerID","no 4");
							cal=4;
							angle=angle+270;
						}else if(Car_lat>Desteny_lat && Car_lon<Desteny_lon){
							/*1번 사항*/
							cal=1;
							angle=angle+90;
						}else if(Car_lat<Desteny_lat && Car_lon < Desteny_lon){
							//Log.d("MakerID","no 2");
							cal=2;
							/*2번 사항*/
							angle=90-angle;
						}
						
						 //rad=angle;
						 //angle=90;
						
						 /*각도 보정*/
						 azimuth= azimuth >0 ? azimuth: 360+azimuth; 
						 if(azimuth >= 0 && azimuth<270)
						 {
							   azimuth=azimuth+90;
						 }
						 else
						 {
							   azimuth=azimuth -270;
						 }
						 
						 rad =  angle - azimuth; // 각도차 완성
						 
					/*	 
						AutoThead.this.autoAct.ioio_thread_.setRadText("(rad "+ (int)rad+") =  "+"("+(int)angle+") - "+"(azimuth"+(int)azimuth+")");
						AutoThead.this.autoAct.ioio_thread_.setDistanceState("distace "+(int)longR[0]+"m");
						AutoThead.this.autoAct.ioio_thread_.setPointState("point "+Destny_point_reach);
						if(autoAct.controlBuf[1] ==0x01)
							AutoThead.this.autoAct.ioio_thread_.setCalState("left turn");
						else if (autoAct.controlBuf[1] ==0x10)
							AutoThead.this.autoAct.ioio_thread_.setCalState("right turn");
						else 
							AutoThead.this.autoAct.ioio_thread_.setCalState("forward");
						*/
						 
						 Send_Car_Point();
						
					 }		
					
					
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
		
		axis_X = 0;
	}
	public  byte[] intToByteArray(int integer) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.putInt(integer);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.array();
	}
	public void Send_Car_Point(){
		memBuf = DoubletoByteArray(Car_lat);
		//System.arraycopy(memBuf, 0, SendBuf, 0, memBuf.length);
		for(int i=0;i<8;i++)
			SendBuf[i]=memBuf[i];
		memBuf = DoubletoByteArray(Car_lon);
		for(int i=0;i<8;i++)
			SendBuf[i+8]=memBuf[i];
		//System.arraycopy(memBuf, 0, SendBuf, 8, memBuf.length);
		IntmemBuf=intToByteArray((int)azimuth);
		for(int i=0;i<4;i++)
			SendBuf[i+16]=IntmemBuf[i];
		
		//System.arraycopy(IntmemBuf,0,SendBuf,16,IntmemBuf.length);
		try {
			dataSoc.send(dataPack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("Car", "Sned "+dataPack.getPort()+" "+dataPack.getAddress()+" "+ip);
	}
	public void Send_Car_End(){
		SendBuf[28]=0x17;
		SendBuf[29]=0x17;
		try {
			dataSoc.send(dataPack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init() {
		setDaemon(true);		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 정밀도
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM); // 소비전력량
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(false);
		best = locationManager.getBestProvider(criteria, true);
		
		locationManager.requestLocationUpdates(best, 100, 0, mLocationListener);
		mAccManager.registerListener(mAccLister, mAccSenosr, Sensor.TYPE_ACCELEROMETER);
		mMaSensorManager.registerListener(mMaginLister, mMaginSensor, Sensor.TYPE_MAGNETIC_FIELD);
		
		presentState=wait;
		Auto_isRun = true;
		this.start();

		Desteny_lat = autoAct.Destny_point.lat.get(0);
		Desteny_lon = autoAct.Destny_point.lon.get(0);
		
		
		try {
			memBuf=new byte[8];
			SendBuf=new byte[30];
			IntmemBuf=new byte[4];
			Addr = InetAddress.getByName(ip);
			dataPack= new DatagramPacket(SendBuf, SendBuf.length, Addr, 8000);
			dataSoc =new DatagramSocket();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//조종 :192.168.10.108
	//자동차 :192.168.10.106
	public void stop_thread(){
		
		Auto_isRun = false;
		locationManager.removeUpdates(mLocationListener);
		mAccManager.unregisterListener(mAccLister);
		mMaSensorManager.unregisterListener(mMaginLister);
		dataSoc.close();
	}
	

	public boolean compInterrupt() {

		return presentState > interruptStatae ? false : true;

	}

	public void run() {
		// 우선순위에 따라서 스레드들의 컨트롤을 해줍니다.
		while (true) {
			if (Auto_isRun) {
				//AutoThead.this.autoAct.ioio_thread_.setLoopState("loop "+ presentState);
				switch (presentState) {
				case avoid:
					avoid();
					break;
				case dgree:
					DgreeMove();
					break;
				case move:
					move();
					break;
				default:
					break;

				}
				// autoAct.ioio_thread_.setText(Integer.toString(this.presentState)
				// );
			}
		}// end while

	}

	public void Stop_Auto() {
		
		locationManager.removeUpdates(mLocationListener);
		Auto_isRun = false;
		it.isRun = false;
	}

	// //////////////////////각종 동작에 관한 루프//////////////////
	public void reach() {
		// 도착점에 도착했을때의 루프
		Location.distanceBetween(Car_lat, Car_lon, Desteny_lat, Desteny_lon,distan);
		if (distan[0] <= 15.0) {
			++Destny_point_reach;
			if( Destny_point_reach+1 < autoAct.Destny_point.lat.size())
			{
				Desteny_lat = autoAct.Destny_point.lat.get(Destny_point_reach);
				Desteny_lon = autoAct.Destny_point.lon.get(Destny_point_reach);
			}
			else
			{
				presentState=stop;
				Send_Car_End();
			}
			
		}

	}

	public void move() {
		// 단순히 전진하는 루프

		while (true) {

			if (presentState < 4) {
				// 좀더 위기상황이 발생하면 루프탈출
				break;
			}
			autoAct.controlBuf[0] = 0x01;
			autoAct.controlBuf[1] = 0x00;

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}// end while
	}

	public void avoid() {
		// 1초정도 우또는 좌회전하고서 후진, 그뒤에 다시 루프 복귀
		presentState = avoid;
		int i = 0; // 100ms단위로 체크
		
		byte dir = (byte) (rad >0 ? 0x01 : 0x10);

		while (autoAct.ioio_thread_.echoDistanceCm < 50) {
			if (presentState < avoid)
				return; // 좀더 위기상황이 발생하면 루프탈출

			if (autoAct.ioio_thread_.echoDistanceCm < 100) {
				// 일단 핸들을 꺽는다.
				i = 0;
				//autoAct.controlBuf[0] = 0x10;
				//autoAct.controlBuf[1] = dir;
			} else {
				// 다음에 물체를 지나칠만한 시간이 필요하다.
				// i를 증가시켜주어 물체를 지나가게 한다.
				i++;
			}

			if (i > 150)
				break;

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}// end while
		presentState = move;
	}// end avoid

	public void DgreeMove() {
		//뒤로왼쪽 		 buff[0] = 0x10;buff[1] = 0x01;
		//뒤로오른쪽      buff[0] = 0x10; buff[1] = 0x10;
		//192.168.10.108
		//192.168.10.107
		presentState = dgree;
		int duty=0;
		
		while (Math.abs(rad) > 10) {
			if (presentState < dgree)
				return; // 좀더 위기상황이 발생하면 루프탈출
			// Log.d("state", "DgreeMove");
			if(rad < 0)
			{
				autoAct.controlBuf[0] = 0x01;
				autoAct.controlBuf[1] = 0x01;
			}
			else 
			{
				autoAct.controlBuf[0] = 0x01;
				autoAct.controlBuf[1] = 0x10;
			}
			
			
		}

		autoAct.controlBuf[0] = 0x00;
		autoAct.controlBuf[1] = 0x00;
		while (duty < 10) {
			if (presentState < dgree)
				return; // 좀더 위기상황이 발생하면 루프탈출
			try {
				Thread.sleep(50);

			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			duty++;
		}
		presentState=4;
		
		
		
		

		
	}
	public byte[] DoubletoByteArray(double dos) {
		String str = Double.toString(dos);
		ByteBuffer bb = ByteBuffer.allocate(Double.SIZE);
		bb.putDouble(dos);
		bb.order(ByteOrder.BIG_ENDIAN);
		return bb.array();

	}
	
	
	

	
	
}//end class

class InterruptThead extends Thread {
	/**
	 * 실시간으로 변하는 센서값들을 읽어서 도착이나 자동모드전환 물체피하기등의 돌발적인 상황에서 AutoThead를 제어하여 대체하게
	 * 도와주는 스레드입니다.
	 */
	boolean isRun;
	AutoThead auto;

	public InterruptThead(AutoThead auto) {
		this.auto = auto;
		isRun = false;
		auto.echoDistanceCm = 100;
	}
	public void init(){
		this.isRun=true;
		this.start();
	}
	public void stop_thread(){
		this.isRun=false;
	}

	public void run() {
		while (true) {
			if (isRun) {
				// 0순위 대기
				// if(auto.Car_lat ==0 || auto.Car_lon ==0 ){
				// auto.interruptStatae=auto.wait;
				// }

				// 1순위 물체피하기
				if (auto.autoAct.ioio_thread_.echoDistanceCm < 50
						&& auto.presentState > auto.avoid) {
					auto.presentState = auto.avoid;

				}

				// 3순위 각도
				if (Math.abs(auto.rad) > 30 && auto.presentState > auto.dgree) {
					auto.presentState = auto.dgree;
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Log.d("state",
				// "check "+auto.autoAct.ioio_thread_.echoDistanceCm);

			}// end isRun
		}// end while
	}// end run

}
