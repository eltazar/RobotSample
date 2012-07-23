package it.sapienza.robotsample;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import netInterface.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ConfigurationActivity extends BaseActivity implements OnClickListener{

	private netInterface.MessageIOStream socketAndStream;
	private Button connectBtn;
	private Button disconnectBtn;
	private Button automaticBtn;
	private Button manualBtn;
	private Button rescanBtn;
	private Button backBtn;
	private final int activity_index = 3;
	private ProtocolAdapter pAdapt;
	private static boolean isAutoconnection = false;

	private TextView ipRobot;
	private TextView portRobot;
	private TextView status;

	private EditText ipAddressEditText;
	private EditText portEditText;
		
	private ArrayList<String> scannedIp;

	private ProgressBar bar;
	
	private ProgressDialog dialog;
	
	//gestore per i messaggi relativi alla progress bar
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			Bundle bundle = msg.getData();
						
			if(bundle.containsKey("ConnectionStatus")){
				String text = bundle.getString("ConnectionStatus");
				if(text.equals("connecting")){
					dialog = ProgressDialog.show(ConfigurationActivity.this, "", "Connessione  in corso...");
				}
				else if(text.equals("connected")){
					connectBtn.setEnabled(false);
					dialog.dismiss();
					loadLayoutConnection(Layouts.DISCONNECT);
					ConfigurationActivity.this.openOptionsMenu();
				}
				else if(text.equals("failed")){
					status.setText("Impossibile connettersi");
					status.setTextColor(Color.RED);
					dialog.dismiss();
				}
			}
			else{
				//System.out.println("INCREMENTO PROGRESS BAR");
				//incremento la status bar col valore ritornato
				bar.incrementProgressBy(msg.what);
			}
		}
	};
	//private String mode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//recupero protocol adapter
		pAdapt = ProtocolAdapter.getInstance();

		//recupero parametri extra passati dal chiamante
		Bundle extras = getIntent().getExtras();
		if(extras != null && extras.getString("toast").equals("noAuto")) {
			//mode = extras.getString("mode");
			Toast.makeText(getApplicationContext(), "Autoconnessione non riuscita, riprova od esegui una connessione manuale", Toast.LENGTH_LONG).show();
		}
		
		System.out.println("ON CREATE confituration activity");
		
	}

	@Override
	public void onPause(){
		super.onPause();
		System.out.println("ON PAUSE configuration activity");
	}

	@Override
	public void onResume(){
		super.onResume();
		System.out.println("ON RESUME configuration activity");

		if(pAdapt.isThereAvaiableStream()){
			loadLayoutConnection(Layouts.DISCONNECT);
		}
		else{
			loadLayoutConnection(Layouts.CHOOSE);
		}
	}

	private void findViewReferences(){
		status = (TextView) findViewById(R.id.status);
		ipRobot = (TextView) findViewById(R.id.ipRobot);
		portRobot = (TextView) findViewById(R.id.portRobot);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch ( v.getId() ) {
		case R.id.autoconnectBtn:
			loadLayoutConnection(Layouts.AUTO);
			if(pAdapt.isThereAvaiableStream() == false)
				networkScanning();
			break;	
		case R.id.manualConnection:
			loadLayoutConnection(Layouts.MANUAL);
			break;
		case R.id.connectionBtn:
			final EditText ipAddress = (EditText)findViewById(R.id.edit_ipAddress);
			final EditText port = (EditText)findViewById(R.id.edit_port);
			
			Thread thread = new Thread()
			{
			    @Override
			    public void run() {
					connectToServer(ipAddress.getText().toString(),port.getText().toString());
			    }
			};
			thread.start();
			break;
		case R.id.disconnectionBtn:
			disconnectFromServer();
			break;
		case R.id.rescan:
			if(pAdapt.getAssociatedStream() == null || 
				(pAdapt.getAssociatedStream() != null && pAdapt.getAssociatedStream().isConnected() == false)){
				networkScanning();
				rescanBtn.setEnabled(false);
			}
			else{
				Context context = getApplicationContext();
				CharSequence text = "Sei giˆ connesso al robot!";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
				
			break;
		case R.id.back:
			loadLayoutConnection(Layouts.CHOOSE);
			//setContentView(R.layout.choosetypeconnection);
		//	automaticBtn = (Button) findViewById(R.id.autoconnectBtn);
			//automaticBtn.setOnClickListener(this);
			//manualBtn = (Button) findViewById(R.id.manualConnection);
			//manualBtn.setOnClickListener(this);
			break;
		}
	}

	private void loadLayoutConnection(Layouts value){
		
		switch(value){
			case CHOOSE:
				setContentView(R.layout.choosetypeconnection);
				isAutoconnection = false;
				automaticBtn = (Button) findViewById(R.id.autoconnectBtn);
				automaticBtn.setOnClickListener(this);
				manualBtn = (Button) findViewById(R.id.manualConnection);
				manualBtn.setOnClickListener(this);
				break;
			case DISCONNECT:
				setContentView(R.layout.disconnectconnection);
				isAutoconnection = false;
				disconnectBtn = (Button) findViewById(R.id.disconnectionBtn);
				disconnectBtn.setOnClickListener(this);
				break;
			case MANUAL:
				setContentView(R.layout.manualconnection);
				isAutoconnection = false;
				connectBtn = (Button) findViewById(R.id.connectionBtn);
				connectBtn.setOnClickListener(this);
				ipAddressEditText = (EditText)findViewById(R.id.edit_ipAddress); 
				ipAddressEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				portEditText = (EditText)findViewById(R.id.edit_port); 
				portEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				backBtn = (Button) findViewById(R.id.back);
				backBtn.setOnClickListener(this);
				break;
			case AUTO:
				setContentView(R.layout.automaticconnection);
				isAutoconnection = true;
				rescanBtn = (Button) findViewById(R.id.rescan);
				rescanBtn.setEnabled(false);
				rescanBtn.setOnClickListener(this);
				bar = (ProgressBar) findViewById(R.id.progressbarConnection);
				backBtn = (Button) findViewById(R.id.back);
				backBtn.setOnClickListener(this);
				break;
		}
		
		//recupero view comuni a tutti i layout
		findViewReferences();
		
		
		//controllo stato connessione e aggiorno textView
		if(pAdapt.isThereAvaiableStream()){
			status.setText("Connesso");
			status.setTextColor(Color.GREEN);
			ipRobot.setText("Ip: "+pAdapt.getAssociatedStream().getIpAddress());
			portRobot.setText("Porta: "+pAdapt.getAssociatedStream().getPort());
			if(rescanBtn != null)
				rescanBtn.setEnabled(false);
			if(connectBtn != null)
				connectBtn.setEnabled(false);
		}
		else{
			status.setText("Non connesso");
			status.setTextColor(Color.RED);
			ipRobot.setText("Ip: --");
			portRobot.setText("Porta: --");
			if(rescanBtn != null)
				rescanBtn.setEnabled(true);
			if(connectBtn != null)
				connectBtn.setEnabled(true);
		}
		
	}
	
	private void networkScanning(){
		
		NetworkUtility.getInstance().setContext(this);
		NetworkUtility.getInstance().setHandler(handler);
		
		TextView searchTxt = (TextView) findViewById(R.id.searchTxt);
		searchTxt.setVisibility(View.VISIBLE);
		new Thread(new Runnable(){
			public void run(){
				
				String hack = ((TextView)findViewById(R.id.hack)).getText().toString();
				scannedIp = NetworkUtility.getInstance().doScan();
				System.out.println("Thread ricerca: hack -> "+hack);
				if(isAutoconnection)
					autoConnect();				
			};
		}).start();
	}	
	
	/*
	 * SISTEMARE STA COSA: autoconnect Ã¨ sia qui che in splashScreenActivity-> disaccopiare
	 * **/
	private void autoConnect(){
		System.out.println("#### Inizio prova autoconnesione...");
		String ack = "";
		boolean isAutoconnected = false;
		
		ConfigurationActivity.this.runOnUiThread(new Runnable() {
		    public void run() {
				try{
					((TextView)findViewById(R.id.searchTxt)).setVisibility(View.VISIBLE);
				}
				catch(NullPointerException e){
					System.out.println("Configuration activity nullpointer -> "+e.getLocalizedMessage());
				}
		    }
		});		
		
		for( String ip : scannedIp){
			System.out.println("Sto provando a connettermi a: = "+ip);
			
			MessageIOStream socket;
			
			try {
				socket = new MessageIOStream(InetAddress.getByName(ip),80,2500);
				pAdapt.setProtocolAdapter(socket);
				//mando messaggio al server per dire che sono l'app
				ack = pAdapt.sendMessage("#CNT0\r");
				System.out.println("ConfigActivity -> Ack ricevuto = "+ack+" -> Lunghezza = "+ack.length());

				//se ricevo ack corretto fermo ciclo
				//System.out.println("Substring ack = "+ack.substring(1, ack.length()));
				
				//controllo che il primo carattere sia 6 (in ascii = "ack")
				//if(ack != null && ack.length() != 0 && (byte)ack.charAt(0) == 6){
					//byte b = (byte)ack.charAt(0);
					//System.out.println("BYTE = "+b);
					//if(b==6){
					if(ack.substring(0, 10).equals("6RoborRack")){
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
			
			//lancio activity joystick appena connesso
			//startActivity(new Intent(this, InterfacciaRobotActivity.class));

			//oppure aggiorno interfaccia e mostro optionMenu ----> DA DECIDERE
			ConfigurationActivity.this.runOnUiThread(new Runnable() {
			    public void run() {
			    	//ipRobot.setText("Ip: "+pAdapt.getAssociatedStream().getIpAddress());
					//portRobot.setText("Porta: "+pAdapt.getAssociatedStream().getPort());
					//status.setText("Connesso");
					rescanBtn.setEnabled(false);
					//((TextView)findViewById(R.id.searchTxt)).setVisibility(View.INVISIBLE);
					bar.setProgress(0);
//					setContentView(R.layout.disconnectconnection);
//					openOptionsMenu();
					loadLayoutConnection(Layouts.DISCONNECT);
					openOptionsMenu();
			    }
			});
			
		}
		else{
			System.out.println("Autoconnessione non riuscita -> aggiorno activity");
			ConfigurationActivity.this.runOnUiThread(new Runnable() {
			    public void run() {
			    	ipRobot.setText("Ip: --");
					portRobot.setText("Porta: --");
					status.setText("Impossibile connettersi");
					status.setTextColor(Color.RED);
					rescanBtn.setEnabled(true);
					((TextView)findViewById(R.id.searchTxt)).setVisibility(View.INVISIBLE);
					bar.setProgress(0);
			    }
			});
		}
	}
	
	private void disconnectFromServer(){

		try {
			if(pAdapt.isThereAvaiableStream()){
				
				pAdapt.closeCommunication();
				pAdapt.setProtocolAdapter(null);
				loadLayoutConnection(Layouts.CHOOSE);
				System.out.println("Client: Connessione terminata");
			}
		} catch (java.io.IOException e) {
			System.out.println("Disconnessione fallita: "+e.getLocalizedMessage());
		}
	}

	private void connectToServer(String ipAddress,String port){

		System.out.println("Cliccatto bottone connessione");
		int portNumber;
		boolean connected = false;
        
        Message msg = handler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putString("ConnectionStatus", "connecting");
		msg.setData(bundle);
		handler.sendMessage(msg);

		try{
			//creo socket con stream in/out associati
			portNumber = Integer.parseInt(port);
			System.out.println("Client: Richiesta connessione con: " + ipAddress+"/"+port);
			
			socketAndStream = new MessageIOStream( InetAddress.getByName(ipAddress),portNumber,2500);
			System.out.println("Client: Connessione stabilita");
			//associo al protocolAdapter un socket e stream
			pAdapt.setProtocolAdapter(socketAndStream);
			connected = true;
		}
		catch (UnknownHostException ex) {
			System.out.println("Client: Impossibile connettersi"+ex.getLocalizedMessage());
			connected = false;
		} catch (java.io.IOException ex) {
			System.out.println("Client: Impossibile connettersi"+ex.getLocalizedMessage());
			connected = false;
		}
		catch(NumberFormatException ex){
			System.out.println("Client: Numero di porta errato:"+ex.getLocalizedMessage());
			connected = false;
		}

		bundle.clear();
		
		if(connected){
			bundle.putString("ConnectionStatus", "connected");
		}
		else{
			bundle.putString("ConnectionStatus", "failed");
		}
	
		msg = handler.obtainMessage();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		//disabilita il relativo tasto del menu option
		menu.getItem(activity_index).setEnabled(false);
		return true;
	}
	
	private enum Layouts {
		AUTO("automaticconnection"), MANUAL("manualconnection"), CHOOSE("choosetypeconnection"), DISCONNECT("disconnectconnection");
		private String layout;

		private Layouts(String layout) {
			this.layout = layout;
		}
		
		public String getLayout(){
			return layout;
		}
	};  

	
}