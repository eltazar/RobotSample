package netInterface;

import java.io.*;

/** Rappresenta la classe per la ricezione dei pacchetti su un InputStream al quale è agganciato un DataInputStream. 
 */
public class MessageInputStream {

    private DataInputStream dataIn;
    private InputStream in;

    /**Crea il MessageInputStream associando ad un'InputStream un DataInputStream
    @param  in
     */
    public MessageInputStream(InputStream in) {
	this.in = in;
        //associo all'input stram un data input stream
        dataIn = new DataInputStream(in);

    }

    /** Riceve un pacchetto e ne estrapola l'informazione, il metodo è sincronizzato sullo stream di input.
    @return messaggio
    @throws IOException
     */
    public String receive() throws IOException {

        int dim; //dim del messaggio che si sta per ricevere
        int readDim = 0; //byte letti fino ad ora
        byte[] buffer; //buffer dello stream di input per poter contenere il messaggio da leggere

        //sincronizzazione sull'input stream per acquisirne il lock
        synchronized (in) {
            dim = dataIn.readInt(); //legge la dimensione del messaggio dallo stream
            if (dim == -1) {
                return null;
            }
            buffer = new byte[dim];	//crea buffer
            while (readDim < dim) {    //finchè non ha letto tutti i bytes dallo stream continua a leggere
                readDim += dataIn.read(buffer, readDim, dim - readDim); //legge dallo stream dataIn e salva nel buffer.
            }
        }

        //ritorna il messaggio contenuto nel buffer e convertito in string
        return new String(buffer, "UTF-16");

    }

    /**Chiude lo stream di input*/
    public void closeInput() throws java.io.IOException {
        dataIn.close();
    }
}
