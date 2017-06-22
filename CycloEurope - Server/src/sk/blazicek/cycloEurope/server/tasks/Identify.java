package sk.blazicek.cycloEurope.server.tasks;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Used for discovering server on a local network.
 * 
 * @author Jozef Blazicek
 */
public class Identify implements HttpHandler {
	private static final Logger LOG = Logger.getLogger(Identify.class.getName());
	private String ipAddress = null;

	public Identify(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public void handle(HttpExchange httpExchange) {
		try {
			LOG.log(Level.INFO, "identify : " + httpExchange.getRemoteAddress().getAddress().toString() + "/identify?"
					+ httpExchange.getRequestURI().getQuery());

			httpExchange.sendResponseHeaders(200, ipAddress.length());
			OutputStream outputStream = httpExchange.getResponseBody();
			outputStream.write(ipAddress.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
