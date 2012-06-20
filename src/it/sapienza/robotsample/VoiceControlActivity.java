package it.sapienza.robotsample;


import java.util.ArrayList;
import java.util.Date;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class VoiceControlActivity extends BaseActivity implements OnTouchListener, RecognitionListener, OnCheckedChangeListener {
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
	/**
	 * Performance counter view.
	 */
	TextView performance_text;
	/**
	 * Editable text view.
	 */
	EditText edit_text;
	
	private ImageView signalImg;
	
	/**
	*La webView per la cam del robot;
	*/
	private WebView baseWV;
	
	private ToggleButton toggleBtn;

	private ArrayList<String> hotWords;
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			 Bundle bundle = msg.getData();
			 
			 System.out.println("HANDLER: RICEVUTO MESSAGGIO = "+bundle.getString("cmd"));
			 
		      if(bundle.containsKey("cmd")) {
		      
		    	String cmd = bundle.getString("cmd");
		    	
		        if(cmd.equals("STOP") || cmd.equals("OFF")){
		        	signalImg.setImageResource(R.drawable.stop);		        
				}
				else if(cmd.equals("FORWARD") || cmd.equals("STRAIGHT")){
					signalImg.setImageResource(R.drawable.forw);
				}
				else if(cmd.equals("BACKWARD") || cmd.equals("BACK")){
					signalImg.setImageResource(R.drawable.backw);
				}
				else if(cmd.equals("RIGHT")){
					signalImg.setImageResource(R.drawable.right);
				}
				else if(cmd.equals("LEFT")){
					signalImg.setImageResource(R.drawable.left);
				}
			}
		}
	};
	/**
	 * 
	 * Respond to touch events on the Speak button.
	 * 
	 * This allows the Speak button to function as a "push and hold" button, by
	 * triggering the start of recognition when it is first pushed, and the end
	 * of recognition when it is released.
	 * 
	 * @param v
	 *            View on which this event is called
	 * @param event
	 *            Event that was triggered.
	 */
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			start_date = new Date();
			this.listening = true;
			this.rec.start();
			break;
		case MotionEvent.ACTION_UP:
			Date end_date = new Date();
			long nmsec = end_date.getTime() - start_date.getTime();
			this.speech_dur = (float)nmsec / 1000;
			if (this.listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				this.rec_dialog = ProgressDialog.show(VoiceControlActivity.this, "", "Recognizing speech...", true);
				this.rec_dialog.setCancelable(false);
				this.listening = false;
			}
			this.rec.stop();
			break;
		default:
			;
		}
		/* Let the button handle its own state */
		return false;
	}
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pocketsphinx);
		
		signalImg = (ImageView) findViewById(R.id.signal);
		
		baseWV = (WebView)findViewById(R.id.webView);
		baseWV.loadUrl("http:www.google.it");
		baseWV.getSettings().setJavaScriptEnabled(true);
		baseWV.getSettings().setPluginsEnabled(true);
				
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
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		if(isChecked){
			System.out.println("ASCOLTO");
			this.listening = true;
			this.rec.start();
		
			//auto stop dopo X secondi
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
			  @Override
			  public void run() {
				  System.out.println("AUTO FERMO REC");
				  //PocketSphinxDemo.this.toggleBtn.setChecked(false);
			  }
			}, 5000);
		}
		else{
			System.out.println("FERMO");
			if (this.listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				this.listening = false;
			}
			this.rec.stop();
		}
		
	}

	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final VoiceControlActivity that = this;
		final String hyp = b.getString("hyp");

		System.out.println("RISULTATO PARZIALE CALCOLATO = "+hyp);
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
		final String hyp = b.getString("hyp");
		
		matchUtterance(hyp);
		//fermo registrazione quando riconosciuto comando
		VoiceControlActivity.this.toggleBtn.setChecked(false);
	}

	public void onError(int err) {
		System.out.println("ERRORE RICONOSCIMENTO = "+err);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.getItem(2).setEnabled(false);
		return true;
	}
	
	
	private void matchUtterance(String utt){
		
		System.out.println("UTTERANCE ARRIVATO: "+utt);
		
		String cmd = "STOP";
		
		if(utt != null){
		
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
		}
		System.out.println("IL COMANDO è: = "+cmd);
		
		//passo all'handler il messaggio per poterlo consegnare al thread dell' UI
		Message msg = this.rec.getHandler().obtainMessage();
		Bundle b = new Bundle();
		b.putString("cmd", cmd);
		msg.setData(b);
		this.rec.getHandler().sendMessage(msg);
		
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
		hotWords.add("STOP");
		hotWords.add("OFF");
		hotWords.add("FORWARD");
		hotWords.add("STRAIGHT");
		hotWords.add("BACKWARD");
		hotWords.add("RIGHT");
		hotWords.add("LEFT");
		hotWords.add("BACK");
	}	
}
	