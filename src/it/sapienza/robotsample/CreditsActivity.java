package it.sapienza.robotsample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CreditsActivity extends BaseActivity implements OnClickListener {

	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.credits);
	}
	
	public void onResume(){
		super.onResume();
		
		
		//bottoni nomi ed email
        Button supervision = (Button)findViewById(R.id.ButtonS);
        supervision.setOnClickListener(this);
        
        Button dev1 = (Button)findViewById(R.id.dev1);
        dev1.setOnClickListener(this);
        Button dev2 = (Button)findViewById(R.id.dev2);
        dev2.setOnClickListener(this);
        Button dev3 = (Button)findViewById(R.id.dev3);
        dev3.setOnClickListener(this);
        
        Button ur1 = (Button)findViewById(R.id.ur1);
        ur1.setOnClickListener(this);
        Button ur2 = (Button)findViewById(R.id.ur2);
        ur2.setOnClickListener(this);
        
        Button web = (Button)findViewById(R.id.ButtonW);
        web.setOnClickListener(new OnClickListener() {
        @Override
            public void onClick(View v) {
        		Intent web = new Intent("android.intent.action.VIEW", Uri.parse("http://www.sapienzaapps.it/"));  
        		startActivity(web);
            }
        }); 
        
        Button web2 = (Button)findViewById(R.id.ButtonW2);
        web2.setOnClickListener(new OnClickListener() {
        @Override
            public void onClick(View v) {
        		Intent web = new Intent("android.intent.action.VIEW", Uri.parse("http://www.rackbot.com/"));  
        		startActivity(web);
            }
        }); 
        
      
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId()){
			case R.id.dev1:
				sendEmail(this, new String[]{"mrgreco3@gmail.com"}, "Invio Email", "Android RobotRack: ", "Testo email");
				break;
			case R.id.dev2:
				sendEmail(this, new String[]{"damianoher@gmail.com"}, "Invio Email", "Android RobotRack: ", "Testo email");
				break;
			case R.id.dev3:
				sendEmail(this, new String[]{"federico.scacco@gmail.com"}, "Invio Email", "Android RobotRack: ", "Testo email");
				break;
			case R.id.ButtonS:
				sendEmail(this, new String[]{"panizzi@di.uniroma1.it"}, "Invio Email", "Android RobotRack: ", "Testo email");
				break;
			case R.id.ur1:
				sendEmail(this, new String[]{"redeagle_@hotmail.it"}, "Invio Email", "Android RobotRack: ", "Testo email");
				break;
			case R.id.ur2:
				sendEmail(this, new String[]{"flaviarot88@gmail.com"}, "Invio Email", "Android RobotRack: ", "Testo email");
				break;
			
		}
	}
	
	//METODO PER INVIO EMAIL
		public static void sendEmail(Context context, String[] recipientList,
	            String title, String subject, String body) {
	        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

	        emailIntent.setType("plain/text");

	        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipientList);

	        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

	        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

	        context.startActivity(Intent.createChooser(emailIntent, title));

	}

	
	
}
