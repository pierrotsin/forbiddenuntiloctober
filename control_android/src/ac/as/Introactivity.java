package ac.as;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Introactivity extends Activity
{
    
    Handler h;
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.introactvity);
        
        h = new Handler();
        h.postDelayed(irun, 3300);
        
    }
    
  
    
    Runnable irun = new Runnable() {
        public void run()
        {
            Intent i = new Intent(Introactivity.this, Control_androidActivity.class);// introactivtiy
            // --> main
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        h.removeCallbacks(irun);
    }
}