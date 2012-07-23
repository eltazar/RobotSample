package it.sapienza.robotsample;

import java.util.ArrayList;

import netInterface.NetworkUtility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

@SuppressLint("HandlerLeak")
public class ReloadWebcam extends Activity {

	private WebcamHandler gui;
	private ProgressBar bar;
	
	private ImageView close2;
	private Button annulla;
	private ArrayList<String> scannedIp;
	private boolean gone;

	private FrameLayout frameLayout;

	// gestore per i messaggi relativi alla progress bar
	private Handler handler = new Handler() {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// incremento la status bar col valore ritornato
			bar.incrementProgressBy(msg.what);
		}
	};

	public ReloadWebcam(FrameLayout layout, WebcamHandler baseActivity) {

		frameLayout = layout;
		
		close2 = (ImageView)frameLayout.findViewById(R.id.close);
		annulla = (Button)frameLayout.findViewById(R.id.annulla);

		bar = (ProgressBar) frameLayout.findViewById(R.id.progress);
		bar.setProgress(0);

		//mostra il layout con la barra di ricerca webcam
		gui = baseActivity;
		gui.restoreLayout(false);
		
		gone = false;

		networkScanning();
	}

	private void networkScanning() {
		
		close2.setOnClickListener(new ImageView.OnClickListener(){

			public void onClick(View arg0) {
				closeAll();
			}
		});
		
		annulla.setOnClickListener(new ImageView.OnClickListener(){

			public void onClick(View arg0) {
				closeAll();
			}
		});

		NetworkUtility.getInstance().setHandler(handler);

		new Thread(new Runnable() {
			public void run() {
				scannedIp = NetworkUtility.getInstance().doScan();
				refreshDone();
			}
		}).start();
	}

	private void refreshDone() {
		
		if(gone){			
			gone = false;
			return;
		}
		
		//una volta finita la ricerca, carica il precedente layout
		this.runOnUiThread(new Runnable() {
			public void run() {
				frameLayout.setVisibility(View.GONE);
				gui.reSetIps(scannedIp);
				gui.restoreLayout(true);
			}
		});
	}
	
	private void closeAll() {
		
		frameLayout.setVisibility(View.GONE);
		gone = true;
		
		gui.getIRB().setSelectedIp(null);
		return;
		
	}
}
