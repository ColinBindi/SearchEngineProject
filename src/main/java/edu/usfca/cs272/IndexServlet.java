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

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet to GET handle requests to /index.
 */
public class IndexServlet extends HttpServlet {
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
	private static final Path INDEX = Path.of("src", "main", "resources", "index.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path OPTIONS = Path.of("src", "main", "resources", "options.html");

	/**
	 * Servlet Constructor
	 * 
	 * @param index the inverted index
	 * @param workQueue the work queue
	 */
	public IndexServlet(ThreadSafeInvertedIndex index, WorkQueue workQueue) {
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
		
		PrintWriter out = response.getWriter();
		
		Map<String, String> values = new HashMap<>();
		
		ArrayList<String> inputList = (ArrayList<String>) session.getAttribute("input");
		
		values.put("index", index.toWeb());
		
		Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		
		values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
				"&ensp;|&ensp;Words Stored: "+index.size()+
				"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
				"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
		
		out.print(new StringSubstitutor(values).replace(Files.readString(INDEX, UTF_8)));
		
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
		
		PrintWriter out = response.getWriter();
		
		Map<String, String> values = new HashMap<>();
		
		Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		
		values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
				"&ensp;|&ensp;Words Stored: "+index.size()+
				"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
				"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
		
		if (request.getParameter("options") != null) {
			out.print(Files.readString(OPTIONS, UTF_8));
			return;
		}
		
		if (request.getParameter("word") != null) {
			String word = request.getParameter("word");

			word = word == null ? "" : word;
			
			word = StringEscapeUtils.escapeHtml4(word);
			
			if (index.contains(word)) {
				StringBuilder builder = new StringBuilder();
				
				builder.append("<strong>Stem:</strong> "+word+"<br>");
				for (String location : index.getLocations(word)) {
					builder.append("<a href=\""+location+"\">"+location+"</a><strong> - ");
					builder.append("Positions: </strong>"+index.getIndices(word, location).size());
					builder.append("<br>");
				}
				
				values.put("index", builder.toString());
				
				out.print(new StringSubstitutor(values).replace(Files.readString(INDEX, UTF_8)));
			} else {
				values.put("index", "This word is not in the Inverted Index");
				
				out.print(new StringSubstitutor(values).replace(Files.readString(INDEX, UTF_8)));
			}
		}
	}
}