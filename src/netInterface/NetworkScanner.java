package netInterface;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.lang.Runnable;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkScanner{

	private static final int NB_THREADS = 10;
	private static final String LOG_TAG = "NetworkScanner";
	private String gatewayAddress;
	private Context mContext;
	private ArrayList<String> ipScanned;

	public NetworkScanner(String gatewayAddress){
		this.gatewayAddress = gatewayAddress;
	}
	
	/*
	 * 
	 * */
	public NetworkScanner(Activity act) {
		// TODO Auto-generated constructor stub
		 mContext = act;
		 ipScanned = new ArrayList<String>();
	}

	/*
	 * Effettua una scansione multithread della subnet al quale siamo connessi
	 * */
	public void doScan() {
	    Log.i(LOG_TAG, "Start scanning");

	    ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);
	    for(int dest=1; dest<255; dest++) {
	        String host = "192.168.0." + dest;
	        executor.execute(pingRunnable(host));
	    }

	    Log.i(LOG_TAG, "Waiting for executor to terminate...");
	    executor.shutdown();
	    try { executor.awaitTermination(60*1000, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) { }

	    Log.i(LOG_TAG, "Scan finished");
	    System.out.println("Ip raggiungibili \n = "+ipScanned);
	}

	private Runnable pingRunnable(final String host) {
		//Log.i(LOG_TAG,"dentro ping runnable addr = "+host);
	    return new Runnable() {
	        public void run() {
	            Log.i(LOG_TAG, "Pinging " + host + "...");
	            try {
	                InetAddress inet = InetAddress.getByName(host);
	                boolean reachable = inet.isReachable(1000);
	                Log.i(LOG_TAG, "=> Result: " + (reachable ? "reachable" : "not reachable"));
	                if(reachable){
	                	ipScanned.add(host);
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
	 * Ritorna le informazioni relative all'hotspot al quale il device  connesso
	 * */
	public void getInfoWifiConnection(){
		
		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

		// Get WiFi status
		WifiInfo info = wifi.getConnectionInfo();
		DhcpInfo dhcpInfo = wifi.getDhcpInfo();
		System.out.println("DHCP gateway = "+intToIpAddress(dhcpInfo.gateway));
		int ip = info.getIpAddress();
		
		
		System.out.println("WIFI ADDRESS = "+ intToIpAddress(ip)+"bssid "+info.getMacAddress()+"ssid"+info.getSSID());
	}
	
	/*
	 * Ritorna un indirizzo ip formattato data la sua notazione decimale
	 * */
	private String intToIpAddress(int ip){
		
		String ipString = String.format("%d.%d.%d.%d",(ip & 0xff),(ip >> 8 & 0xff),(ip >> 16 & 0xff),(ip >> 24 & 0xff));
		
		return ipString;
	}

}
