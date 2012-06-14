package it.sapienza.robotsample;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import netInterface.NetworkUtility;
import it.sapienza.robotsample.ProtocolAdapter;
import netInterface.MessageIOStream;

public class SplashScreenActivity extends Activity implements OnClickListener{

	private ProgressBar bar;
	private ArrayList<String> scannedIp;
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
	private NetworkUtility netScan;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splashscreen);

		bar = (ProgressBar) findViewById(R.id.progress);
		//associo al networkScanner l'handler
		netScan = new NetworkUtility(handler,this);
		
		final CharSequence[] items = {"Esegui connessione manuale", "Riprova autoconnessione"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Non � stato possibile auto-connettersi al robot");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				if(item == 0){
					launchConfig("manual");
				}
				else{
					launchConfig("auto");
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void onStart() {
		super.onStart();
		bar.setProgress(0);

		//faccio partire la ricerca su un nuovo thread
		/*new Thread(new Runnable(){
			public void run(){
				scannedIp = netScan.doScan();
				autoConnect();				
			};
		}).start();*/

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

	/*
	 * Prova a connettersi in automatico con gli indirizzi ritornati dagli scanner alla ricerca 
	 * di quello appartenente al robot
	 * */
	private void autoConnect(){
		System.out.println("#### PARTITA AUTOCONNESSIONE");
		ProtocolAdapter pAdapt = ProtocolAdapter.getInstance();
		String ack = "";
		boolean isAutoconnected = false;

		for( String ip : scannedIp){
			System.out.println("Indirizzo ip prova di connessione = "+ip);
			MessageIOStream socket;
			try {
				socket = new MessageIOStream(InetAddress.getByName(ip),80,5000);
				pAdapt.setProtocolAdapter(socket);
				try {
					ack = pAdapt.sendMessage("#CNT0\r");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Ack ricevutooo = "+ack);
				System.out.println(ack.length());
				//se ricevo ack corretto fermo ciclo
				System.out.println(ack.substring(1, ack.length()));
				byte b = (byte)ack.charAt(0);
				if(b==6){
					System.out.println("AUTOCONNESSIONE RIUSCITA");
					isAutoconnected = true;
					break;
				}
				//altrimenti chiudo socket e risorse associate
				socket.getMis().closeInput();
				socket.getMos().closeOutput();
				socket.close();
				pAdapt.setProtocolAdapter(null);				

			} catch (UnknownHostException e) {
				System.out.println(" Eccezione = "+e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println(" Eccezione = "+e.getLocalizedMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(isAutoconnected == true){
			//se autoconnessione avvenuta mostrare interfaccia con joystick
			// startActivity(new Intent(this, InterfacciaRobotActivity.class));

//			final CharSequence[] items = {"Esegui connessione manuale", "Riprova autoconnessione"};
//
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle("Non � stato possibile auto-connettersi al robot");
//			builder.setItems(items, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int item) {
//					//Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
//					if(item == 0){
//						launchConfig("manual");
//					}
//					else{
//						launchConfig("auto");
//					}
//				}
//			});
//			AlertDialog alert = builder.create();
//			alert.show();
		}
		else{
			//mostrare interfaccia configurazione
			startActivity(new Intent(this, ConfigurationActivity.class));
		}
	}

	private void launchConfig(String mode){
		if(mode.equals("manual")){
			startActivity(new Intent(this, ConfigurationActivity.class));
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch ( v.getId() ) {
		default:
			break;
		}
	}
}
