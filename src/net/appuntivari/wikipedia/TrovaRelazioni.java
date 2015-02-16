package net.appuntivari.wikipedia;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.invoke.ConstantCallSite;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class TrovaRelazioni {
	
	private final static Logger LOGGER = Logger.getLogger("TrovaRelazioni"); 
	
	
	private static  boolean  cleanRelazioni = false;
	private static  boolean  relazioni = true;
		
    static public void main(String argv[]) throws InterruptedException, IOException {    
    	 
    	 String intestazione = 	"################################# Progetto Parser/Scanner XML Wikipedia #############################################################\n\n";
    	 
    	 System.out.println(intestazione);
    	 
    	 //connessione al Database
    	 Database db = new Database("wikipedia_db","","root","localhost");
    	 if(db.connect()){
    		 	System.out.println("Connessione al Database ESEGUITA.");    		 	
    	 }
    	 if(cleanRelazioni){
    		 db.cleanRelazioni();
    	 }

    	 long startTime = System.currentTimeMillis();

		 if(relazioni){
			 int cont = 0;
			 System.out.println("Relazioni delle sole Biografie");
			 
			 //tiro fuori Nome+Cognome corrente
			
			 List<String> listaPersonaggi = db.getNomiCognomi();
			 for (String nomeCogn : listaPersonaggi) {
				System.out.println(nomeCogn + " in attr. DESCRIZIONE dei seguenti personaggi =====> ");
				cont = db.checkInDescrizioni(nomeCogn);
				System.out.println("=====>"+cont+"\n\n");
			}
			 
		 }    	 
    	 
    	 long endTime = System.currentTimeMillis();
    	 System.out.println("Tempo Esecuzione: " + (endTime - startTime) + " millisecondi => secondi "+(endTime - startTime)/1000);
    	 System.out.println("FINE PROGRAMMA.");
    	 db.disconnect();    	 
    }

}