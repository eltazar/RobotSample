package it.sapienza.robotsample;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import netInterface.NetworkUtility;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VoiceControlActivity extends BaseActivity implements RecognitionListener, OnCheckedChangeListener, OnClickListener {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}
	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date start_date;
	/**
	 * Number of seconds of speech.
	 */
	float speech_dur;
	/**
	 * Are we listening?
	 */
	boolean listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog rec_dialog;
	
	private ImageView signalImg;
	
	/**
	*La webView per la cam del robot;
	*/
	private WebView baseWV;
	private final int activity_index = 1;
	private ToggleButton toggleBtn;
	private ProgressBar speedometer;
	private Timer timer;
	private ImageView webcam;
	private static VoiceControlActivity vocalInt;
	private ArrayList<String> hotWords;
	private ArrayList<String> speedWords;
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			 Bundle bundle = msg.getData();

			 ProtocolAdapter pAdapt = ProtocolAdapter.getInstance();

			 if(bundle.containsKey("check")){
				//fermo registrazione quando riconosciuto comando
					VoiceControlActivity.this.toggleBtn.setChecked(false);
			 }
			 else if(bundle.containsKey("cmd") /*&& bundle.containsKey("spd")*/) {
		      
				System.out.println("HANDLER: RICEVUTO COMANDO = "+bundle.getString("cmd")/*+" "+bundle.getInt("spd")*/);
		    	String cmd = bundle.getString("cmd");
		    	
		    	//per comandi vocali avanzati 
		    	//String spd = bundle.getString("spd");
		    	//String command = String.format("SPD0%d\r", spd);
		    	try {
		    		if(cmd.equals("STOP") || cmd.equals("OFF")){
		    			signalImg.setImageResource(R.drawable.stop);	
		    			speedometer.setProgress(0);
		    			pAdapt.sendMessage("#SPD00\r");
		    			pAdapt.sendMessage("#TRN00\r");		        	
		    		}
		    		else if(cmd.equals("FORWARD") || cmd.equals("STRAIGHT")){
		    			signalImg.setImageResource(R.drawable.forw);
		    			speedometer.setProgress(90);
		    			//pAdapt.sendMessage(command);
		    			pAdapt.sendMessage("#SPD090\r");
		    			pAdapt.sendMessage("#TRN00\r");
		    		}
		    		else if(cmd.equals("BACKWARD") || cmd.equals("BACK")){
		    			signalImg.setImageResource(R.drawable.backw);
		    			speedometer.setProgress(90);
		    			pAdapt.sendMessage("#SPD0-90\r");
		    			pAdapt.sendMessage("#TRN00\r");
		    		}
		    		else if(cmd.equals("RIGHT")){
		    			signalImg.setImageResource(R.drawable.right);
		    			speedometer.setProgress(60);
		    			pAdapt.sendMessage("#SPD00\r");
		    			pAdapt.sendMessage("#TRN060\r");
		    		}
		    		else if(cmd.equals("LEFT")){
		    			signalImg.setImageResource(R.drawable.left);
		    			speedometer.setProgress(60);
		    			pAdapt.sendMessage("#SPD00\r");
		    			pAdapt.sendMessage("#TRN0-60\r");
		    		}
		    	 } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
			}
		}
	};
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		timer = new Timer();
		vocalInt = this;
		System.out.println("VOICE CONTROL: On Create");
		setContentView(R.layout.pocketsphinx);

		signalImg = (ImageView) findViewById(R.id.signal);
		speedometer = (ProgressBar) findViewById(R.id.speedo);
		baseWV = (WebView)findViewById(R.id.webView);
		//baseWV.loadUrl("http://www.google.it");
		baseWV.getSettings().setJavaScriptEnabled(true);
		baseWV.getSettings().setPluginsEnabled(true);

		webcam = (ImageView) findViewById(R.id.webcam);
		webcam.setOnClickListener(this);
		
		createCommands();

		this.rec = new RecognizerTask(handler);
		//rec.setUsePartials(true);
		this.rec_thread = new Thread(this.rec);
		this.listening = false;

		toggleBtn = (ToggleButton) findViewById(R.id.toggle);
		toggleBtn.setOnCheckedChangeListener(this);


		this.rec.setRecognitionListener(this);
		this.rec_thread.start();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		System.out.println("VOICE CONTROL: On Pause");
		//this.rec.setRecognitionListener(null);
		//this.rec.stop();
		//this.rec_thread.interrupt();
		//this.rec_thread = null;
		
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		if(isChecked){
			System.out.println("ASCOLTO");
			this.listening = true;
			this.rec.start();
			
			timer = new Timer();
			
			try{
				timer.schedule(new TimerTask() {

					public void run() {
						runOnUiThread(new Runnable() {
			                public void run() {
			                    // some code #3 (that needs to be ran in UI thread)
								System.out.println("AUTO FERMO REC");
								toggleBtn.setChecked(false);
			                }
			            });						
					}
				}, 5000);
			}
			catch(IllegalStateException e){
				System.out.println("ERRORE timer: "+e.getLocalizedMessage());
			}
		}
		else{
			System.out.println("FERMO");
			if (this.listening) {
				this.listening = false;
			}
			if(timer != null){
				timer.cancel();
				timer.purge();
			}
			this.rec.stop();
		}
		
	}

	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final VoiceControlActivity that = this;
		final String hyp = b.getString("hyp");
		
		
		try{
			if(hyp.split(" ").length == 3 || hyp.split(" ").length == 2){
				Message msg = this.rec.getHandler().obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putString("check", "check");
				//b.putInt("spd",speed);
				msg.setData(bundle);
				this.rec.getHandler().sendMessage(msg);
			}
		}
		catch(NullPointerException e){
			System.out.println("Frase parziale null point exception");
		}
		
		//System.out.println("RISULTATO PARZIALE CALCOLATO = "+hyp);
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
		final String hyp = b.getString("hyp");
		matchUtterance(hyp);
	}

	public void onError(int err) {
		System.out.println("ERRORE RICONOSCIMENTO = "+err);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.getItem(activity_index).setEnabled(false);
		return true;
	}
	
	
	private void matchUtterance(String utt){
		
		System.out.println("UTTERANCE ARRIVATO: "+utt);
		
		String cmd = "STOP";
		int speed = 30;
		
		if(utt != null){
		
			//vettore parole utterance
			String[] words = utt.split(" ");
			
			boolean commandRecognized = false;

			for(String hot:hotWords){
				for(int i = 0; i < words.length; i++){
					if(hot.equals(words[i])){
						//trovato parola
						commandRecognized = true;
						cmd = hot;
					}
				}
				if(commandRecognized){
					break;
				}			
			}
	
			/*//per gestire velocit� comando
			if(words.length == 3){
				
				if( words[1].equals("LEFT") || words[1].equals("RIGHT") || words[1].equals("FORWARD")){
					
					if(words[2].equals("ONE")){
						speed = 60;
					}
					else if(words[2].equals("TWO")){
						speed = 90;
					}
				}
			}
			*/
			
			System.out.println("IL COMANDO �: = "+cmd /*+"VELOCIT�  = "+speed*/);
			
			//passo all'handler il messaggio per poterlo consegnare al thread dell' UI
			Message msg = this.rec.getHandler().obtainMessage();
			Bundle b = new Bundle();
			b.putString("cmd", cmd);
			//b.putInt("spd",speed);
			msg.setData(b);
			this.rec.getHandler().sendMessage(msg);
		}

		
	}
	
	private void sendCommand(String cmd){
		
		ProtocolAdapter pAdapt = ProtocolAdapter.getInstance();
		
		//int id = getResources().getIdentifier("yourpackagename:drawable/" + StringGenerated, null, null);
		
		if(cmd.equals("STOP") || cmd.equals("OFF")){
			
			//invio comandi per stare fermo
		}
		else if(cmd.equals("FORWARD") || cmd.equals("STRAIGHT")){
			ImageView signalImg = (ImageView) findViewById(R.id.signal);
			signalImg.setImageResource(R.drawable.forw);
		}
		else if(cmd.equals("BACWARD") || cmd.equals("BACK")){
			
		}
		else if(cmd.equals("RIGHT")){

		}
		else if(cmd.equals("LEFT")){
			
		}

//		PocketSphinxDemo.this.runOnUiThread(new Runnable() {
//		    public void run(int id) {
//		    	
//		    	
//		    }
//		});
		
	}
	
	private void createCommands(){
	
		hotWords = new ArrayList<String>();
		speedWords = new ArrayList<String>();
		
		hotWords.add("STOP");
		hotWords.add("OFF");
		hotWords.add("FORWARD");
		hotWords.add("STRAIGHT");
		hotWords.add("BACKWARD");
		hotWords.add("BACK");
		hotWords.add("RIGHT");
		hotWords.add("LEFT");
		//hotWords.add("TAKE");
		//hotWords.add("LEAVE");
		
		
		speedWords.add("ONE");
		speedWords.add("TWO");
	}

	@Override
	public void onClick(View v) {
		switch ( v.getId() ) {
			case R.id.webcam:
				if(NetworkUtility.getInstance().getIpAddresses().size() == 0){
        			Toast.makeText(getApplicationContext(), "Webcam non disponibile, effettuare una nuova scansione", Toast.LENGTH_LONG).show();
        			}
        		else {
        			FrameLayout frameLayout = (FrameLayout)findViewById(R.id.preview);        		
        			//lancia la nuova activity
        			new WebcamHandler(frameLayout, vocalInt);
        		}
				break;
			default:
				break;
		}
	}
	
	//setta l'ip della webcam al valore selezionato dall'utente
	public void setSelectedIp(String ipWebcam)
	{
		//String finalIp = "http://rackbot:rackbot@" + ipWebcam + "/mobile.htm";
		//baseWV.loadUrl(finalIp);
		baseWV.loadUrl(ipWebcam);
	}
}
	