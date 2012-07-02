package it.sapienza.robotsample;


import netInterface.NetworkUtility;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.os.Handler;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AccelerometroActivity extends BaseActivity implements SensorEventListener {
	private SensorManager sensorManager;
	private WebView baseWV;
	private static ProtocolAdapter protocolAdapter;
	private ProgressBar speedometer;
	private ImageView webcam;
	private static AccelerometroActivity AccelRobot;
	private final int activity_index = 3;
	
	private Handler handler = new Handler() {
			
			private int oldValue = 0;
			@Override
			public void handleMessage(Message msg) {
				//System.out.println("INCREMENTO PROGRESS BAR");
				//incremento la status bar col valore ritornato

				if(msg.what > oldValue){
					//System.out.println("ACCEL: ora = "+speedometer.getProgress()+"arrivato = "+msg.what+" incremento di = "+Math.abs(msg.what-speedometer.getProgress()));
					speedometer.incrementProgressBy(Math.abs(msg.what-speedometer.getProgress()));
					oldValue = msg.what;
				}
				else if(msg.what < oldValue){
					//System.out.println("DECEL: ora = "+speedometer.getProgress()+"arrivato = "+msg.what+" decremento di = "+Math.abs(msg.what-speedometer.getProgress()));
					speedometer.incrementProgressBy(-(Math.abs(msg.what-speedometer.getProgress())));
					oldValue = msg.what;
				}
			}
		};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		//*******************ACCELLEROMETRO

		super.onCreate(savedInstanceState);
		setContentView(R.layout.accelerometro);

		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		// aggiungo il listener. il listener sar� questa stessa classe
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		
		
		//*******************WEBVIEW
		
		//Otteniamo il riferimento alla WebView
		baseWV = (WebView)findViewById(R.id.webView);
		
		baseWV.loadUrl("http://www.google.it");
		baseWV.getSettings().setJavaScriptEnabled(true);
		baseWV.getSettings().setPluginsEnabled(true);
		
		//
		speedometer = (ProgressBar) findViewById(R.id.speedo);
		setUpViews();
		AccelRobot = this;
		
	}
	
	private void setUpViews() {

		webcam = (ImageView) findViewById(R.id.webcam);

		//controllo sulla webcam
		webcam.setOnClickListener(new ImageView.OnClickListener() {  
        public void onClick(View v)
            {
        	
        		if(NetworkUtility.getInstance().getIpAddresses().size() == 0)
        			Toast.makeText(getApplicationContext(), "Webcam non disponibile, effettuare una nuova scansione", Toast.LENGTH_LONG).show();
        		else {
        		FrameLayout frameLayout = (FrameLayout)findViewById(R.id.preview);
        		//findViewById(R.id.imageView2).setVisibility(WebView.GONE);
        		
                //lancia la nuova activity
        		new WebcamHandler(frameLayout, AccelRobot);
        		}
            }
         });
	}
	
	@Override
    protected void onPause() {
        //  chiamata quando un'altra attivit� viene visualizzata
        super.onPause();
        sensorManager.unregisterListener(this);
    }
   

    @Override
    protected void onResume() {
        //chiamata quando si riattiva l'istanza
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    protected void onStop() {
        //chiamata quando non � pi� visibile
        super.onStop();
        sensorManager.unregisterListener(this);
    }

	public void onSensorChanged(SensorEvent event){

		// check sensor type
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			
			
			float [] XVector= new float [3];
			float [] YVector= new float [3];
			float [] ZVector= new float [3];
			float [] RawVector= new float [3];
			float [] AccelerometerVector= new float [3];
			float [] AccelerometerZeroVector= new float [3];
			float AccelerometerZeroRollZ;
		    float Pitch,Roll;
		    float xRoll,yRoll,zRoll;
		    float xRollDiff,yRollDiff,zRollDiff;
		    float Sensitivity=1.0f;

			RawVector[0]=event.values[0];
			RawVector[1]=event.values[1];
			RawVector[2]=event.values[2];
			
			XVector[0]=1.0f;XVector[1]=0.0f;XVector[2]=0.0f;
			YVector[0]=0.0f;YVector[1]=1.0f;YVector[2]=0.0f;
			ZVector[0]=0.0f;ZVector[1]=0.0f;ZVector[2]=1.0f;
			
			float magnitude = (float)(Math.sqrt((double)RawVector[0]*RawVector[0] + (double)RawVector[1]*RawVector[1] + (double)RawVector[2]*RawVector[2]));

			AccelerometerVector[0] = (event.values[0]/magnitude);
		    AccelerometerVector[1] = (event.values[1]/magnitude);
		    AccelerometerVector[2] = (event.values[2]/magnitude);
			
		    AccelerometerZeroVector[0]=-0.6f;
		    AccelerometerZeroVector[1]=0.0f;
		    AccelerometerZeroVector[2]=-0.8f;
		    
		    xRoll=((float)Math.acos(MVectScalarProductOf(AccelerometerVector,XVector)));    
		    yRoll=((float)Math.acos(MVectScalarProductOf(AccelerometerVector,YVector)));     
		    zRoll=((float)Math.acos(MVectScalarProductOf(AccelerometerVector,ZVector)));  
		    
		    xRollDiff=xRoll-((float)Math.acos(MVectScalarProductOf(AccelerometerVector,XVector)));    
		    yRollDiff=yRoll-((float)Math.acos(MVectScalarProductOf(AccelerometerVector,YVector)));    
		    zRollDiff=zRoll-((float)Math.acos(MVectScalarProductOf(AccelerometerVector,ZVector)));  
		    
		    xRollDiff*=Sensitivity;
		    yRollDiff*=Sensitivity;
		    zRollDiff*=Sensitivity;
		    
		    xRollDiff=Math.max(-1.0f,Math.min(1.0f,xRollDiff));
		    yRollDiff=Math.max(-1.0f,Math.min(1.0f,yRollDiff));
		    zRollDiff=Math.max(-1.0f,Math.min(1.0f,zRollDiff));
		    
		    AccelerometerZeroRollZ=(float)Math.acos(MVectScalarProductOf(AccelerometerZeroVector,ZVector));
		    
		    if (AccelerometerZeroRollZ>=0.78539816339745f && AccelerometerZeroRollZ<=2.35619449019235f) 
		        Pitch=zRollDiff; 
		    else if (zRoll>1.5707963267949f) 
		        Pitch=-xRollDiff; 
		    else 
		        Pitch=-1.0f; 
		    Roll=yRollDiff; 
		    
		    Pitch=Math.min(1.0f,Math.max(-1.0f,Pitch*2.0f)); //Sensibilit�
		    Roll=Math.min(1.0f,Math.max(-1.0f,Roll*1.5f)); //Sensibilit�

		    int speed=(int)Math.max(-128.0f,Math.min(128.0f,Pitch * 128.0f));
		    int turn=(int)Math.max(-128.0f,Math.min(128.0f,Roll * 128.0f));
		    /*
		    try{

				protocolAdapter.sendMessage(Speed, Turn);
				
			}
			catch (java.io.IOException ex) {
				System.out.println("Eccezione in sendMessage: "+ex.getLocalizedMessage());
			}
		    */
		    
		    float maxSpd = Math.max(Math.abs(turn),Math.abs(speed));
			int spd = (int)(maxSpd);
		    handler.sendEmptyMessage(spd);
		    
		    System.out.println("ROLL = "+Roll+ "PITCH  = "+Roll+" VELOCIT� = "+spd);
		    
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.getItem(activity_index).setEnabled(false);
		return true;
	}

	
	public double MVectScalarProductOf(float [] p_vector1, float [] p_vector2)
	{
	    return (((double)p_vector1[0]*p_vector2[0] + (double)p_vector1[1]*p_vector2[1] + (double)p_vector1[2]*p_vector2[2]));
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}
}