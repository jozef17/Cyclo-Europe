package sk.blazicek.cycloEurope.server;

/**
 * Contains entry point of Server
 * 
 * @author Jozef Blazicek
 */
public class Main {
	
	public static void main(String[] argv) {
		try {
			HTTPServer server = new HTTPServer();
			server.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
