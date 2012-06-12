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
	
	private netInterface.MessageIOStream socketAndStream;
	private Button connectBtn;
	private Button disconnectBtn;
	private ProtocolAdapter pAdapt;
	private TextView connectionStatus; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        System.out.println("ON CREATE");
        
        setContentView(R.layout.configactivity);
        connectBtn = (Button) findViewById(R.id.connect_button);
        connectBtn.setOnClickListener(this);
        
        disconnectBtn = (Button) findViewById(R.id.disconnect_button);
        disconnectBtn.setOnClickListener(this);
        disconnectBtn.setClickable(false);
        
        //scanBtn = (Button) findViewById(R.id.scan_button);
        //scanBtn.setOnClickListener(this);
        
        System.out.println("Connection pannel avviato");
        
        EditText ipAddressEditText = (EditText)findViewById(R.id.edit_ipAddress); 
        ipAddressEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        EditText portEditText = (EditText)findViewById(R.id.edit_port); 
        portEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        
        connectionStatus = (TextView)findViewById(R.id.connection_status); 
        
        pAdapt = ProtocolAdapter.getInstance();
        System.out.println("PADATP = "+pAdapt);

        if(pAdapt.isThereAvaiableStream() == true){
        	//c'era giˆ una connessione attiva
        	connectBtn.setClickable(false);
    		disconnectBtn.setClickable(true);
    		connectionStatus.setText("Connesso");
        }
        else{
        	//non c'erano connessioni attive
        	connectBtn.setClickable(true);
    		disconnectBtn.setClickable(false);
    		connectionStatus.setText("Nessuna connessione");
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
            case R.id.connect_button:
            	final EditText ipAddress = (EditText)findViewById(R.id.edit_ipAddress);
            	final EditText port = (EditText)findViewById(R.id.edit_port);
            	connectToServer(ipAddress.getText().toString(),port.getText().toString());
            break;
            case R.id.disconnect_button:
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