package it.sapienza.robotsample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import netInterface.NetworkScanner;;

public class SplashScreenActivity extends Activity {

	private ProgressBar bar;
	//private int mProgressStatus = 0;
	
	//gestore per i messaggi relativi alla status bar
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
		bar = (ProgressBar) findViewById(R.id.progress);
		//associo al networkScanner l'handler
		netScan = new NetworkScanner(handler);
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
 
}
