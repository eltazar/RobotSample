package it.sapienza.robotsample;

import java.net.InetAddress;
import java.net.UnknownHostException;

import netInterface.*;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.Menu;

public class RobotSampleActivity extends Activity implements OnClickListener{
	
	private netInterface.MessageIOStream socketAndStream;
	private Button connectBtn;
	private Button disconnectBtn;
	private ProtocolAdapter pAdapt;
	private TextView connectionStatus; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        connectBtn = (Button) findViewById(R.id.connect_button);
        connectBtn.setOnClickListener(this);
        
        disconnectBtn = (Button) findViewById(R.id.disconnect_button);
        disconnectBtn.setOnClickListener(this);
        disconnectBtn.setClickable(false);
        System.out.println("Connection pannel avviato");
        
        EditText ipAddressEditText = (EditText)findViewById(R.id.edit_ipAddress); 
        ipAddressEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        EditText portEditText = (EditText)findViewById(R.id.edit_port); 
        portEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        
        connectionStatus = (TextView)findViewById(R.id.connection_status); 
        
        pAdapt = ProtocolAdapter.getInstance();
        
        //se ho salvato uno stato precedente, in cui la connessione era stata stabilita setto i tasti
        if( savedInstanceState != null ) {
        	System.out.println("Recupero stato salvato");
        	if(savedInstanceState .getString("connStatus").equals("Connesso")){
        		connectBtn.setClickable(false);
        		disconnectBtn.setClickable(true);
        	}
        	//setto la label con lo status giusto
        	connectionStatus.setText(savedInstanceState .getString("connStatus"));
        }
    }
    
    @Override
    protected void onSaveInstanceState (Bundle outState){
    	super.onSaveInstanceState(outState);
    	System.out.println("SALVO STATO");
    	outState.putString("connStatus",connectionStatus.getText().toString());
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.option_menu, menu);

        return true;

    }
  
    public boolean onOptionsItemSelected(MenuItem item) {
    	 
        //respond to menu item selection
    	switch (item.getItemId()) {
    		case R.id.standardcontroller:
    			startActivity(new Intent(this, StandardRobotController.class));
    			return true;
    		case R.id.bho:
    			//startActivity(new Intent(this, Help.class));
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}

    }

    
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
    			socketAndStream = new MessageIOStream( InetAddress.getByName(ipAddress),portNumber);
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
}