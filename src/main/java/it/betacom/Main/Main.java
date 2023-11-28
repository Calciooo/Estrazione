package it.betacom.Main;

import java.util.Scanner;

import it.betacom.Connection.DBHandler;

public class Main {

	public static void main(String[] args) {
//		app();
		DBHandler c = DBHandler.getInstance();
		c.reset();
		c.init();
		c.readCSV();
		for (int i=0; i<80; i++) {
			c.estrazione();
		}
		c.printPDF();
	}
	
	
	public static void app() {
		
	Scanner scan = new Scanner(System.in);
	DBHandler c = DBHandler.getInstance();
	System.out.println("---------------------------------------------------"
			+ "\nScegliere operazione da eseguire"
			+ "\n1. Inizializzazione"
			+ "\n2. Estrazione"
			+ "\n3. Stampa su PDF"
			+ "\n4. Reset"
			+ "\n5. Chiudi"
			+ "\n---------------------------------------------------");

	while(scan.hasNext()) {
		System.out.println("---------------------------------------------------"
				+ "\nScegliere operazione da eseguire"
				+ "\n1. Inizializzazione"
				+ "\n2. Estrazione"
				+ "\n3. Stampa su PDF"
				+ "\n4. Reset"
				+ "\n5. Chiudi"
				+ "\n---------------------------------------------------");
		
		switch (scan.nextInt()) {
		case 1:
			c.init();
			break;
		case 2:
			c.estrazione();
			break;
		case 3:
			c.printPDF();
			break;
		case 4:
			c.reset();
			break;		
		case 5:
			return;
		}
	}

		
	}

}
