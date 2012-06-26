package it.sapienza.robotsample;

import java.util.ArrayList;

import netInterface.NetworkUtility;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

public class WebcamHandler {

	private ArrayList<String> ips;
	private BaseActivity gui;
	private int ips_size;
	private WebView preview1;
	private WebView preview2;
	private FrameLayout frameLayout;

	private int num_pages;
	private int current_page;

	private TextView previous;
	private TextView next;
	private TextView pages;
	private TextView firstIp;
	private TextView secondIp;

	public WebcamHandler (FrameLayout frame, BaseActivity irb) {

		ips = NetworkUtility.getInstance().getIpAddresses();
		setIRB(irb);
		
		frameLayout = frame;

		frameLayout.setVisibility(FrameLayout.VISIBLE);
		frameLayout.focusableViewAvailable(frameLayout);

		preview1 = (WebView)frameLayout.findViewById(R.id.webView1);
		preview1.setInitialScale(1);
		preview2 = (WebView)frameLayout.findViewById(R.id.webView2);
		preview2.setInitialScale(1);

		previous = (TextView)frameLayout.findViewById(R.id.previous);
		next = (TextView)frameLayout.findViewById(R.id.next);
		pages = (TextView)frameLayout.findViewById(R.id.pages);
		firstIp = (TextView)frameLayout.findViewById(R.id.firstip);
		secondIp = (TextView)frameLayout.findViewById(R.id.secondip);



		/*
		 * Da decommentare se presente il robot
		 *  
		 */
		ips = NetworkUtility.getInstance().getIpAddresses();

		/*
		 * Da commentare se presente il robot
		 *  
		 */
		/*
		ips = new ArrayList<String>();
		ips.add("http://www.java.it/");
		ips.add("http://www.wikipedia.it/");
		ips.add("http://www.mare.it/");
		ips.add("http://www.google.it/");
		ips.add("http://www.meteo.it/");
		 */
		ips_size = ips.size();

		setWebViews();
		setLink();
	}

	//gestisce la visualizzazione delle preview
	private void setWebViews() {

		num_pages = ips_size %2 == 0 ? ips_size/2 : (ips_size+1)/2;
		current_page = 0;
		showWebViews(current_page);

		//gestisce il click sulla prima preview		
		preview1.setOnTouchListener(new WebView.OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {

				getIRB().setSelectedIp(ips.get(current_page*2));
				frameLayout.setVisibility(FrameLayout.GONE);
				return false;
			}

		});

		//gestisce il click sulla seconda preview		
		preview2.setOnTouchListener(new WebView.OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {

				getIRB().setSelectedIp(ips.get(current_page*2+1));
				frameLayout.setVisibility(FrameLayout.GONE);
				return false;
			}

		});

	}

	//carica il contenuto delle preview
	private void showWebViews(int page) {

		setCurrent_page(page);
		int index = (current_page * 2);

		//devo riempire tutte e due le preview
		if((index+1) < ips_size) {

			//carica l'url delle preview
			preview1.loadUrl(formURL(ips.get(index)));
			preview2.loadUrl(formURL(ips.get(index+1)));

			//mostra anche la seconda preview
			preview2.setVisibility(WebView.VISIBLE);
			secondIp.setVisibility(TextView.VISIBLE);

			//inserisce l'ip trovato nell'etichetta delle preview
			firstIp.setText(getIpLabel(ips.get(index)));
			secondIp.setText(getIpLabel(ips.get(index+1)));
		}

		//devo riempire una sola preview
		else {

			//carica l'url delle preview
			preview1.loadUrl(formURL(ips.get(index)));

			//nasconde la seconda preview
			preview2.setVisibility(WebView.GONE);
			secondIp.setVisibility(TextView.GONE);

			//inserisce l'ip trovato nell'etichetta della preview
			firstIp.setText(getIpLabel(ips.get(index)));
		}

		//abilito i link "previous" e "next" se necessario
		if(current_page > 0)
			previous.setVisibility(TextView.VISIBLE);
		else
			previous.setVisibility(TextView.GONE);

		if(current_page+1 < num_pages)
			next.setVisibility(TextView.VISIBLE);
		else
			next.setVisibility(TextView.GONE);

		pages.setText("Page " + (current_page+1) + " of " + num_pages);
	}

	//gestisce il comportamento dei link "previous" e "next"
	private void setLink() {

		next.setOnClickListener(new TextView.OnClickListener() {
			public void onClick(View v)
			{
				int page = getCurrent_page();
				showWebViews(page+1);
			}
		});

		previous.setOnClickListener(new TextView.OnClickListener() {
			public void onClick(View v)
			{
				int page = getCurrent_page();
				showWebViews(page-1);
			}
		});
	}

	private String formURL(String ip) {

		return "http://rackbot:rackbot@" + ip + "/mobile.htm";
	}

	private CharSequence getIpLabel(String string) {

		return "ip:" + string;
	}

	public int getCurrent_page() {
		return current_page;
	}

	public void setCurrent_page(int current_page) {
		this.current_page = current_page;
	}

	public BaseActivity getIRB() {
		return gui;
	}

	public void setIRB(BaseActivity iRB) {
		gui = iRB;
	}
}
