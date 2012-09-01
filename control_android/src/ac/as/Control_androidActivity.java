package ac.as;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Control_androidActivity extends Activity
{
    /** Called when the activity is first created. */
    EditText ip;
    
    Button start;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ip = (EditText) findViewById(R.id.ip);
        start = (Button) findViewById(R.id.Start);
        
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
        
    }
}