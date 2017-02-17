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


public class Main {
	
	private final static Logger LOGGER = Logger.getLogger("Main"); 
	
	//private static final int  tipoPersDb= 1;
	private static final int  tipoBioDb= 2;
	private static final int  tipoDescrDb= 3;
	
	//test1
	
	private static  boolean  storeDb = true;
	private static  boolean  storeFile = true;
	private static  boolean  trunkDb = true;
	private static  boolean  allFile = false;
	
	private static final int contLineaSoglia = 0; //da modificare per riprendere la scansione dopo un "GC Error"
	
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	private static final boolean clean = true;
	
    static public void main(String argv[]) throws InterruptedException, IOException {    
            	
    	 //List<String> listaLinee = new ArrayList<String>();
    	 List<String> listaLineeBio = new ArrayList<String>(); 
    	 String descrizione = new String("");
    	 String finalString = new String("");
    	 String nome_cognome = new String("");
    	 String titolo_nome = new String("");

    	 
    	 String intestazione = 	"################################# Progetto Parser/Scanner XML Wikipedia #############################################################\n\n";
    	 
    	 String[] arrayFile = new String[1];
    	 arrayFile[0] = "fileTest/itwiki-20140213-pages-meta-current1.xml";
    	 //arrayFile[0] = "fileTest/itwiki-20140213-pages-meta-current2.xml"; 
    	 //arrayFile[0] = "fileTest/itwiki-20140213-pages-meta-current3.xml";
    	 //arrayFile[0] = "fileTest/itwiki-20140213-pages-meta-current4.xml";
    	 
    	 BufferedReader in = null;
    	 BufferedReader specialIn = null;
    	 PrintWriter outPersonaggio = null;
    	 PrintWriter outBio = null;
    	 Scanner sc = new Scanner(System.in);

    	 System.out.println(intestazione);
    	 
    	 //connessione al Database
    	 Database db = new Database("wikipedia_db","","root","localhost");
    	 if(db.connect()){
    		 	System.out.println("Connessione al Database ESEGUITA.");    		 	
    	 }
    	 if(trunkDb){
    		 db.trunkDb();
    	 }

    	 long startTime = System.currentTimeMillis();
    	 
    	 int idPersonaggio = -1;
    	 int idAttributo = -1;
    	 //int contPersonaggio = 0;
		 int contBio = 0;
		 int contDescr = 0;

    	 if(storeFile){
			 outPersonaggio = new PrintWriter(new FileWriter("fileTest/outputPersonaggi.txt"));
			 outBio = new PrintWriter(new FileWriter("fileTest/outputBiografie.txt"));
		 
	    	 try{
	    		 
	    		 for(String itemFile : arrayFile){
	    			 
	    			 System.out.println("File XML in input: "+itemFile+", premi un tasto per continuare");
	    			 sc.nextLine();
	
		    		 in = new BufferedReader(new FileReader(itemFile));
	
		    		 String l;
		    		 //boolean isTabella = false;
		    		 boolean isTabellaBio = false;
		    		 
		    		 int contLinea = 0;
		    		 while ( (l = in.readLine()) != null ){
		    			 //System.out.println("contLinea = "+contLinea);
		    			 if(contLinea >= contLineaSoglia){
		    			 	 
		    				 /*
			    			 if(isTabella == false && l.matches("\\{\\{Personaggio.*")){
			    				 isTabella = true;
			    				 listaLinee = new ArrayList<String>();
			    			 }
			    		 	*/
		    				 
			    			 if(isTabellaBio == false && l.matches("\\{\\{Bio.*")){
			    				 isTabellaBio = true;
			    				 listaLineeBio = new ArrayList<String>();
			    			 }
			    		 
			    			 /*
			    			 if(isTabella == true){
			    				 if(l.matches("^\\|[ ]*.*")){
			    					 listaLinee.add(l);
			    				 }
			    			 }
			    			 */
			    			 
			    			 if(isTabellaBio == true){
			    				 if(l.matches("^\\|[ ]*.*")){
			    					 listaLineeBio.add(l);
			    				 }
			    			 }
			    			 
			    			 //controllo tabella "Personaggio"
			    			 /*
			    			 if(isTabella == true && l.matches("^(\\|)?\\}\\}") && listaLinee.size() > 0){
			    				  
			    				  //qualche controllo in più (opzionale)
								  if(getMatchingStrings(listaLinee, "\\|[ ]*nome[ ]*=.*").size() > 0 &&
									 getMatchingStrings(listaLinee, "\\|[ ]*cognome[ ]*=.*").size() > 0
									 ){
									  
											 System.out.println("\n#### Personaggio "+contPersonaggio+": #### \n");
											 if(storeFile){
												 outPersonaggio.println("#### Personaggio "+contPersonaggio+": ####");
											 }
											 
											 if(storeDb){
												//memorizzo il Personaggio e ne ricavo l'ID
												 idPersonaggio = db.insertPersonaggio("Personaggio "+contPersonaggio);
												 System.out.println("newIdPersonaggio = "+idPersonaggio);
											 }
											 
											 //ora posso memorizzare i DATI su DB e su File
											 for (String string : listaLinee) {
												 
												 //memorizzo solo gli attributi valorizzati
												 finalString = customRow(string);
												 String[] finalProperty = finalString.split("=");
												 if(finalProperty.length > 1 && !finalProperty[1].trim().equals("")){
					
													 idAttributo = db.checkExistAttributo(finalProperty[0].trim());
													 if(idAttributo != -1 ){
														 System.out.println(finalString);
													 }
													 
													 if(storeDb){
														 //store su DB
														 if(idAttributo != -1 ){
															 db.insertDato(idPersonaggio, idAttributo, tipoPersDb, finalProperty[1].trim());
															 if(finalProperty[0].trim().equals("nome") || finalProperty[0].trim().equals("cognome")){
																 nome_cognome = nome_cognome + finalProperty[1].trim() + " ";
															 }
														 }
														 // Elenco attributi già Normalizzato
														 // else{
														 //	 idAttributo = db.insertAttributo(finalProperty[0].trim());
														 //	 db.insertDato(idPersonaggio, idAttributo, tipoPersDb, finalProperty[1].trim());
														 //}
														 
													 }
													 if(storeFile){
														 //store su File
														 if(idAttributo != -1 ){
															 outPersonaggio.println(finalString);
														 }
													 }
												 }
												 idAttributo = -1;
											 }
											 
											 //Aggiorna Nome+Cognome del Personaggio
											 db.updateNomeCognomePersonaggio(idPersonaggio, nome_cognome.trim());
											 nome_cognome = "";
											 contPersonaggio++;
											 
								 }
								 
								 isTabella = false;
								 listaLinee = null;   
								 idPersonaggio = -1;
								 idAttributo = -1;
			    			 }
    	 					 */
		    			 
			    			 //controllo tabella "Biografia"
			    			 if(isTabellaBio == true && l.matches(".*\\}\\}") && listaLineeBio.size() > 0){
			    				 
			     				//qualche controllo in più (opzionale)
								 if(getMatchingStrings(listaLineeBio, "\\|[ ]*Nome[ ]*=.*").size() > 0 &&
										 getMatchingStrings(listaLineeBio, "\\|[ ]*Cognome[ ]*=.*").size() > 0 &&
										 getMatchingStrings(listaLineeBio, "\\|[ ]*Sesso[ ]*=.*").size() > 0 &&
										 getMatchingStrings(listaLineeBio, "\\|[ ]*LuogoNascita[ ]*=.*").size() > 0 &&
										 getMatchingStrings(listaLineeBio, "\\|[ ]*GiornoMeseNascita[ ]*=.*").size() > 0 &&
										 getMatchingStrings(listaLineeBio, "\\|[ ]*AnnoNascita[ ]*=.*").size() > 0 &&
										 getMatchingStrings(listaLineeBio, "\\|[ ]*AnnoMorte[ ]*=.*").size() > 0
										 ){
									 												 	
												 System.out.println("\n#### Bio "+contBio+": #### \n");
												 if(storeFile){
													 outBio.println("#### Bio "+contBio+": ####");
												 }
												 
												 if(storeDb){
													//memorizzo la Biografia e ne ricavo l'ID
													idPersonaggio = db.insertPersonaggio("Bio "+contBio);
													System.out.println("newIdBio = "+idPersonaggio);
												 }
												 
												 for (String string : listaLineeBio) {
													 
													 //memorizzo solo gli attributi valorizzati
													 finalString = customRow(string);
													 String[] finalProperty = finalString.split("=");
													 if(finalProperty.length > 1 && !finalProperty[1].trim().equals("")){
						
														 idAttributo = db.checkExistAttributo(finalProperty[0].trim());
														 if(idAttributo != -1 ){
															 System.out.println(finalString);
														 }
														 
														 if(storeDb){
															 //store su DB
															 if(idAttributo != -1 ){
																 db.insertDato(idPersonaggio, idAttributo, tipoBioDb, finalProperty[1].trim());
																 if(finalProperty[0].trim().equals("Nome") || finalProperty[0].trim().equals("Cognome")){
																	 if(finalProperty[1].trim().length() > 0){
																		 nome_cognome = nome_cognome + finalProperty[1].trim() + " ";
																	 }else{
																		 nome_cognome = ""; //non mi serve, tengo conto del Titolo + Nome
																	 }
																 }
																 if(finalProperty[0].trim().equals("Titolo") || finalProperty[0].trim().equals("Nome")){
																	 titolo_nome = titolo_nome + finalProperty[1].trim() + " ";
																 }
															 }
															 // Elenco attributi già Normalizzato
															 //else{
															 // idAttributo = db.insertAttributo(finalProperty[0].trim());
															 // db.insertDato(idPersonaggio, idAttributo, tipoBioDb, finalProperty[1].trim());
															 //}
															 
														 }
														 if(storeFile){
															 //store su File
															 if(idAttributo != -1 ){
																 outBio.println(finalString);
															 }
														 }
													 }
													 idAttributo = -1;
												 }
												 
												 //Aggiorna Nome+Cognome del Personaggio
												 if(!nome_cognome.equals("")){
													 db.updateNomeCognomePersonaggio(idPersonaggio, nome_cognome.trim());
												 }else{
													 db.updateNomeCognomePersonaggio(idPersonaggio, titolo_nome.trim());
													 System.out.println("titolo_nome = "+titolo_nome);
													 System.exit(0);
												 }
												 nome_cognome = "";
												 titolo_nome = "";
												 contBio++;
												 
												 //controllo l'esistenza di una possibile Descrizione associata alla Biografia (deve essere sulle righe successive)
												 specialIn = new BufferedReader(in);
												 int contRowDescr = 0;
												 while((l = specialIn.readLine()) != null){
													 if(contRowDescr == 2){ //può capitare come prima o seconda linea successiva alla Biografia
														 break;
													 }
													 if(l.matches("^[a-zA-Z].*")){
														 descrizione = l;
														 finalString = customRow(descrizione);
														 
														 System.out.println("#### Descr di Bio "+(contBio-1)+": ####");
														 System.out.println(finalString);
														 
														 if(storeFile){
															 //store su File
															 outBio.println("#### Descr di Bio "+(contBio-1)+": ####");
															 outBio.println(finalString+"\n\n");
														 }
					
														 if(storeDb){
															 //store su DB
															 idAttributo = db.checkExistAttributo("altra descrizione");
															 if(idAttributo != -1 ){
																 db.insertDato(idPersonaggio, idAttributo, tipoDescrDb, finalString);
															 }
															 /* Elenco attributi già Normalizzato
															 else{
																 idAttributo = db.insertAttributo("altra descrizione");
																 db.insertDato(idPersonaggio, idAttributo, tipoDescrDb, finalString);
															 }
															 */
														 }			    					
								    					 
								    					 contDescr++;
								    					 break;
													 }
													contRowDescr++;
												 }
									
								 }
			
								 isTabellaBio = false;
								 listaLineeBio = null;
								 idPersonaggio = -1;
								 idAttributo = -1;
			    			 }
		    			 }
			    			 contLinea++;
		    		 }//fine while principale
		    		 
		    		 if(allFile == false){
		    			 break; //quindi solo il primo file
		    		 }
		    		 
	    		 }// fine for
	    		 
	    		 //System.out.println("\n\nTOT dei Possibili Tabelle: "+contPersonaggio);
	    		 System.out.println("TOT dei Possibili Biografie: "+contBio);
	    		 System.out.println("TOT dei Possibili Descrizioni: "+contDescr);
				
	    	}finally{
	    		 if(in != null)
	    			 in.close();
	    		 if(outPersonaggio != null && outBio != null){
	    			 outPersonaggio.close();
	    		 	 outBio.close();
	    		 }
	    		 if(sc != null)
	    			 sc.close();
	    	}
    	 
    	 }
    	 
    	 long endTime = System.currentTimeMillis();
    	 System.out.println("Tempo Esecuzione: " + (endTime - startTime) + " millisecondi => secondi "+(endTime - startTime)/1000);
    	 System.out.println("FINE PROGRAMMA.");
    	 db.disconnect();    	 
    }
    
    private static List<String> getMatchingStrings(List<String> list, String regex) {

    	  ArrayList<String> matches = new ArrayList<String>();
    	  Pattern p = Pattern.compile(regex);

    	  for (String s:list) {
    	    if (p.matcher(s).matches()) {
    	      matches.add(s);
    	    }
    	  }

    	  return matches;
    }
    
    private static String customRow(String row) {

  	  	String finalString = new String("");
  	  	
  	  	//NORMALIZZO la riga
  	  	
	  	  	//sostituisco gli apici ''' e poi '' con "
		  	finalString = row.replaceAll("\'\'\'","\'").replaceAll("\'\'", "\'");
		  	
		  	//scelgo la PRIMA OPZIONE tra gli OR all'interno delle parentesi quadre
			 if(finalString.matches(".*\\[\\[.*\\]\\].*")){
				 finalString = finalString.replaceAll("(\\[\\[)([^\\[\\]]*)(\\|)([^\\[\\]]*)(\\]\\])", "$2");
				 finalString = finalString.replaceAll("(\\[\\[)([^\\[\\]]*)(\\]\\])", "$2");
			 }
			 
			 //elimino |
			 finalString = finalString.replaceAll("\\|","");
			 
			 //elimino {{....}}
		     finalString = finalString.replaceAll("\\{\\{.*\\}\\}", "");
		     
		     //elimino &....;
		     finalString = finalString.replaceAll("\\&.*;", "");

  	  return finalString;
  }
    
    @SuppressWarnings("unused")
	private static String cleaner(String row) {

  	  	String finalString = new String("");
  	  	
		     if(clean){
			     //converto caratteri Speciali Conosciuti
			     finalString = finalString.replaceAll("Ã", "à");
			     
				 //convesione caratteri Speciali http://utf8-chartable.de/unicode-utf8-table.pl?utf8=char
				 finalString = decodeUTF8(finalString.getBytes());
		     }

  	  return finalString;
  }
    
    
    //procedure per conversioni
    @SuppressWarnings("unused")
	private static String removeDiacriticalMarks(String string) { //NON USARE
        return Normalizer.normalize(string, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    
    @SuppressWarnings("unused")
	private static String removeASCII(String string) { //NON USARE
        return Normalizer.normalize(string, Form.NFD)
            .replaceAll("[^\\p{ASCII}]", "");
    }
    
    @SuppressWarnings("unused")
	private static String removeLatin(String string) { //NON USARE
        return Normalizer.normalize(string, Form.NFD)
            .replaceAll("[^\\p{InBasic_Latin}]", "");
    }
    
    static String decodeUTF8(byte[] bytes) {  //NON USARE
        return new String(bytes, UTF8_CHARSET);
    }
    static byte[] encodeUTF8(String string) {
        return string.getBytes(UTF8_CHARSET);
    }
    
}