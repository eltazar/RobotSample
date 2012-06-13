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
	/*public String _receive() throws IOException {

		int dim; //dim del messaggio che si sta per ricevere
		int readDim = 0; //byte letti fino ad ora
		byte[] buffer; //buffer dello stream di input per poter contenere il messaggio da leggere

		//System.out.println("DIMENSIONE RICEVUTA = "+dataIn.readInt());

		//sincronizzazione sull'input stream per acquisirne il lock
		synchronized (in) {
			System.out.println("DENTRO SYNCRONIZED");
			//dim = dataIn.readInt(); //legge la dimensione del messaggio dallo stream
			//if (dim == -1) {
			//     return null;
			//}
			buffer = new byte[12];	//crea buffer

			for (int i = 0; buffer[i] != -1; i++) {    //finchè non ha letto tutti i bytes dallo stream continua a leggere
				byte b = dataIn.readByte(); //legge dallo stream dataIn e salva nel buffer.
				buffer[i] = b;
				System.out.println((char)b);
				if(b=='\r'){
					break;}
			}
		}

		//ritorna il messaggio contenuto nel buffer e convertito in string
		return new String(buffer, "UTF-8");

	}*/

	public String receive() throws IOException{

		byte[] buffer = new byte [12];

		synchronized(in){

			for(int i = 0; i < buffer.length; i++){
				//leggo byte per byte
				byte b = dataIn.readByte();
				//salvo i-esimo byte nel buffer
				buffer[i] = b;
				//System.out.println((char)b); //debug
				
				if(b == '\r'){
					//se b � carattere di terminazione fermo ciclo
					break;
				}
			}
		}

		return new String(buffer, "UTF-8");
	}

	/**Chiude lo stream di input*/
	public void closeInput() throws java.io.IOException {
		dataIn.close();
	}
}
