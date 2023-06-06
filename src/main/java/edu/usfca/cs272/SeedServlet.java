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
 * Servlet to GET handle requests to /add.
 */
public class SeedServlet extends HttpServlet {
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
	private static final Path SEED = Path.of("src", "main", "resources", "seed.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path OPTIONS = Path.of("src", "main", "resources", "options.html");

	/**
	 * Servlet Constructor
	 * 
	 * @param index the inverted index
	 * @param workQueue the work queue
	 */
	public SeedServlet(ThreadSafeInvertedIndex index, WorkQueue workQueue) {
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
		
		ArrayList<String> inputList = (ArrayList<String>) session.getAttribute("input");
		
		Map<String, String> values = new HashMap<>();
		
		values.put("build", "");
		
		Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		
		values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
				"&ensp;|&ensp;Words Stored: "+index.size()+
				"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
				"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
		
		out.print(new StringSubstitutor(values).replace(Files.readString(SEED, UTF_8)));

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
		
		PrintWriter out = response.getWriter();
		
		ArrayList<String> inputList = (ArrayList<String>) session.getAttribute("input");
		
		Map<String, String> values = new HashMap<>();
		
		Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		
		values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
				"&ensp;|&ensp;Words Stored: "+index.size()+
				"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
				"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
		
		if (request.getParameter("options") != null) {
			out.print(Files.readString(OPTIONS, UTF_8));
		}
		
		if (request.getParameter("add") != null) {
			String seed = request.getParameter("seed");
			String max = request.getParameter("max");
			
			max = max == "" ? "1" : max;
			
			int num = Integer.parseInt(max);

			seed = seed == null ? "" : seed;
			
			seed = StringEscapeUtils.escapeHtml4(seed);
			
			max = StringEscapeUtils.escapeHtml4(max);
			
			if (!index.containsPath(seed)) {
				try {
					WebCrawler webCrawler = new WebCrawler(index, num, workQueue);
					webCrawler.build(seed);
					workQueue.finish();
					values.put("build", "The seed URL was added to index.");
				} catch (IOException e) {
					values.put("build", "Unable to build the inverted index from URL: " + seed.toString());
				}
			} else {
				values.put("build", "This URL has already been crawled.");
			}
			
			out.print(new StringSubstitutor(values).replace(Files.readString(SEED, UTF_8)));
		}
		
		out.flush();
		response.flushBuffer();
		
		response.setContentType("text/html;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
	}
}