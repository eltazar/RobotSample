package it.sapienza.robotsample;

import java.net.InetAddress;
import java.net.UnknownHostException;

import netInterface.*;
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
	private ProtocolAdapter pAdapt;
	private TextView connectionStatus; 
	private Button autoconnectBtn;
	private TextView ipAddress;
	private TextView portNumber;
	private EditText ipAddressEditText;
	private EditText portEditText;
	private String mode;
	private Button automaticBtn;
	private Button manualBtn;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        mode = "no mode";
        
        //recupero protocol adapter
        pAdapt = ProtocolAdapter.getInstance();
        System.out.println("PADATP = "+pAdapt);
        
        //recupero parametri extra passati dal chiamante
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
        	mode = extras.getString("mode");
        }
        
        System.out.println("ON CREATE =  "+mode);
        
        //decido quale xml caricare in base a pulsante del dialog premuto
        if(mode.equals("auto")){
        	setContentView(R.layout.automaticconnection);
        }
        else if(mode.equals("manual")){
        	setContentView(R.layout.configactivity);
        	connectBtn = (Button) findViewById(R.id.connectionBtn);
            connectBtn.setOnClickListener(this);
            ipAddressEditText = (EditText)findViewById(R.id.edit_ipAddress); 
            ipAddressEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            portEditText = (EditText)findViewById(R.id.edit_port); 
            portEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        else{
        	//se non dipende dal dialogo allora dipende dallo stato della connessione
        	if(pAdapt.isThereAvaiableStream()){

        		setContentView(R.layout.disconnectconnection);
        		disconnectBtn = (Button) findViewById(R.id.disconnectionBtn);
        		disconnectBtn.setOnClickListener(this);
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
        setContentView(R.layout.configactivity);
        connectBtn = (Button) findViewById(R.id.connect_button);
        connectBtn.setOnClickListener(this);
        
        disconnectBtn = (Button) findViewById(R.id.disconnect_button);
        disconnectBtn.setOnClickListener(this);*/
        //disconnectBtn.setClickable(false);
        
        autoconnectBtn = null;//(Button) findViewById(R.id.autoconnectBtn);
        //scanBtn = (Button) findViewById(R.id.scan_button);
        //scanBtn.setOnClickListener(this);
        
        //System.out.println("Connection pannel avviato");
        
        /*
        ipAddress = (TextView) findViewById(R.id.ipAddress);
        portNumber = (TextView) findViewById(R.id.port);
        ipAddressEditText = (EditText)findViewById(R.id.edit_ipAddress); 
        ipAddressEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        portEditText = (EditText)findViewById(R.id.edit_port); 
        portEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        */
        
        connectionStatus = (TextView)findViewById(R.id.status); 

        /*
        if(pAdapt.isThereAvaiableStream() == true){
        	//connessione attiva
        	
        	//connectBtn.setClickable(false);
    		//disconnectBtn.setClickable(true);
        	connectBtn.setVisibility(View.INVISIBLE);
        	disconnectBtn.setVisibility(View.VISIBLE);
        	autoconnectBtn.setVisibility(View.INVISIBLE);
        	
        	ipAddressEditText.setVisibility(View.INVISIBLE);
        	portEditText.setVisibility(View.INVISIBLE);
        	ipAddress.setVisibility(View.INVISIBLE);
        	portNumber.setVisibility(View.INVISIBLE);
        	
    		connectionStatus.setText("Connesso");
        }
        else{
        	//non ci sono connessioni attive
        	//connectBtn.setClickable(true);
    		//disconnectBtn.setClickable(false);
        	connectBtn.setVisibility(View.VISIBLE);
        	disconnectBtn.setVisibility(View.INVISIBLE);
        	autoconnectBtn.setVisibility(View.INVISIBLE); 
        	
        	ipAddressEditText.setVisibility(View.VISIBLE);
        	portEditText.setVisibility(View.VISIBLE);
        	ipAddress.setVisibility(View.VISIBLE);
        	portNumber.setVisibility(View.VISIBLE);
        	
    		connectionStatus.setText("Nessuna connessione");
        }*/
        
        
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
            disconnectBtn.setOnClickListener(this);
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
            break;
            case R.id.manualConnection:
            	setContentView(R.layout.configactivity);
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
             MessageIOStream tempSock = pAdapt.getAssociatedStream();
             tempSock.getMis().closeInput();
             tempSock.getMos().closeOutput();
             tempSock.close();
             
             System.out.println("Client: Connessione terminata");
             connectionStatus.setText("Disconnesso");
             connectBtn.setClickable(true);
             disconnectBtn.setClickable(false);
             //rimuovo lo stream associato durante la connessione
             pAdapt.setProtocolAdapter(null);
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
    			connectionStatus.setText("Connesso");
    			System.out.println("Client: Connessione stabilita");
    			connectBtn.setClickable(false);
    			disconnectBtn.setClickable(true);
    			//associo al protocolAdapter un socket e stream
    			pAdapt.setProtocolAdapter(socketAndStream);
    		}
    		catch (UnknownHostException ex) {
                System.out.println("Client: Impossibile connettersi"+ex.getLocalizedMessage());
                connectionStatus.setText("Impossibile connettersi");
                //System.exit(0);
            } catch (java.io.IOException ex) {
                System.out.println("Client: Impossibile connettersi"+ex.getLocalizedMessage());
                connectionStatus.setText("Impossibile connettersi");
                //System.exit(0);
            }
    	}
    	catch(NumberFormatException ex){
        	System.out.println("Client: Numero di porta errato:"+ex.getLocalizedMessage());
        	 connectionStatus.setText("Numero porta formalmente non valido");
        }
        
    }
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
    	//disabilita il relativo tasto del menu option
        menu.getItem(0).setEnabled(false);
        return true;
    }
}