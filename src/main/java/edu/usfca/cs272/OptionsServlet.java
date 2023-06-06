package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet to GET handle requests to /options.
 */
public class OptionsServlet extends HttpServlet {
	/** ID used for serialization, which we are not using. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The inverted index.
	 */
	public ThreadSafeInvertedIndex index;
	
	/**
	 * The work queue.
	 */
	public WorkQueue workQueue;
	
	/** Location of the HTML template for this servlet. */
	private static final Path RESULTS = Path.of("src", "main", "resources", "results.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path OPTIONS = Path.of("src", "main", "resources", "options.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path PRIVATE = Path.of("src", "main", "resources", "private.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path EXACT = Path.of("src", "main", "resources", "exact.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path REVERSE = Path.of("src", "main", "resources", "reverse.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path QUICK = Path.of("src", "main", "resources", "quick.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path HISTORY = Path.of("src", "main", "resources", "history.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path SEED = Path.of("src", "main", "resources", "seed.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path INDEX = Path.of("src", "main", "resources", "index.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path COUNTS = Path.of("src", "main", "resources", "counts.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path RESET = Path.of("src", "main", "resources", "reset.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path SHUTDOWN = Path.of("src", "main", "resources", "shutdown.html");
	
	/**
	 * Servlet Constructor
	 * 
	 * @param index the inverted index
	 * @param workQueue the work queue
	 */
	public OptionsServlet(ThreadSafeInvertedIndex index, WorkQueue workQueue) {
		this.index = index;
		this.workQueue = workQueue;
	}
	/**
	 * Displays a form where users can enter a URL to check. When the button is
	 * pressed, submits the URL back to "/web" as a GET request.
	 *
	 * If a URL was included as a parameter in the GET request, safely fetch and
	 * display the HTTP headers of that URL.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(true);
		
		if (session.isNew()) {
			session.setAttribute("time", LocalDateTime.now());
		}
		
		LocalDateTime lastVisit = (LocalDateTime) session.getAttribute("time");
		
		if (lastVisit == null) {
			session.setAttribute("time", LocalDateTime.now());
			lastVisit = (LocalDateTime) session.getAttribute("time");
		}
		
		ArrayList<String> inputList = (ArrayList<String>) session.getAttribute("input");
		
		if (inputList == null) {
			inputList = new ArrayList<>();
		}
		
		PrintWriter out = response.getWriter();
		
		Map<String, String> values = new HashMap<>();
		
		values.put("text", "");
		
		Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		
		values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
				"&ensp;|&ensp;Words Stored: "+index.size()+
				"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
				"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
		
		out.print(new StringSubstitutor(values).replace(Files.readString(OPTIONS, UTF_8)));
		
		response.setContentType("text/html;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(true);
		
		if (session.isNew()) {
			session.setAttribute("time", LocalDateTime.now());
		}
		
		LocalDateTime lastVisit = (LocalDateTime) session.getAttribute("time");
		
		if (lastVisit == null) {
			session.setAttribute("time", LocalDateTime.now());
			lastVisit = (LocalDateTime) session.getAttribute("time");
		}
		
		ArrayList<String> inputList = (ArrayList<String>) session.getAttribute("input");
		
		if (inputList == null) {
			inputList = new ArrayList<>();
		}
		
		PrintWriter out = response.getWriter();
		
		Map<String, String> values = new HashMap<>();
		
		values.put("text", "");
		
		Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		
		values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
				"&ensp;|&ensp;Words Stored: "+index.size()+
				"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
				"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
		
		if (request.getParameter("options") != null) {
			out.print(Files.readString(OPTIONS, UTF_8));
		}
		
		if (request.getParameter("search") != null) {
			out.print(new StringSubstitutor(values).replace(Files.readString(RESULTS, UTF_8)));
		}
		
		if (request.getParameter("private") != null) {
			values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
					"&ensp;|&ensp;Words Stored: "+index.size()+
					"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0"));
			
			out.print(new StringSubstitutor(values).replace(Files.readString(PRIVATE, UTF_8)));
		}
		
		if (request.getParameter("exact") != null) {
			out.print(new StringSubstitutor(values).replace(Files.readString(EXACT, UTF_8)));
		}
		
		if (request.getParameter("reverse") != null) {
			out.print(new StringSubstitutor(values).replace(Files.readString(REVERSE, UTF_8)));
		}
		
		if (request.getParameter("quick") != null) {
			out.print(new StringSubstitutor(values).replace(Files.readString(QUICK, UTF_8)));
		}
		
		if (request.getParameter("history") != null) {
			StringBuilder builder = new StringBuilder();
			
			if (inputList != null && !inputList.isEmpty()) {
				int i = 1;
				for (String search : inputList) {
					builder.append("<strong>"+i+".</strong> "+search+"<br>");
					i++;
				}
			}
			
			values.put("history", builder.toString());
			
			out.print(new StringSubstitutor(values).replace(Files.readString(HISTORY, UTF_8)));
		}
		
		if (request.getParameter("new") != null) {
			values.put("build", "");
			out.print(new StringSubstitutor(values).replace(Files.readString(SEED, UTF_8)));
		}
		
		if (request.getParameter("index") != null) {
			values.put("index", index.toWeb());
			out.print(new StringSubstitutor(values).replace(Files.readString(INDEX, UTF_8)));
		}
		
		if (request.getParameter("location") != null) {
			values.put("counts", index.wordCountToWeb());
			out.print(new StringSubstitutor(values).replace(Files.readString(COUNTS, UTF_8)));
		}
		
		if (request.getParameter("reset") != null) {
			out.print(new StringSubstitutor(values).replace(Files.readString(RESET, UTF_8)));
		}
		
		if (request.getParameter("shutdown") != null) {
			out.print(new StringSubstitutor(values).replace(Files.readString(SHUTDOWN, UTF_8)));
		}
		
		out.flush();
		response.flushBuffer();
		
		response.setContentType("text/html;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
	}
}