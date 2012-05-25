package it.sapienza.robotsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class StandardRobotController extends BaseActivity implements OnClickListener, OnSeekBarChangeListener {

	private ProtocolAdapter pAdapt;
	private Button aheadBtn;
	private Button backBtn;
	private Button leftBtn;
	private Button rightBtn;
    private TextView messageFromServer;
    private SeekBar bar;
    private TextView textProgress;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.standardrobotcontroller);
        
        aheadBtn = (Button) findViewById(R.id.ahead);
        aheadBtn.setOnClickListener(this);
        backBtn = (Button) findViewById(R.id.back);
        backBtn.setOnClickListener(this);
        rightBtn = (Button) findViewById(R.id.right);
        rightBtn.setOnClickListener(this);
        leftBtn = (Button) findViewById(R.id.left);
        leftBtn.setOnClickListener(this);
        
        messageFromServer = (TextView) findViewById(R.id.message_server);
        pAdapt = ProtocolAdapter.getInstance();
        
        bar = (SeekBar)findViewById(R.id.seekBar1); // make seekbar object
        bar.setOnSeekBarChangeListener(this); // set seekbar listener.
     // make text label for progress value
        textProgress = (TextView)findViewById(R.id.textViewProgress);
           
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
    		boolean fromUser) {
    	// TODO Auto-generated method stub

    	// change progress text label with current seekbar value
    	textProgress.setText("The value is: "+(progress-128));
    	sendMessage("#SPD0"+(progress-128)+"\r");
    	sendMessage("#TRN00\r");
    	// change action text label to changing
    }
	
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    	System.out.println("HO CLICCATO UN PULSANTE");
        switch ( v.getId() ) {
            case R.id.ahead:
            	sendMessage("#armu\r");
            break;
            case R.id.back:
            	sendMessage("#armd\r");
            break;
            case R.id.left:
            	sendMessage("#SPD00\r");
            break;
            case R.id.right:
            	sendMessage("right");
            break;
        }
    }
    
    //invia il messaggio al server (robot)
    private void sendMessage(String message){
    	
    	String answer = "";
    	
    	try{
    		answer = pAdapt.sendMessage(message);
    	}
    	catch (java.io.IOException ex) {
            System.out.println("Eccezione in sendMessage: "+ex.getLocalizedMessage());
        }
    	
    	//aggiorno textView
    	messageFromServer.setText(answer);
    }

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
    
}
