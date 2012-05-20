package netInterface;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
/**Crea un socket ed una connessione verso un altro host; ad esso sono associati un MessageInputStream ed un MessageOutputStream
per la ricezione e l'invio di messaggi sul canale di trasmissione.
 */
public class MessageIOStream extends Socket {

    private MessageInputStream inStr;
    private MessageOutputStream outStr;

    /**Istanzia un MessageIoStream
    @throws UnknownHostException
    @throws IOException*/
    public MessageIOStream(InetAddress address)
            throws java.net.UnknownHostException, java.io.IOException {

        super(address, 15000); //indirizzo e porta del server

        inStr = new MessageInputStream(super.getInputStream());
        outStr = new MessageOutputStream(super.getOutputStream());
    }
    
    public MessageIOStream(InetAddress address,int port)
            throws java.net.UnknownHostException, java.io.IOException {

        super(address, port); //indirizzo e porta del server

        inStr = new MessageInputStream(super.getInputStream());
        outStr = new MessageOutputStream(super.getOutputStream());
    }

    /**Istanzia un MessageIoStream
    @throws UnknownHostException
    @throws IOException*/
    public MessageIOStream(Socket socket)
            throws java.io.IOException {
        inStr = new MessageInputStream(socket.getInputStream());
        outStr = new MessageOutputStream(socket.getOutputStream());
    }

    /**Riceve un messaggio dallo stream di input
    @return messaggio
    @throws IOException*/
    public String receiveMessage() throws IOException {

        return inStr.receive();
    }

    /**Invia il messaggio sullo stream di output
    @param  messaggio
    @throws IOException*/
    public void sendMessage(String mex) throws IOException {

        outStr.writeChars(mex); //scrive stringa sul messageOutputStream, in particolare nel suo buffer interno
        outStr.send(); //lo invia sullo output stream vero

    } 


    //get
    public MessageInputStream getMis(){
        return inStr;
    }

    public MessageOutputStream getMos(){
        return outStr;
    }
}
