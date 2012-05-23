package it.sapienza.robotsample;


import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


/**Classe singleton.
 * Si occupa di codificare, inviare e ricevere le informazioni sul canale lato client, per farlo usa i metodi
 * di MessageIOStream associato al clientMain
 */
public class ProtocolAdapter {

    private netInterface.MessageIOStream messageStream = null;
    private static ProtocolAdapter pAdpt = null;
    
    /**Istanzia un Protocol Adapter dato un MessageIOStream
     @param messageStream
    */
    private ProtocolAdapter(netInterface.MessageIOStream messageStream) {
        this.messageStream = messageStream;
    }

    private ProtocolAdapter(){
    	
    }
    
    public String sendMessage(String message) throws IOException{
    	
    	System.out.println("Protocol adapter.sendMessage");
    	String answer = "";
    	try{
    		messageStream.sendMessageAsUTF8(message);
    		answer = messageStream.receiveMessage();
           
    		System.out.println("Protocol adapter answer = "+answer);
    	}
    	catch (NullPointerException ex){
    		System.out.println("Protocol adapter eccezione = "+ex.getLocalizedMessage());
    		answer = "Devi connetterti!";
    	}
        return answer;
    }
    
    public static ProtocolAdapter getInstance(){
    	if(pAdpt == null){
    		pAdpt = new ProtocolAdapter();
    	}
    	return pAdpt;
    }
    
    //associa al singleton un socket con stream
    public void setProtocolAdapter(netInterface.MessageIOStream mIos){
    	this.messageStream = mIos;
    }
   
    public netInterface.MessageIOStream  getAssociatedStream(){
    	return messageStream;
    }
    
    public Boolean isValidInstance(){
    	
    	if(messageStream == null)
    		return false;
    	else return true;
    }
}
