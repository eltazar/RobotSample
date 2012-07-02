package it.sapienza.robotsample;

import java.io.IOException;

import netInterface.NetworkUtility;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class InterfacciaRobotActivity extends BaseActivity implements OnTouchListener, OnCheckedChangeListener{

	private DragController mDragController;
	private DragLayer mDragLayer;
	private WebView baseWV;
	private static InterfacciaRobotActivity IntRobot;

	private float mCenter_x;
	private float mCenter_y;
	private float increment = 0.5f;
	
	private final int activity_index = 0;
	
	private ProgressBar speedometer;
	private ImageView webcam;
	private ToggleButton arm;
	
	//gestore per i messaggi relativi alla progress bar
	private Handler handler = new Handler() {
		
		private int oldValue = 0;
		@Override
		public void handleMessage(Message msg) {
			//System.out.println("INCREMENTO PROGRESS BAR");
			//incremento la status bar col valore ritornato
			

			if(msg.what > oldValue){
				//System.out.println("ACCEL: ora è = "+speedometer.getProgress()+"arrivato = "+msg.what+" incremento di = "+Math.abs(msg.what-speedometer.getProgress()));
				speedometer.incrementProgressBy(Math.abs(msg.what-speedometer.getProgress()));
				oldValue = msg.what;
			}
			else if(msg.what < oldValue){
				//System.out.println("DECEL: ora è = "+speedometer.getProgress()+"arrivato = "+msg.what+" decremento di = "+Math.abs(msg.what-speedometer.getProgress()));
				speedometer.incrementProgressBy(-(Math.abs(msg.what-speedometer.getProgress())));
				oldValue = msg.what;
			}
//			else if(msg.what == 0){
//				for(int i = speedometer.getProgress(); i >= 0; --i){
//					
//						speedometer.incrementProgressBy(i-5);
//				}
//			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interfacciarobotactivity);

		//Otteniamo il riferimento alla WebView
		baseWV = (WebView)findViewById(R.id.baseWV);
		//baseWV.loadUrl("http://rackbot:rackbot@172.20.10.4/mobile.htm");
		//baseWV.loadUrl("http://www.google.it");
		baseWV.getSettings().setJavaScriptEnabled(true);
		baseWV.getSettings().setPluginsEnabled(true);

		speedometer = (ProgressBar) findViewById(R.id.speed);
		
		mDragController = new DragController(this, handler);
		setUpViews();
		IntRobot = this;
		
		arm = (ToggleButton) findViewById(R.id.arm);
		arm.setOnCheckedChangeListener(this);
		System.out.println("INTERFACCIA ROBOT -> ONCREATE");
	}
	
	public void onPause(){
		super.onPause();
		System.out.println("INTERFACCIA ROBOT -> ONPAUSE");
	}
	
	public void onResume(){
		super.onResume();
		System.out.println("INTERFACCIA ROBOT -> ONRESUME");
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.getItem(activity_index).setEnabled(false);
		return true;
	}

	//metodo che si attiva ognivolta che l'oggetto viene toccato
	@Override
	public boolean onTouch(View v, MotionEvent ev) {

		//calcolo l'altezza della status bar
		Rect rect= new Rect();
		Window window= this.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);
		int status_bar_height= rect.top;

		//calcolo le coordinate iniziali del centro della ImageView
		final int start_x = findViewById(R.id.imageView2).getLeft();
		final int start_y = findViewById(R.id.imageView2).getTop();
		final int image_width = findViewById(R.id.imageView2).getWidth();
		final int image_height = findViewById(R.id.imageView2).getHeight();

		mCenter_x = start_x + (image_width/2) + increment;
		mCenter_y = start_y + (image_height/2) + status_bar_height + increment;

		//simply start on the down event
		mDragController.startDrag(v, DragController.DRAG_ACTION_MOVE, mCenter_x, mCenter_y);

		return false;
	}

	//setta le variabili private
	private void setUpViews() {

		DragController dragController = mDragController;

		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
		mDragLayer.setDragController(dragController);
		dragController.addDropTarget(mDragLayer);
		webcam = (ImageView) findViewById(R.id.imageView3);

		ImageView IW = (ImageView) findViewById(R.id.imageView2);

		//controllo sulla webcam
		webcam.setOnClickListener(new ImageView.OnClickListener() {  
        public void onClick(View v)
            {
        	
        		if(NetworkUtility.getInstance().getIpAddresses().size() == 0)
        			Toast.makeText(getApplicationContext(), "Webcam non disponibile, effettuare una nuova scansione", Toast.LENGTH_LONG).show();
        		else {
        		FrameLayout frameLayout = (FrameLayout)findViewById(R.id.preview);
        		findViewById(R.id.imageView2).setVisibility(WebView.GONE);
        		
                //lancia la nuova activity
        		new WebcamHandler(frameLayout, IntRobot);
        		}
            }
         });

		//controllo sul joypad
		IW.setOnTouchListener(this);
	}
	
	//setta l'ip della webcam al valore selezionato dall'utente
	public void setSelectedIp(String ipWebcam)
	{
		String finalIp = "http://rackbot:rackbot@" + ipWebcam + "/mobile.htm";
		baseWV.loadUrl(finalIp);
		findViewById(R.id.imageView2).setVisibility(WebView.VISIBLE);
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		if(isChecked){
			
			try {
				System.out.println("PRENDO OGGETTO");
				ProtocolAdapter.getInstance().sendMessage("#armu00\r");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		else{
			try {
				System.out.println("LASCIO OGGETTO");
				ProtocolAdapter.getInstance().sendMessage("#armd00\r");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}	
	}
}

