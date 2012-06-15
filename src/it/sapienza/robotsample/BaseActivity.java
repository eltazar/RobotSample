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

	
	public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.option_menu, menu);

        return true;

    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
   	 
        //respond to menu item selection
    	switch (item.getItemId()) {
    		case R.id.config:
    			startActivity(new Intent(this, ConfigurationActivity.class));
    			return true;
    		case R.id.JoyPad:
    			startActivity(new Intent(this, InterfacciaRobotActivity.class));
    			return true;
    		case R.id.voiceControl:
    			startActivity(new Intent(this, PocketSphinxDemo.class));
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}

    }
}
