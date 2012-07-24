package it.sapienza.robotsample;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/*
* Si occupa di associare a ogni activty che la eredita lo stesso optionsMenu
* */
public class BaseActivity extends Activity{
	
	private Intent config = null;
	private Intent joypad = null;
	private Intent voicecontrol = null;
	private Intent accelero = null;
	private Intent credits = null;

	
	public boolean onCreateOptionsMenu(Menu menu) {

       MenuInflater inflater = getMenuInflater();

       inflater.inflate(R.menu.option_menu, menu);

       return true;

   }
	
	public boolean onOptionsItemSelected(MenuItem item) {
  	
       //respond to menu item selection
   	switch (item.getItemId()) {
   		case R.id.config:
   			if(config == null)
   				config = new Intent(this, ConfigurationActivity.class);
   			startActivity(config);
   			return true;
   		case R.id.JoyPad:
   			if(joypad == null)
   				joypad = new Intent(this, InterfacciaRobotActivity.class);
   			startActivity(joypad);
   			return true;
   		case R.id.voiceControl:
   			if(voicecontrol == null)
   				voicecontrol = new Intent(this, VoiceControlActivity.class);
   			startActivity(voicecontrol);   			
   			return true;
   		case R.id.accelero:
   			if(accelero == null)
   				accelero = new Intent(this, AccelerometroActivity.class);
   			startActivity(accelero);   			
   			return true;
   		case R.id.credits:
   			//credits(this); 			
   			if(credits == null)
   				credits = new Intent(this, CreditsActivity.class);
   			startActivity(credits);
   			return true;
   		default:
   			return super.onOptionsItemSelected(item);
   	}

   }

	
	//setta l'ip della webcam al valore selezionato dall'utente
	public void setSelectedIp(String ipWebcam)
	{
	}
	
	private  void exit(){
		try {
			ProtocolAdapter.getInstance().closeCommunication();
		} catch (IOException e) {
			System.out.println("Errore disconnessione: "+e.getLocalizedMessage());
		}
	    this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    		    	
	    	ActivityManager mngr = (ActivityManager) getSystemService( ACTIVITY_SERVICE );

	    	List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);

	    	System.out.println(this.getClass().getName()+": LISTA DI TASK = "+taskList.get(0).numActivities);
	    	
	    	if((taskList.get(0).numActivities == 1) && 
	    			taskList.get(0).topActivity.getClassName().equals(this.getClass().getName())) {
	    		
    			System.out.println(this.getClass().getName() +" ultima activity dello stack");

    			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
    			alertbox.setTitle("RobotSample");
    			alertbox.setMessage("Vuoi davvero uscire?");

    			alertbox.setPositiveButton("Si",
    					new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface arg0, int arg1) {
    					exit();
    				}
    			});

    			alertbox.setNeutralButton("No",
    					new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface arg0, int arg1) {
    				}
    			});

    			alertbox.show();
    			
    			return true;
	    	}
	    	else {
		        return super.onKeyDown(keyCode, event);
		    }
	    	
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	
	
}
