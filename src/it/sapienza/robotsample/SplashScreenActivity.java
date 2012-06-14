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
	}
	
	@Override
	public void onStart() {
		super.onStart();
		bar.setProgress(0);

		//lancio ricerca su un nuovo thread
		new Thread(new Runnable(){
			public void run(){
				scannedIp = netScan.doScan();
				autoConnect();				
			};
		}).start();
	}

	/*
	 * Prova a connettersi in automatico con gli indirizzi ritornati dagli scanner alla ricerca 
	 * di quello appartenente al robot. Questo metodo gira DENTRO il trhead che ha fatto partire la rirca!!
	 * */
	private void autoConnect(){
		System.out.println("#### CERCO DI AUTOCONNETTERMI...");
		ProtocolAdapter pAdapt = ProtocolAdapter.getInstance();
		String ack = "";
		boolean isAutoconnected = false;

		for( String ip : scannedIp){
			System.out.println("Sto provando a connettermi a: = "+ip);
			
			MessageIOStream socket;
			
			try {
				socket = new MessageIOStream(InetAddress.getByName(ip),80,5000);
				pAdapt.setProtocolAdapter(socket);
				try {
					//mando messaggio al server per dire che sono l'app
					ack = pAdapt.sendMessage("#CNT0\r");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Ack ricevuto = "+ack+" Lunghezza = "+ack.length());

				//se ricevo ack corretto fermo ciclo
				System.out.println("Substring ack = "+ack.substring(1, ack.length()));
				
				//controllo che il primo carattere sia 6 (in ascii = "ack")
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
			//se autoconnessione avvenuta mostro interfaccia con joystick
			startActivity(new Intent(this, InterfacciaRobotActivity.class));

		}
		else{
			//mostro dialog
			startActivity(new Intent(this, ConfigurationActivity.class));
			//showDialog();
		}
	}

	/*
	 * crea e configura un dialog, questo metodo gira sul thread che ha lanciato la ricerca
	 * */
	private void showDialog(){

		//configuro dialog
		final CharSequence[] items = {"Esegui connessione manuale", "Riprova ricerca robot"};

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Non è stato possibile auto-connettersi al robot");
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
		
		//The method show() must be called from UI thread --> quindi recupero il thread della ui e faccio show
		SplashScreenActivity.this.runOnUiThread(new Runnable() {
		    public void run() {
		    	AlertDialog alert = builder.create();
				alert.show();
		    }
		});
	}
	
	private void launchConfig(String mode){
		Intent i= new Intent(this, ConfigurationActivity.class);
		i.putExtra("mode",mode);
		startActivity(i);
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
