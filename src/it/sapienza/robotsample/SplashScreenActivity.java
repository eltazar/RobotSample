package it.sapienza.robotsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import netInterface.NetworkScanner;;

public class SplashScreenActivity extends Activity implements OnClickListener{

	private ProgressBar bar;
	//private int mProgressStatus = 0;
	
	//gestore per i messaggi relativi alla progress bar
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			System.out.println("INCREMENTO PROGRESS BAR");
			//incremento la status bar col valore ritornato
			bar.incrementProgressBy(msg.what);
		}
	};
	//private Handler mHandler = new Handler();
	private NetworkScanner netScan;
 
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splashscreen);
		Button configBtn = (Button) findViewById(R.id.config_btn);
        configBtn.setOnClickListener(this);
        
		bar = (ProgressBar) findViewById(R.id.progress);
		//associo al networkScanner l'handler
		netScan = new NetworkScanner(handler,this);
	}
 
	public void onStart() {
		super.onStart();
		bar.setProgress(0);
		
		//faccio partire la ricerca su un nuovo thread
		new Thread(new Runnable(){
			public void run(){
				netScan.doScan();
			};
		}).start();
		
//		 new Thread(new Runnable() {
//             public void run() {
//                 for(int i = 1;i< 255;i++){
//                     mProgressStatus = netScan.scannerFasullo(i);
//
//                     // Update the progress bar
//                     mHandler.post(new Runnable() {
//                         public void run() {
//                             bar.setProgress(mProgressStatus);
//                         }
//                     });
//                 }
//             }
//         }).start();
	}
 
	

	@Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch ( v.getId() ) {
            case R.id.config_btn:
            	Intent configActivity = new Intent(this, RobotSampleActivity.class);
            	startActivity(configActivity);
            break;
        }
    }
}
