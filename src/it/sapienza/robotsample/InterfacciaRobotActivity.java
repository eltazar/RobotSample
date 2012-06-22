package it.sapienza.robotsample;

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
import android.widget.ImageView;
import android.widget.ProgressBar;

public class InterfacciaRobotActivity extends BaseActivity implements OnTouchListener {

	private DragController mDragController;
	private DragLayer mDragLayer;
	private WebView baseWV;

	private float mCenter_x;
	private float mCenter_y;
	private float increment = 0.5f;
	
	private final int activity_index = 1;
	
	private ProgressBar speedometer;
	
	//gestore per i messaggi relativi alla progress bar
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//System.out.println("INCREMENTO PROGRESS BAR");
			//incremento la status bar col valore ritornato
			if(msg.what == 0){
				for(int i = speedometer.getProgress(); i >= 0; i--){
					speedometer.incrementProgressBy(-i);
				}
			}
			speedometer.incrementProgressBy(msg.what);
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

		ImageView IW = (ImageView) findViewById(R.id.imageView2);

		IW.setOnTouchListener(this);
	}
}
