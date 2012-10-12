package ac.as;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class StartRecv extends Activity {
	CamView cv;
	Thread tr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String ip = intent.getStringExtra("ip");
		cv = new CamView(this, ip);

		setContentView(cv);


		Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				Log.d("hand", "recv handler");
				//Toast.makeText(StartRecv.this, "받음", Toast.LENGTH_SHORT).show();

				cv.invalidate();
			}

		};
		// mHandler.sendEmptyMessage(0);

		tr = new cam_thread_UDP(cv, mHandler);
		tr.start();

	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		tr.destroy();
		//cv.tr.destroy();

		super.onDestroy();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//tr.stop();
		//cv.tr.stop();
		cv.tr.interrupt();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, Menu.FIRST, 0, "자동주행");
		menu.add(0, 2, 0, "종료");
		// menu.add(0,Menu.,1, "자동주행");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent intent;
		switch(item.getItemId()){
			case Menu.FIRST:
				intent = new Intent(StartRecv.this, Map.class);
				//dffi
				//cv.tr.stop();
				//cv.tr.destroy();

				//cv.tr.stop();
				//
				//onStop();
			    startActivity(intent);
			   // onDestroy();
			    //cv.tr.destroy();
			    break;
			 default:
				 onDestroy();
				 break;
		}





		return super.onOptionsItemSelected(item);
	}
}
