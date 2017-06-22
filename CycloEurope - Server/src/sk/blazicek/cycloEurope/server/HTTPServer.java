package sk.blazicek.cycloEurope.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpServer;

import sk.blazicek.cycloEurope.server.tasks.Closest;
import sk.blazicek.cycloEurope.server.tasks.Connected;
import sk.blazicek.cycloEurope.server.tasks.Identify;

/**
 * HTTP server for communication with application
 * 
 * @author Jozef Blazicek
 */
public class HTTPServer{
	private static final Logger LOG = Logger.getLogger(HTTPServer.class.getName());
	private String ipAddress;
	private int port = 1234;

	public HTTPServer() throws UnknownHostException {
		ipAddress = InetAddress.getLocalHost().getHostAddress();
	}

	public void run() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(ipAddress, port), 0);

			server.createContext("/identify", new Identify(ipAddress));
			LOG.log(Level.INFO, "http://" + ipAddress + ":" + port + "/identify");

			server.createContext("/closest", new Closest());
			LOG.log(Level.INFO, "http://" + ipAddress + ":" + port + "/closest");
			
			server.createContext("/connected", new Connected());
			LOG.log(Level.INFO, "http://" + ipAddress + ":" + port + "/connecting");

			server.setExecutor(null);
			server.start();
			
			LOG.log(Level.INFO, "Running at http://" + ipAddress + ":" + port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
