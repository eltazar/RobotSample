package it.sapienza.robotsample;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/*
* Si occupa di associare a ogni activty che la eredita lo stesso optionsMenu
* */
public class BaseActivity extends Activity{
	
	private Intent config = null;
	private Intent joypad = null;
	private Intent voicecontrol = null;

	
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
   		default:
   			return super.onOptionsItemSelected(item);
   	}

   }

	
	//setta l'ip della webcam al valore selezionato dall'utente
	public void setSelectedIp(String ipWebcam)
	{
	}
}
