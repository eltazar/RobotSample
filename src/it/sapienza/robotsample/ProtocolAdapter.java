package it.sapienza.robotsample;


import java.io.IOException;


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

		//System.out.println("Protocol adapter.sendMessage");
		String answer = "no answer";
		try{
			messageStream.sendMessageAsUTF8(message);

			//se il messaggio � di connessione aspetto risposta
			if(message.equals("#CNT0\r")){
				System.out.println("In attesa di ack dal robot");
				answer = messageStream.receiveMessage();
			}

			System.out.println("Protocol adapter: answer = "+answer);
		}
		catch (NullPointerException ex){
			System.out.println("Protocol adapter eccezione = "+ex.getLocalizedMessage());
			answer = "Devi connetterti!";
		}
		return answer;
	}

	public void sendMessage(float pitch, float roll) throws IOException{

		//System.out.println("Protocol adapter.sendMessage");
		try{
			//System.out.println("pitch = " + (int)(pitch*128) + " roll = " + (int)(roll*128));
			messageStream.sendMessageAsUTF8("#SPD0" + (int)(pitch*128) +"\r");
			messageStream.sendMessageAsUTF8("#TRN0" + (int)(roll*128) + "\r");

		}
		catch (NullPointerException ex){
			System.out.println("Protocol adapter eccezione = "+ex.getLocalizedMessage());
		}
		return;
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

	public Boolean isThereAvaiableStream(){

		if(messageStream == null)
			return false;
		else return true;
	}
	
	public void closeCommunication() throws IOException{
		messageStream.getMis().closeInput();
		messageStream.getMos().closeOutput();
		messageStream.close();
	}
}
