package netInterface;


import it.sapienza.robotsample.ProtocolAdapter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.lang.Runnable;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class NetworkUtility{

	private static final int NB_THREADS = 5;
	private static final String LOG_TAG = "NetworkScanner";
	private Context mContext;
	private ArrayList<String> ipScanned;
	private Handler handler;
	
	private static NetworkUtility netUtil = null;
	
	/*
	 * 
	 * */
	
	public static NetworkUtility getInstance(){
		if(netUtil == null){
			netUtil = new NetworkUtility();
		}
		return netUtil;
	}
	
	private NetworkUtility(){
		ipScanned = new ArrayList<String>();
		
	}
/*	public NetworkUtility(Activity act) {
		mContext = act;
		ipScanned = new ArrayList<String>();
	}

	//costruttore al quale passo un handler per la progress bar
	public NetworkUtility(Handler handler, Activity act) {
		mContext = act;
		this.handler = handler;
		ipScanned = new ArrayList<String>();
	}
*/
	
	public void setContext(Context ac){
		mContext = ac;
	}
	
	public void setHandler(Handler h){
		handler = h;
	}
	
	/*
	 * Effettua una scansione multithread della subnet al quale siamo connessi
	 * */
	public ArrayList<String> doScan() {
	    Log.i(LOG_TAG, "Start scanning");

	    //trovo indirizzo gateway
	    String gtw = getPreGatewayString();
	    
	    //rimuovo precedenti risultati
	    ipScanned.clear();
	    
	    //creo pool di n thread
	    ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);
	    for(int dest=2; dest<255; dest++) {
	    	//costruisco host ip
	        String host = gtw+"."+dest;
	        executor.execute(pingRunnable(host));
	    }

	    Log.i(LOG_TAG, "Waiting for executor to terminate...");
	    executor.shutdown();
	    try { 
	    	executor.awaitTermination(60*1000, TimeUnit.MILLISECONDS); 
	    }
	    catch (InterruptedException ignored) { }

	    Log.i(LOG_TAG, "Scan finished");
	    System.out.println("Ip raggiungibili \n = "+ipScanned);
	    
	    return ipScanned;
	}

	private Runnable pingRunnable(final String host) {
		//Log.i(LOG_TAG,"dentro ping runnable addr = "+host);
	    return new Runnable() {
	        public void run() {
	            Log.i(LOG_TAG, "Pinging " + host + "...");
	            try {
	                InetAddress inet = InetAddress.getByName(host);
	                boolean reachable = inet.isReachable(1200);
	                Log.i(LOG_TAG, "=> Result: " + (reachable ? "reachable" : "not reachable"));
	                if(reachable){
	                	ipScanned.add(host);
	                }	                	
	                if(handler != null){
	                	//se handler esiste invio messaggio di incremento di 1 perch� un host � stato scansionato
	                	handler.sendEmptyMessage(1);
	                }
	                
	            } catch (UnknownHostException e) {
	                Log.e(LOG_TAG, "Not found", e);
	            } catch (IOException e) {
	                Log.e(LOG_TAG, "IO Error", e);
	            }
	        }
	    };
	}
	
	/*
	 * Ritorna le informazioni relative all'hotspot al quale il device � connesso
	 * */
	public void getInfoWifiConnection(){
		
		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

		// Get WiFi status
		WifiInfo info = wifi.getConnectionInfo();
		DhcpInfo dhcpInfo = wifi.getDhcpInfo();
				
		System.out.println("STRINGA FORMATTATA GATEWAY = "+intToIpAddress(dhcpInfo.gateway));
		
		System.out.println("DHCP gateway = "+intToIpAddress(dhcpInfo.gateway));
		int ip = info.getIpAddress();
		
		
		System.out.println("WIFI ADDRESS = "+ intToIpAddress(ip)+"bssid "+info.getMacAddress()+"ssid"+info.getSSID());
		System.out.println(info);
	}
	
	/*
	 * Ritorna un indirizzo ip formattato data la sua notazione decimale
	 * */
	private String intToIpAddress(int ip){
		
		String ipString = String.format("%d.%d.%d.%d",(ip & 0xff),(ip >> 8 & 0xff),(ip >> 16 & 0xff),(ip >> 24 & 0xff));
		
		return ipString;
	}

	//ipotizzando che la maschera sia 255.255.255.0 ritorna la stringa rappresentante
	//i primi 3 byte dell'indirizzo ip del gateway
	private String getPreGatewayString(){
		
		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifi.getDhcpInfo();
		
		StringTokenizer strTok = new StringTokenizer(intToIpAddress(dhcpInfo.gateway),".");
		ArrayList<String> gtwTok = new ArrayList<String>();
		while (strTok.hasMoreTokens()) {
	         gtwTok.add(strTok.nextToken());
	     }
		
		return gtwTok.get(0)+"."+gtwTok.get(1)+"."+gtwTok.get(2);
	}
	
	public ArrayList<String> getIpAddresses(){
		return ipScanned;
	}
	
}
