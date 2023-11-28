package it.betacom.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class DBHandler {
	private final static Logger logger = LogManager.getLogger(DBHandler.class);
	
	public static DBHandler instance;
	
	private String url,user,pass,csv_path,pdf_path;
	private String db_path =  "jdbc:mysql://localhost:3306/";
	private Connection con = null;
	
	
	public static DBHandler getInstance() {
		if (instance == null) {
			instance = new DBHandler();
		}
		return instance;
	}
	
//	creates connection to the databse
	private DBHandler() {		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e1) {e1.printStackTrace();
		}		
		getConfig();
		
		try {
			con = DriverManager.getConnection(url,user,pass);
			logger.info("Connessione riuscita");
		} catch (SQLException e) {e.printStackTrace();
		}
	}
	
//	read config file and set variables values
	private void getConfig() {
		Properties properties = new Properties();
		String propertiesFile = "./resources/config.properties";
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propertiesFile);
			properties.load(inputStream);
			String db = properties.getProperty("db");
			this.url = this.db_path+db;
			this.user = properties.getProperty("user");
			this.pass = properties.getProperty("pass");
			this.csv_path = properties.getProperty("csv_path");
			this.pdf_path = properties.getProperty("pdf_path")+LocalDate.now()+".pdf";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
//	creates the tables
	public void init() {
		Statement stm = null;
		try {
//			tabella partecipanti
			stm = con.createStatement();
			String create_partecipanti = "CREATE TABLE partecipanti (" 
					+ "id INTEGER AUTO_INCREMENT NOT NULL," 
					+ "nome VARCHAR(255),"
					+ "sede VARCHAR (255)," 
					+ "PRIMARY KEY(id)" 
					+ ");";
			stm.executeUpdate(create_partecipanti);
			logger.info("Creato tabella partecipanti");
			
//			inserimento dati
			readCSV();
			
//			tabella estrazioni
			stm = con.createStatement();
			String create_estrazioni = "CREATE TABLE estrazioni (" 
					+ "id INTEGER AUTO_INCREMENT NOT NULL," 
					+ "nome VARCHAR(255),"
					+ "sede VARCHAR(255),"
					+ "data TIMESTAMP,"
					+ "PRIMARY KEY(id)" 
					+ ");";
			stm.executeUpdate(create_estrazioni);
			logger.info("Creato tabella estrazioni");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
//	pick one person at random 
	public void estrazione() {
		Random rand = new Random();

		int id = 1 + rand.nextInt(26);

		Statement stm = null;

		String select = "SELECT * FROM partecipanti WHERE id="+id+";";
		
		try {
			stm = con.createStatement();
			ResultSet rs = stm.executeQuery(select);
			rs.next();
			System.out.println(rs.getString("nome")+" - "+rs.getString("sede"));
			String insert = "INSERT INTO estrazioni (nome,sede,data) VALUES ('"
					+ rs.getString("nome")+"','"
					+ rs.getString("sede")+"',"
					+ "NOW());";
			stm.executeUpdate(insert);
			logger.info("Inserito estrazione");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void readCSV() {
		Scanner sc;
		Statement stm = null;
		String sql;
		try {
			sc = new Scanner(new File(csv_path));
			sc.useDelimiter(";"); 
			while(sc.hasNext()) {
				sql = "INSERT INTO partecipanti (nome,sede) VALUES ('"
								+ sc.nextLine().replace(";","','")
								+ "');";
				stm = con.createStatement();
				stm.executeUpdate(sql);
				logger.info("Inserito partecipanti");
			}
			
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (SQLException e) {e.printStackTrace();}  
		 
	}
	
	public void printPDF() {
		Statement stm = null;
		Document document = new Document();
		String recap = "SELECT COUNT(id) as Counter,nome,sede FROM estrazioni GROUP BY nome,sede  ORDER BY Counter DESC";

		try {
			
			OutputStream outputStream = new FileOutputStream(new File(pdf_path));
			PdfWriter.getInstance(document, outputStream);
			document.open();
			Font font = new Font(Font.FontFamily.COURIER, 12, Font.BOLD);
			
			stm = con.createStatement();			
			ResultSet rs = stm.executeQuery(recap);
			
			while(rs.next()) {
//				System.out.println(rs.getString("nome")+ " - " + rs.getString("Counter"));
				String result = rs.getString("Counter") + " - " + rs.getString("nome");
				Paragraph paragrafo = new Paragraph();
				paragrafo.setFont(font);
				paragrafo.add(result);
				document.add(paragrafo);
			}
			
			logger.info("Stampato file pdf");

			document.close();				
			
		} catch (SQLException e) {e.printStackTrace();
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (DocumentException e) {e.printStackTrace();
		}
	}
	
	
	public void reset() {
		Statement stm = null;
		
		String drop_partecipanti = "DROP TABLE IF EXISTS partecipanti";
		String drop_estrazione = "DROP TABLE IF EXISTS estrazioni";

		try {
			stm = con.createStatement();
			stm.executeUpdate(drop_partecipanti);
			logger.info("Eliminato tabella partecipanti");
			stm.executeUpdate(drop_estrazione);
			logger.info("Eliminato tabella estrazioni");
		} catch (SQLException e) {e.printStackTrace();}
	}
	

}
