package net.appuntivari.wikipedia;


import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
/**
 * Classe che rappresenta una connessione al database. 
 * Contiene i metodi per eseguire query di consultazione o di aggiornamento.
 * Contiene inoltre i metodi per connettersi/disconnettersi al database.
 * 
 */
public class Database {

	/*test*/
	private String DBname;
	private String passwd;
	private String user;
	private String host;
	private boolean connected;
	private Connection conn;
	
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	/**
	 * Costruttore di classe
	 * 
	 * @param DBname	il nome del database
	 * @param passwd	la password di accesso
	 * @param user		il nome utente
	 * @param host		l'indirizzo del server
	 */
	public Database(String DBname, String passwd, String user,String host)
	{
		this.DBname = DBname;
		this.passwd = passwd;
		this.user = user;
		this.host = host;
		connected = false;
	
}

/**
 * Permette di connettersi al database selezionato
 * 
 * @return	true se la connessione ha successo
 */
public boolean connect() {
   connected = false;
   try {

      Class.forName("com.mysql.jdbc.Driver");

      if (!DBname.equals("")) {
         if (user.equals("")) {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/" + DBname + "/?characterEncoding=utf8");
         } else {

            if (passwd.equals("")) {
               conn = DriverManager.getConnection("jdbc:mysql://localhost/" + DBname + "?characterEncoding=utf8&user=" + user);
            } else {
               conn = DriverManager.getConnection("jdbc:mysql://localhost/" + DBname + "?characterEncoding=utf8&user=" + user + "&password=" + passwd);
            }
         }
         connected = true;
      } else {
         System.out.println("Manca il nome del database!!");
         System.out.println("Scrivere il nome del database da utilizzare all'interno del file \"config.xml\"");
         System.exit(0);
      }
   } catch (Exception e) {e.getMessage(); }
   return connected;
}

public int checkExistAttributo(String attributo){
    try {
    	Statement stmt = conn.createStatement();    
       	ResultSet rs = stmt.executeQuery("SELECT id FROM attributi WHERE attributo = \""+attributo+"\"");   
       
		 while (rs.next()) {
			 int id = Integer.parseInt(rs.getString("id"));
			 return id;
		 }
			 
    }catch (Exception e) { 
    	e.printStackTrace(); 
    	e.getMessage(); 
     }   
    
    return -1;
}

public int checkExistPersonaggio(String nome){
    try {
    	Statement stmt = conn.createStatement();    
       	ResultSet rs = stmt.executeQuery("SELECT id FROM personaggi WHERE nome = \""+nome+"\"");   
       
		 while (rs.next()) {
			 int id = Integer.parseInt(rs.getString("id"));
			 return id;
		 }
			 
    }catch (Exception e) { 
    	e.printStackTrace(); 
    	e.getMessage(); 
     }   
    
    return -1;
}

public int insertAttributo(String attributo){
	try {
      	int key = executeUpdateGetId("INSERT into attributi (attributo) VALUES(\""+attributo+"\")");  
      	return key;
	 } catch (Exception e) { 
    	e.printStackTrace(); 
    	e.getMessage(); 
     }   
	
	return -1;
}
public int insertPersonaggio(String nome){
	try {
		int key = executeUpdateGetId("INSERT into personaggi (nome) VALUES(\""+nome+"\")");  
		return key;
	 } catch (Exception e) { 
    	e.printStackTrace(); 
    	e.getMessage(); 
     }   
	
	return -1;
}

public void updateNomeCognomePersonaggio(int id_personaggio, String nome_cognome){
	try {
		executeUpdate("UPDATE personaggi SET nome=\""+nome_cognome+"\" WHERE id="+id_personaggio);  
	 } catch (Exception e) { 
    	e.printStackTrace(); 
    	e.getMessage(); 
     }   
	
}

public int insertDato(int id_personaggio, int id_attributo, int id_tipo, String dato){
	try {
		int key = executeUpdateGetId("INSERT into dati (id_personaggio, id_attributo, dato, id_tipo) VALUES("+id_personaggio+", "+id_attributo+" ,  \""+dato+"\", "+id_tipo+")");  
      	return key;
	 } catch (Exception e) { 
    	e.printStackTrace(); 
    	e.getMessage(); 
     }   
	
	return -1;
}



public void trunkDb(){
	executeUpdate("DELETE FROM dati where id>0");
	executeUpdate("DELETE FROM personaggi where id>0");
	
	/* Nota: non cancellare gli attributi.
	 * L'elenco è stato normalizzato. Nella tabella ci sono tutti quelli Utili.
	executeUpdate("DELETE FROM attributi where id>0");
	*/
}

public void cleanRelazioni(){
	executeUpdate("DELETE FROM relazioni where id>0");
}

/**
 * Esegue altre Query ad-hoc
 * 
 */

public List<String> getNomiCognomi(String escludiCorrente){
	
	 List<String> listaPersonaggiBio = new ArrayList<String>();
	
	 Statement stmt;
	 ResultSet rs;
	 String query = null;
	 
	try {
		stmt = conn.createStatement();

		 query = "SELECT NOME FROM personaggi WHERE nome NOT LIKE \""+escludiCorrente+"\"";
		 String queryEnc = new String(query.getBytes("UTF-8"), "UTF-8");
		 rs = stmt.executeQuery(queryEnc);
		 
		 while (rs.next()) {
    		  listaPersonaggiBio.add(new String(rs.getString("NOME")));
		 }

	} catch (Exception e) {
		e.printStackTrace();
	}    

	System.out.println("getNomiCognomi size "+listaPersonaggiBio.size());
	return listaPersonaggiBio;
}

public List<String> getNomiCognomi(){
	
	 List<String> listaPersonaggiBio = new ArrayList<String>();
	
	 Statement stmt;
	 ResultSet rs;
	 String query = null;
	 
	try {
		stmt = conn.createStatement();

		 query = "SELECT NOME FROM personaggi";
		 String queryEnc = new String(query.getBytes("UTF-8"), "UTF-8");

		 rs = stmt.executeQuery(queryEnc);
		 while (rs.next()) {
   		  listaPersonaggiBio.add(new String(rs.getString("NOME")));
		 }

	} catch (Exception e) {
		e.printStackTrace();
	}    

	System.out.println("getNomiCognomi size "+listaPersonaggiBio.size());
	return listaPersonaggiBio;
}

public int checkInDescrizioni(String nomeCogn){
	List<String> listaPersonaggiBio = new ArrayList<String>();
	
	 Statement stmt;
	 ResultSet rs;
	 String query = null;
	 int conteggio = 0;
	 int id_pers = 0;
	 
	try {
		stmt = conn.createStatement();

		 query = "SELECT ID_PERSONAGGIO FROM dati WHERE id_attributo=21 and dato LIKE \"%"+nomeCogn+"%\" ";
		 rs = stmt.executeQuery(query);
		 while (rs.next()) {
			 id_pers = rs.getInt("ID_PERSONAGGIO");
			 System.out.print("personaggio correlato => ");
			 printPersonaggio(id_pers);
			 conteggio++;
		 }

	} catch (SQLException e) {
		e.printStackTrace();
	}    
	return conteggio;
}

public void printPersonaggio(int id_personaggio){
	List<String> listaPersonaggiBio = new ArrayList<String>();
	
	 Statement stmt;
	 ResultSet rs;
	 String query = null;
	 int conteggio = 0;
	 
	try {
		stmt = conn.createStatement();

		 query = "SELECT nome FROM PERSONAGGI WHERE id="+id_personaggio;
		 String queryEnc = new String(query.getBytes("UTF-8"), "UTF-8");
		 
		 rs = stmt.executeQuery(queryEnc);
		 while (rs.next()) {
			 System.out.println(rs.getString("nome"));
		 }

	} catch (Exception e) {
		e.printStackTrace();
	}    

}

/**
 * Esegue una query di aggiornamento del database
 */
public void executeUpdate(String query) {
   int num = 0;
   try {
	   
		 String queryEnc = new String(query.getBytes(), "UTF-8");
      Statement stmt = conn.createStatement();
      num = stmt.executeUpdate(queryEnc);
      stmt.close();
   } catch (Exception e) {
      e.printStackTrace();
      e.getMessage();
   }
}

public int executeUpdateGetId(String query) {
	PreparedStatement pstmt;  
	int key = 0;  
	try {  
		
		 String queryEnc = new String(query.getBytes(), "UTF-8");
		 
		 
	pstmt = conn.prepareStatement(queryEnc, Statement.RETURN_GENERATED_KEYS);  

	pstmt.executeUpdate();  
	ResultSet keys = pstmt.getGeneratedKeys();  
	  
	keys.next();  
	key = keys.getInt(1);  
	keys.close();  
	pstmt.close();  

	} catch (Exception e) { e.printStackTrace(); }  
	return key;  
}

/**
 * Esegue la disconnessione dal database
 */
public void disconnect() {
   try {
      conn.close();
      connected = false;
   } catch (Exception e) { e.printStackTrace(); }
}

/**
 * Ottiene l'oggetto Connection associato a questo database
 */
public Connection getConnection()
{	
	return conn;
}

/**
 * Indica se la connessione se è attiva
 */
public boolean isConnected()
{
return this.connected;	
}

}
