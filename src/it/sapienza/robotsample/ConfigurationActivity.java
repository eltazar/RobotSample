package it.sapienza.robotsample;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import netInterface.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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

public class ConfigurationActivity extends BaseActivity implements OnClickListener{

	private netInterface.MessageIOStream socketAndStream;
	private Button connectBtn;
	private Button disconnectBtn;
	private Button automaticBtn;
	private Button manualBtn;
	private Button rescanBtn;
	private Button backBtn;
	
	private ProtocolAdapter pAdapt;

	private TextView ipRobot;
	private TextView portRobot;
	private TextView status;

	private EditText ipAddressEditText;
	private EditText portEditText;
	
	private ArrayList<String> scannedIp;

	private ProgressBar bar;
	
	//gestore per i messaggi relativi alla progress bar
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			System.out.println("INCREMENTO PROGRESS BAR");
			//incremento la status bar col valore ritornato
			bar.incrementProgressBy(msg.what);
		}
	};
	//private String mode;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//recupero protocol adapter
		pAdapt = ProtocolAdapter.getInstance();
		//System.out.println("PADATP = "+pAdapt+"LISTA INDIRIZZI = "+NetworkUtility.getInstance().getIpAddresses());

		//recupero parametri extra passati dal chiamante
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			//mode = extras.getString("mode");
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
		super.onPause();
		System.out.println("ON RESUME configuration activity");

		if(ProtocolAdapter.getInstance().isThereAvaiableStream()){
			setContentView(R.layout.disconnectconnection);
			
			disconnectBtn = (Button) findViewById(R.id.disconnectionBtn);
			findViewReferences();
			disconnectBtn.setOnClickListener(this);
			
			status.setText("Connesso");
			//System.out.println("INDIRIZZO IP AL QUALE SONO CONNESSO"+pAdapt.getAssociatedStream().getIpAddress());
			ipRobot.setText("Ip: "+pAdapt.getAssociatedStream().getIpAddress());
			portRobot.setText("Porta: "+pAdapt.getAssociatedStream().getPort());
		}
		else{
			setContentView(R.layout.choosetypeconnection);
			automaticBtn = (Button) findViewById(R.id.autoconnectBtn);
			automaticBtn.setOnClickListener(this);
			manualBtn = (Button) findViewById(R.id.manualConnection);
			manualBtn.setOnClickListener(this);
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
			loadLayoutConnection("auto");
			if(pAdapt.isThereAvaiableStream() == false)
				networkScanning();
			break;	
		case R.id.manualConnection:
			loadLayoutConnection("manual");
			break;
		case R.id.connectionBtn:
			final EditText ipAddress = (EditText)findViewById(R.id.edit_ipAddress);
			final EditText port = (EditText)findViewById(R.id.edit_port);
			connectToServer(ipAddress.getText().toString(),port.getText().toString());
			break;
		case R.id.disconnectionBtn:
			disconnectFromServer();
			break;
		case R.id.rescan:
			networkScanning();
			break;
		case R.id.back:
			setContentView(R.layout.choosetypeconnection);
			automaticBtn = (Button) findViewById(R.id.autoconnectBtn);
			automaticBtn.setOnClickListener(this);
			manualBtn = (Button) findViewById(R.id.manualConnection);
			manualBtn.setOnClickListener(this);
			break;
		}
	}

	private void loadLayoutConnection(String type){
		
		if(type.equals("manual")){
			setContentView(R.layout.manualconnection);
			connectBtn = (Button) findViewById(R.id.connectionBtn);
			connectBtn.setOnClickListener(this);
			ipAddressEditText = (EditText)findViewById(R.id.edit_ipAddress); 
			ipAddressEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			portEditText = (EditText)findViewById(R.id.edit_port); 
			portEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		}
		else if(type.equals("auto")){
			setContentView(R.layout.automaticconnection);
			rescanBtn = (Button) findViewById(R.id.rescan);
			rescanBtn.setEnabled(false);
			rescanBtn.setOnClickListener(this);
			bar = (ProgressBar) findViewById(R.id.progressbarConnection);
		}
		
		//recupero view comuni a tutti i layout
		findViewReferences();
		backBtn = (Button) findViewById(R.id.back);
		backBtn.setOnClickListener(this);
		
		
		//controllo stato connessione e aggiorno textView
		if(pAdapt.isThereAvaiableStream()){
			status.setText("Connesso");
			ipRobot.setText("Ip: "+pAdapt.getAssociatedStream().getIpAddress());
			portRobot.setText("Porta: "+pAdapt.getAssociatedStream().getPort());
			if(rescanBtn != null)
				rescanBtn.setEnabled(false);
			if(connectBtn != null)
				connectBtn.setEnabled(false);
		}
		else{
			status.setText("Non connesso");
			ipRobot.setText("Ip: --");
			portRobot.setText("Porta: --");
			if(rescanBtn != null)
				rescanBtn.setEnabled(false);
			if(connectBtn != null)
				connectBtn.setEnabled(true);
		}
		
	}
	
	private void networkScanning(){
		
		NetworkUtility.getInstance().setContext(this);
		NetworkUtility.getInstance().setHandler(handler);
		
		new Thread(new Runnable(){
			public void run(){
				scannedIp = NetworkUtility.getInstance().doScan();
				autoConnect();				
			};
		}).start();
	}	
	
	/*
	 * SISTEMARE STA COSA: autoconnect è sia qui che in splashScreenActivity-> disaccopiare
	 * **/
	private void autoConnect(){
		System.out.println("#### Inizio prova autoconnesione...");
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
				//if(b==6){
				if(ack.substring(0, 10).equals("6RoborRack")){
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
			
			//lancio activity joystick appena connesso
			//startActivity(new Intent(this, InterfacciaRobotActivity.class));

			//oppure aggiorno interfaccia e mostro optionMenu ----> DA DECIDERE
			ConfigurationActivity.this.runOnUiThread(new Runnable() {
			    public void run() {
			    	ipRobot.setText("Ip: "+pAdapt.getAssociatedStream().getIpAddress());
					portRobot.setText("Porta: "+pAdapt.getAssociatedStream().getPort());
					status.setText("Connesso");
					rescanBtn.setEnabled(false);
					bar.setProgress(0);
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
					rescanBtn.setEnabled(true);
					bar.setProgress(0);
			    }
			});
		}
	}
	
	private void disconnectFromServer(){

		try {
			if(pAdapt.isThereAvaiableStream()){
				MessageIOStream tempSock = pAdapt.getAssociatedStream();
				tempSock.getMis().closeInput();
				tempSock.getMos().closeOutput();
				tempSock.close();

				System.out.println("Client: Connessione terminata");

				ipRobot.setText("Ip: --");
				portRobot.setText("Porta: --");
				status.setText("Disconnesso");
				//rimuovo lo stream associato durante la connessione
				pAdapt.setProtocolAdapter(null);
			}
		} catch (java.io.IOException e) {
			System.out.println("Disconnessione fallita: "+e.getLocalizedMessage());
		}
	}

	private void connectToServer(String ipAddress,String port){

		System.out.println("Cliccatto bottone connessione");
		int portNumber;

		try{
			portNumber = Integer.parseInt(port);

			try{
				//creo socket con stream in/out associati
				System.out.println("Client: Richiesta connessione con: " + ipAddress+"/"+port);
				socketAndStream = new MessageIOStream( InetAddress.getByName(ipAddress),portNumber,5000);
				System.out.println("Client: Connessione stabilita");
				//associo al protocolAdapter un socket e stream
				pAdapt.setProtocolAdapter(socketAndStream);
				
				status.setText("Connesso");
				ipRobot.setText("Ip: "+ pAdapt.getAssociatedStream().getIpAddress());
				portRobot.setText("Porta: "+pAdapt.getAssociatedStream().getPort());
				connectBtn.setEnabled(false);
				this.openOptionsMenu();
			}
			catch (UnknownHostException ex) {
				System.out.println("Client: Impossibile connettersi"+ex.getLocalizedMessage());
				status.setText("Impossibile connettersi");
				//System.exit(0);
			} catch (java.io.IOException ex) {
				System.out.println("Client: Impossibile connettersi"+ex.getLocalizedMessage());
				status.setText("Impossibile connettersi");
				//System.exit(0);
			}
		}
		catch(NumberFormatException ex){
			System.out.println("Client: Numero di porta errato:"+ex.getLocalizedMessage());
			status.setText("Numero porta errato");
		}

	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		//disabilita il relativo tasto del menu option
		menu.getItem(0).setEnabled(false);
		return true;
	}
}