package edu.usfca.cs272;

import java.time.LocalDateTime;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Creates a web server.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class WebServer {
	/**
	 * The port of the server
	 */
	private final int port;
	
	/**
	 * The inverted index.
	 */
	public ThreadSafeInvertedIndex index;
	
	/**
	 * The work queue.
	 */
	public WorkQueue workQueue;
	
	/**
	 * The server uptime.
	 */
	public static LocalDateTime uptime;
	
	/**
	 * Sets the port
	 * 
	 * @param port the port of the server
	 * @param index the inverted index
	 * @param workQueue the work queue
	 */
	public WebServer(int port, ThreadSafeInvertedIndex index, WorkQueue workQueue) {
		this.port = port;
		this.index = index;
		this.workQueue = workQueue;
	}

	/**
	 * Starts a Jetty server on port 8080.
	 *
	 * @throws Exception if unable to start server successfully
	 */
	public void startServer() throws Exception {
		Server server = new Server(port);

		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		
		servletContext.setContextPath("/");
		servletContext.addServlet(new ServletHolder(new WebServlet(index, workQueue)), "/");
		servletContext.addServlet(new ServletHolder(new HistoryServlet(index, workQueue)), "/history");
		servletContext.addServlet(new ServletHolder(new SeedServlet(index, workQueue)), "/seed");
		servletContext.addServlet(new ServletHolder(new IndexServlet(index, workQueue)), "/index");
		servletContext.addServlet(new ServletHolder(new CountServlet(index, workQueue)), "/counts");
		servletContext.addServlet(new ServletHolder(new PrivateServlet(index, workQueue)), "/private");
		servletContext.addServlet(new ServletHolder(new PopularServlet(index, workQueue)), "/popular");
		servletContext.addServlet(new ServletHolder(new ResetServlet(index, workQueue)), "/reset");
		servletContext.addServlet(new ServletHolder(new ExactServlet(index, workQueue)), "/exact");
		servletContext.addServlet(new ServletHolder(new ReverseServlet(index, workQueue)), "/reverse");
		servletContext.addServlet(new ServletHolder(new OptionsServlet(index, workQueue)), "/options");
		servletContext.addServlet(new ServletHolder(new QuickServlet(index, workQueue)), "/quick");
		servletContext.addServlet(new ServletHolder(new ShutdownServlet(index, workQueue)), "/end");
		
		HandlerList handlers = new HandlerList();
		
		handlers.addHandler(new ShutdownHandler("password", false, false));
		
		handlers.addHandler(servletContext);
		
		server.setHandler(handlers);
		
		server.start();
		
		uptime = LocalDateTime.now();
		
		System.out.println("Server started");

		server.join();
	}
	
	/**
	 * Gets the uptime
	 * 
	 * @return the uptime
	 */
	public static LocalDateTime serverUptime() {
		return uptime;
	}
}
