package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

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

import edu.usfca.cs272.InvertedIndex.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Servlet to GET handle requests to /.
 */
public class WebServlet extends HttpServlet {
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
	
	/**
	 * The time of the last user visit
	 */
	public static LocalDateTime lastVisit;
	
	/** Location of the HTML template for this servlet. */
	private static final Path RESULTS = Path.of("src", "main", "resources", "results.html");
	
	/** Location of the HTML template for this servlet. */
	private static final Path OPTIONS = Path.of("src", "main", "resources", "options.html");
	
	/**
	 * Servlet Constructor
	 * 
	 * @param index the inverted index
	 * @param workQueue the work queue
	 */
	public WebServlet(ThreadSafeInvertedIndex index, WorkQueue workQueue) {
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
		
		if (inputList == null) {
			inputList = new ArrayList<>();
		}
		
		Map<String, String> values = new HashMap<>();
		
		values.put("text", "");
		
		Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		
		values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
				"&ensp;|&ensp;Words Stored: "+index.size()+
				"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
				"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
		
		out.print(new StringSubstitutor(values).replace(Files.readString(RESULTS, UTF_8)));

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
		
		Map<String, String> values = new HashMap<>();
		
		ArrayList<String> inputList = (ArrayList<String>) session.getAttribute("input");
		
		if (inputList == null) {
			inputList = new ArrayList<>();
		}
		
		if (request.getParameter("options") != null) {
			out.print(Files.readString(OPTIONS, UTF_8));
		}
		
		if (request.getParameter("search") != null) {
			LocalDateTime startTime = LocalDateTime.now();
			
			String queries = request.getParameter("queries");

			queries = queries == null ? "" : queries;
			
			queries = StringEscapeUtils.escapeHtml4(queries);
			
			if (queries != null) {
				inputList.add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))+" - "+queries);
				session.setAttribute("input", inputList);
			}
			
			ArrayList<Result> resultList = index.search(TextFileStemmer.uniqueStems(queries, new SnowballStemmer(ENGLISH)), false);
			
			StringBuilder builder = new StringBuilder();
			
			int num = 1;
			
			for (Result result : resultList) {
				String location = result.toString();
				if (WebCrawler.getPage(location) != null) {
					builder.append("<strong>"+num+". </strong><a href=\""+location+"\">"+WebCrawler.getPage(location).getTitle()+"</a><br>");
					builder.append(WebCrawler.getPage(location).getSnippet());
					builder.append("<br><strong>&bull; Score: </strong>"+String.format("%.2f", result.getScore()));
					builder.append("<br><strong>&bull; Matches: </strong>"+result.getMatches());
					builder.append("<br><strong>&bull; Content Length: </strong>"+WebCrawler.getPage(location).getLength());
					builder.append("<br><strong>&bull; Time Stamp: </strong>"+WebCrawler.getPage(location).getTimeStamp());
					builder.append("<br><br>");
					num++;
				}
			}
			
			if (builder.isEmpty()) {
				values.put("text", "No matches were found for your search.");
			} else {
				values.put("text", builder.toString());
			}
			
		    Duration searchDuration = Duration.between(WebServer.serverUptime(), LocalDateTime.now());
		    
		    values.put("stats", "<br>Server Uptime: "+String.format("%02d:%02d:%02d", searchDuration.toHours(), searchDuration.toMinutesPart(), searchDuration.toSecondsPart())+
		    		"&ensp;|&ensp;Words Stored: "+index.size()+
		    		"&ensp;|&ensp;Queries Conducted: "+(inputList != null ? Integer.toString(inputList.size()) : "0")+
		    		"&ensp;|&ensp;Last Visit: "+(lastVisit == null ? "" : lastVisit.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a")))+
		    		"<br><br>Search Time: "+String.format("0.00%d seconds", Duration.between(startTime, LocalDateTime.now()).toMillis())+
		    		" | Search Results: "+(num-1)+"<br>");
			
			out.print(new StringSubstitutor(values).replace(Files.readString(RESULTS, UTF_8)));
		}
		
		out.flush();
		response.flushBuffer();
		
		response.setContentType("text/html;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
	}
}