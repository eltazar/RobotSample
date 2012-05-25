package netInterface;

import java.io.*;

/**
 * Rappresenta la classe per la creazione e l'inivio di un pacchetto sull'OutputStream (al quale è agganciato un DataOutputStream).
 */
public class MessageOutputStream extends DataOutputStream {

    private ByteArrayOutputStream buffer; //qui vengono scritti i dati dalle varie write
    private DataOutputStream dataOut;
    private OutputStream out;

    /**
     * Istanzia un MessageOutputStream.
     * @param out
     */
    public MessageOutputStream(OutputStream out) {
        //passo al costruttore un buffer, i dati verranno scritti in tale buffer
        super(new ByteArrayOutputStream());
        //salvo outputStream associato a un certo socket
        this.out = out;
        //ottengo la reference del ByteArrayOutputSream instanziato sopra, essa è salvata nella variabile out della classe madre
        buffer = (ByteArrayOutputStream) super.out;
        //attacco all'output stream out, un DataOutputStream
        dataOut = new DataOutputStream(out);

    }
     
    /**
     * Incapsula il messaggio in pacchetti formati dai dati più la loro informazione di controllo (in questo caso la dimensione in byte).
     * @throws IOException
     */
    public void send() throws IOException {
    	
    	//System.out.println("dentro message output stream");
    	//System.out.println("buffer contiene in utf-16 = "+new String(buffer.toByteArray(),Charset.forName("UTF-16")));
    	//System.out.println("buffer contiene in utf-8 = "+new String(buffer.toByteArray(),Charset.forName("UTF-8")));

        //ottiene il lock sull'output stream
        synchronized (out) {
            //scrive sull'OutputStream la dimensione del buffer, ovvero del messaggio
            dataOut.writeInt(buffer.size());
            //scrive dal buffer allo stream di output
            buffer.writeTo(dataOut);
        }
        buffer.reset();
         //invia subito il pacchetto
        dataOut.flush();
    }

    /**Chiude lo stream di output*/
     public void closeOutput() throws java.io.IOException {
        dataOut.close();
    }
}
