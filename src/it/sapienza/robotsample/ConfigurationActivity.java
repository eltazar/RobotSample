package it.sapienza.robotsample;

import java.net.InetAddress;
import java.net.UnknownHostException;

import netInterface.*;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigurationActivity extends BaseActivity implements OnClickListener{

	private static final int INVISIBLE = 1;
	private static final int VISIBLE = 0;
	private netInterface.MessageIOStream socketAndStream;
	private Button connectBtn;
	private Button disconnectBtn;
	private Button automaticBtn;
	private Button manualBtn;

	private ProtocolAdapter pAdapt;

	private TextView ipRobot;
	private TextView status;

	private EditText ipAddressEditText;
	private EditText portEditText;

	//private String mode;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//mode = "no mode";

		//recupero protocol adapter
		pAdapt = ProtocolAdapter.getInstance();
		System.out.println("PADATP = "+pAdapt);

		//recupero parametri extra passati dal chiamante
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			//mode = extras.getString("mode");
		}

		//System.out.println("ON CREATE =  "+mode);

		//in base allo stato della connessione carico l'opportuno layout
		if(pAdapt.isThereAvaiableStream()){

			setContentView(R.layout.disconnectconnection);
			disconnectBtn = (Button) findViewById(R.id.disconnectionBtn);
			disconnectBtn.setOnClickListener(this);
			status = (TextView) findViewById(R.id.status);
			ipRobot = (TextView) findViewById(R.id.ipRobot);

			status.setText("Connesso");
			ipRobot.setText(pAdapt.getAssociatedStream().getIpAddress());
		}
		else{
			setContentView(R.layout.choosetypeconnection);
			automaticBtn = (Button) findViewById(R.id.autoconnectBtn);
			automaticBtn.setOnClickListener(this);
			manualBtn = (Button) findViewById(R.id.manualConnection);
			manualBtn.setOnClickListener(this);
		}

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
			disconnectBtn.setOnClickListener(this);
			status.setText("Connesso");
			System.out.println("INDIRIZZO IP AL QUALE SONO CONNESSO"+pAdapt.getAssociatedStream().getIpAddress());
			ipRobot.setText(pAdapt.getAssociatedStream().getIpAddress());
		}
		else{
			setContentView(R.layout.choosetypeconnection);
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

	private void disconnectFromServer(){
		//TextView connectionStatus = (TextView)findViewById(R.id.connection_status); 
		try {
			//    		 //socketAndStream = pAdapt.getSocketStream();
			//    		 socketAndStream.getMis().closeInput();
			//             socketAndStream.getMos().closeOutput();
			//             socketAndStream.close();

			if(pAdapt.isThereAvaiableStream()){
				MessageIOStream tempSock = pAdapt.getAssociatedStream();
				tempSock.getMis().closeInput();
				tempSock.getMos().closeOutput();
				tempSock.close();

				System.out.println("Client: Connessione terminata");

				ipRobot.setText("");
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
				status.setText("Connesso");
				System.out.println("Client: Connessione stabilita");
				//associo al protocolAdapter un socket e stream
				pAdapt.setProtocolAdapter(socketAndStream);
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

	private void changeFont(TextView textview){
		Typeface font=Typeface.createFromAsset(getAssets(), "fonts/droidsans.ttf");
		textview.setTypeface(font);
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		//disabilita il relativo tasto del menu option
		menu.getItem(0).setEnabled(false);
		return true;
	}
}