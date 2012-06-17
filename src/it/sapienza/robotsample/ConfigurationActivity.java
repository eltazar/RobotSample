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

		//mode = "no mode";

		//recupero protocol adapter
		pAdapt = ProtocolAdapter.getInstance();
		System.out.println("PADATP = "+pAdapt+"LISTA INDIRIZZI = "+NetworkUtility.getInstance().getIpAddresses());

		//recupero parametri extra passati dal chiamante
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			//mode = extras.getString("mode");
		}

		//System.out.println("ON CREATE =  "+mode);
		
		/*
		//in base allo stato della connessione carico l'opportuno layout
		if(pAdapt.isThereAvaiableStream()){

			setContentView(R.layout.disconnectconnection);
			disconnectBtn = (Button) findViewById(R.id.disconnectionBtn);
			disconnectBtn.setOnClickListener(this);
			status = (TextView) findViewById(R.id.status);
			ipRobot = (TextView) findViewById(R.id.ipRobot);

			status.setText("Connesso");
			ipRobot.setText("Ip :"+pAdapt.getAssociatedStream().getIpAddress());
		}
		else{
			setContentView(R.layout.choosetypeconnection);
			automaticBtn = (Button) findViewById(R.id.autoconnectBtn);
			automaticBtn.setOnClickListener(this);
			manualBtn = (Button) findViewById(R.id.manualConnection);
			manualBtn.setOnClickListener(this);
		}
		
		*/
		
		//NON NECESSARIO SE CONTROLLO STATO CONNESSIONE COME SOPRA
		//se ho salvato uno stato precedente, in cui la connessione era stata stabilita setto i tasti
		/* if( savedInstanceState != null ) {
        	System.out.println("Recupero stato salvato");
        	if(savedInstanceState .getString("connStatus").equals("Connesso")){
        		connectBtn.setClickable(false);
        		disconnectBtn.setClickable(true);
        	}
        	//setto la label con lo status giusto
        	connectionStatus.setText(savedInstanceState .getString("connStatus"));
        }
		 */
	}

	@Override
	public void onPause(){
		super.onPause();
		System.out.println("ON PAUSE");
	}

	@Override
	public void onResume(){
		super.onPause();
		System.out.println("ON RESUME");

		if(ProtocolAdapter.getInstance().isThereAvaiableStream()){
			setContentView(R.layout.disconnectconnection);
			
			disconnectBtn = (Button) findViewById(R.id.disconnectionBtn);
			status = (TextView) findViewById(R.id.status);
			ipRobot = (TextView) findViewById(R.id.ipRobot);
			portRobot = (TextView) findViewById(R.id.portRobot);
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

	/*
    @Override
    protected void onSaveInstanceState (Bundle outState){
    	super.onSaveInstanceState(outState);
    	System.out.println("SALVO STATO");
    	outState.putString("connStatus",connectionStatus.getText().toString());
    }*/

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch ( v.getId() ) {
		case R.id.autoconnectBtn:
			setContentView(R.layout.automaticconnection);
			status = (TextView) findViewById(R.id.status);
			ipRobot = (TextView) findViewById(R.id.ipRobot);
			portRobot = (TextView) findViewById(R.id.portRobot);
			bar = (ProgressBar) findViewById(R.id.progressbarConnection);
			networkScanning();
			//startActivity(new Intent(this, SplashScreenActivity.class));
			break;
		case R.id.manualConnection:
			setContentView(R.layout.manualconnection);
			connectBtn = (Button) findViewById(R.id.connectionBtn);
			connectBtn.setOnClickListener(this);
			ipAddressEditText = (EditText)findViewById(R.id.edit_ipAddress); 
			ipAddressEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			portEditText = (EditText)findViewById(R.id.edit_port); 
			portEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			status = (TextView) findViewById(R.id.status);
			ipRobot = (TextView) findViewById(R.id.ipRobot);
			portRobot = (TextView) findViewById(R.id.portRobot);
			break;
		case R.id.connectionBtn:
			final EditText ipAddress = (EditText)findViewById(R.id.edit_ipAddress);
			final EditText port = (EditText)findViewById(R.id.edit_port);
			connectToServer(ipAddress.getText().toString(),port.getText().toString());
			break;
		case R.id.disconnectionBtn:
			disconnectFromServer();
			break;
			/*case R.id.scan_button:
            	//MessageIOStream.checkHosts("192.168.0");
            	System.out.println("STO PER AVVIARE IP SCAN");
            	//MessageIOStream.checkReachable();
            	NetworkUtility netScan = new NetworkUtility(this);
            	//netScan.getInfoWifiConnection();
            	netScan.doScan();
            break;
			 */
		}
	}

	private void networkScanning(){
		
		NetworkUtility.getInstance().setContext(this);
		NetworkUtility.getInstance().setHandler(handler);
		
		new Thread(new Runnable(){
			public void run(){
				scannedIp = NetworkUtility.getInstance().doScan();
				autoConnect(ipRobot,portRobot,status);				
			};
		}).start();
	}	
	
	/*
	 * SISTEMARE STA COSA: autoconnect è sia qui che in splashScreenActivity-> disaccopiare
	 * **/
	private void autoConnect(TextView...textViews){
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
			startActivity(new Intent(this, InterfacciaRobotActivity.class));

		}
		else{
			System.out.println("Autoconnessione non riuscita -> aggiorno activity");
			ConfigurationActivity.this.runOnUiThread(new Runnable() {
			    public void run() {
			    	refreshActivity();
			    }
			});
		}
	}
	
	private void refreshActivity(){
		
		ipRobot.setText("Ip: --");
		portRobot.setText("Porta: --");
		status.setText("Impossibile connettersi");
		bar.setProgress(0);
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

		//TextView connectionStatus = (TextView)findViewById(R.id.connection_status); 

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