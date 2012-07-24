package it.sapienza.robotsample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
	final static String SD_PATH = Environment.getExternalStorageDirectory().toString()+"/";
	
	//private int mProgressStatus = 0;

	//gestore per i messaggi relativi alla progress bar
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//System.out.println("INCREMENTO PROGRESS BAR");
			//incremento la status bar col valore ritornato
			bar.incrementProgressBy(msg.what);
		}
	};
	//private Handler mHandler = new Handler();
	private NetworkUtility netScan;
	private ProgressDialog progDailog;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splashscreen);

		SharedPreferences mPrefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
		
		if(!mPrefs.getBoolean("has_started_before", false)) {
            // Do what ever you want to do the first time the app is run
        	System.out.println("APP LANCIATA PRIMA VOLTA");
        	//copio i file necessari allo speech recognition dalla cartella Assets alla sd card
        	new Thread(new Runnable(){
    			public void run(){
    				System.out.println("THREAD per installazione file su SDCARD");
    	        	copyFileOrDir("edu.cmu.pocketsphinx");
    	        	renameFile("mdef.mp3","mdef",SD_PATH+"edu.cmu.pocketsphinx/hmm/en_US/hub4wsj_sc_8k/");
    	        	renameFile("sendump.mp3","sendump",SD_PATH+"edu.cmu.pocketsphinx/hmm/en_US/hub4wsj_sc_8k/");
    	        	renameFile("wsj0vp.5000.DMP.mp3","wsj0vp.5000.DMP",SD_PATH+"edu.cmu.pocketsphinx/lm/en_US/");
    	        	getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("has_started_before", true).commit();		
    			};
    		}).start();
        }
		//Remember our choice for next time
		//mPrefs.edit().putBoolean("has_started_before", false).commit();

		System.out.println("NON è PRIMA VOLTA APP");

		bar = (ProgressBar) findViewById(R.id.progress);
		//associo al networkScanner l'handler
		netScan = NetworkUtility.getInstance();
       
	}
	
	@Override
	public void onStart() {
		super.onStart();
		bar.setProgress(0);

		netScan.setContext(this);
		netScan.setHandler(handler);
		//lancio ricerca su un nuovo thread
		new Thread(new Runnable(){
			public void run(){
				scannedIp = netScan.doScan();
				autoConnect();				
			};
		}).start();
		//startActivity(new Intent(this, InterfacciaRobotActivity.class));
	}
	
	/*
	 * Copia file o dir dalla directory assets nella SDCARD
	 * */
	private void copyFileOrDir(String path) {
	    AssetManager assetManager = this.getAssets();
	    String assets[] = null;
	    try {
	        Log.i("tag", "copyFileOrDir() "+path);
	        assets = assetManager.list(path);
	        if (assets.length == 0) {
	            copyFile(path);
	        } else {
	            String fullPath =  SD_PATH + path;
	            Log.i("tag", "path="+fullPath);
	            File dir = new File(fullPath);
	            if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
	                if (!dir.mkdirs());
	                    Log.i("tag", "could not create dir "+fullPath);
	            for (int i = 0; i < assets.length; ++i) {
	                String p;
	                if (path.equals(""))
	                    p = "";
	                else 
	                    p = path + "/";

	                if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
	                    copyFileOrDir( p + assets[i]);
	            }
	        }
	    } catch (IOException ex) {
	        Log.e("tag", "I/O Exception", ex);
	    }
	}

	private void copyFile(String filename) {
	    AssetManager assetManager = this.getAssets();

	    InputStream in = null;
	    OutputStream out = null;
	    String newFileName = null;
	    try {
	        Log.i("tag", "copyFile() "+filename);
	        in = assetManager.open(filename);
	        if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
	            newFileName = SD_PATH + filename.substring(0, filename.length()-4);
	        else
	            newFileName = SD_PATH + filename;
	        out = new FileOutputStream(newFileName);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        Log.e("tag", "Exception in copyFile() of "+newFileName + "perchè: "+e.getCause());
	        Log.e("tag", "Exception in copyFile() "+e.toString());
	    }

	}

	private void renameFile(String oldName, String newName, String path){

		//punta a alla dir opportuna
		File dir = new File(path);

		File from = new File(dir,oldName);
		File to = new File(dir,newName);
		if(from.exists())
			from.renameTo(to);
	}
	
	/*
	 * Prova a connettersi in automatico con gli indirizzi ritornati dagli scanner alla ricerca 
	 * di quello appartenente al robot. Questo metodo gira DENTRO il thread che ha fatto partire la ricerca!!
	 * */
	private void autoConnect(){
		System.out.println("#### CERCO DI AUTOCONNETTERMI...");
		ProtocolAdapter pAdapt = ProtocolAdapter.getInstance();
		String ack = "";
		boolean isAutoconnected = false;

		for( String ip : scannedIp){
			System.out.println("Sto provando a connettermi a: = "+ip);
			
			MessageIOStream socket;
	
			//TODO: controllare che ack abbia lunghezza >1 per nn farlo crashare durante la stampa di debug
			
			try {
				socket = new MessageIOStream(InetAddress.getByName(ip),80,2500);
				pAdapt.setProtocolAdapter(socket);

				//mando messaggio al server per dire che sono l'app
				ack = pAdapt.sendMessage("#CNT0\r");

				System.out.println("Splash screen -> ack ricevuto = "+ack+" --> Lunghezza = "+ack.length());

				//se ricevo ack corretto fermo ciclo
				//System.out.println("Substring ack = "+ack.substring(1, ack.length()));
				
				//controllo che il primo carattere sia 6 (in ascii = "ack")
				if(ack != null && ack.length() != 0 && (byte)ack.charAt(0) == 6){
					//byte b = (byte)ack.charAt(0);
					//System.out.println("BYTE = "+b);
					//if(b==6){
					//if(ack.substring(0, 10).equals("6RoborRack")){
					System.out.println("AUTOCONNESSIONE RIUSCITA");
					isAutoconnected = true;
					break;
					//}	
				}
				else{
					//altrimenti chiudo socket e risorse associate
					pAdapt.closeCommunication();
					pAdapt.setProtocolAdapter(null);
				}

			} catch (UnknownHostException e) {
				System.out.println("SplashScreen--> Unkonown Host Exception = "+e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("SplashScreen--> IOException = "+e.getLocalizedMessage());
				e.printStackTrace();
			}
			
		}

		if(isAutoconnected == true){
			//se autoconnessione avvenuta mostro interfaccia con joystick
			//startActivity(new Intent(this, InterfacciaRobotActivity.class));
			Intent intent = new Intent(this, ConfigurationActivity.class);
			intent.putExtra("toast","auto");
			startActivity(intent);

		}
		else{
			//mostro dialog
			//startActivity(new Intent(this, ConfigurationActivity.class));
			//showDialog();
			Intent intent = new Intent(this, ConfigurationActivity.class);
			intent.putExtra("toast","noAuto");
			startActivity(intent);
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
