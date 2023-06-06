package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for crawling the web.
 *
 * @author Colin Bindi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class WebCrawler {
	/**
	 * The total number of URLs to crawl.
	 */
	private final int max;
	
	/**
	 * The inverted index.
	 */
	private ThreadSafeInvertedIndex index;
	
	/**
	 * The work queue.
	 */
	private final WorkQueue workQueue;
	
	/**
	 * Set containing each URL.
	 */
	private Set<URL> lookup;
	
	/**
	 * Map containing each Page.
	 */
	private static Map<String, Page> pages;
	
	/**
	 * Constructor for the web crawler.
	 * 
	 * @param index the inverted index
	 * @param max the total number of URLs to crawl
	 * @param workQueue the work queue
	 */
	public WebCrawler(ThreadSafeInvertedIndex index, int max, WorkQueue workQueue) {
		this.max = max;
		this.index = index;
		this.workQueue = workQueue;
		this.lookup = new HashSet<URL>();
		pages = new HashMap<String, Page>();
		
	}
	/**
	 * Traverses through the directory and its sub-directories, outputting all
	 * paths to the console. For files, also includes the file size in bytes.
	 *
	 * @param start the initial path to traverse
	 * @throws IOException if an I/O error occurs
	 */
	public void build(String start) throws IOException {
		URL base = new URL(start);
		
		lookup.add(base);
		
		Task task = new Task(base, base);
		
		workQueue.execute(task);
	}
	
	/**
	 * Builds text snippets
	 * 
	 * @param html the html
	 * @return group the words
	 */
	public String fetchSnippet(String html) {
		if (html == null) {
			html = "";
			return "";
		}
		
		String regex = "(?i)<p.*?>([^<>]{50,800})<?\\/?p.*?>";
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		
		String group = "";
		
		while (matcher.find()) {
			group += matcher.group(matcher.groupCount());
			group = group.replaceAll("[\\s]*?", "");
			
		}
		
		if (group.length() > 400) {
			group = group.substring(0, 400);
		}
		
		if (group.startsWith(", ")) {
			group = group.replaceFirst(", ", "");
		}

		if (group == "") {
			return "Contains links";
		}
		
		return group;
	}
	
	/**
	 * Fetches the title
	 * 
	 * @param html the html
	 * @return group the match
	 */
	public String fetchTitle(String html) {
		if (html == null) {
			html = "";
			return "";
		}
		
		String regex = "(?is)<title>(.*?)<\\/title>";
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		
		String group = "";
		
		if (matcher.find()) {
			group = matcher.group(matcher.groupCount());
			group = group.replaceAll("\n", "");
			group = group.trim();
			
		}
		
		return group;
	}
	
	/**
	 * Gets the page
	 * 
	 * @param url the link
	 * @return the page
	 */
	public static Page getPage(String url) {
		return pages.get(url);
	}
	
	/**
	 * The non-static task class that will update the shared set
	 * using the increment number.
	 */
	public class Task implements Runnable {
		/** The link. */
		private URL url;
		
		/**
		 * The URL base
		 */
		private URL base;

		/**
		 * Initializes this task.
		 *
		 * @param url the link
		 * @param base the base
		 */
		public Task(URL url, URL base) {
			this.url = url;
			this.base = url;
		}

		@Override
		public void run() {
			try {
				url = LinkParser.normalize(url);
				
				String html = HtmlFetcher.fetch(url, 3);
				
				String urlString = url.toString();
				
				pages.put(urlString, new Page(urlString, fetchSnippet(html), fetchTitle(html), HtmlFetcher.getContentLength(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm a"))));
				
				html = HtmlCleaner.stripBlockElements(html);
				
				ArrayList<URL> list = LinkParser.getValidLinks(base, html);
				
				synchronized (lookup) {
					for (URL link : list) {
						if (lookup.size() == max) {
							break;
						} else {
							if (!lookup.contains(link)) {
								lookup.add(link);
								Task task = new Task(link, url);
								workQueue.execute(task);
							}
						}
					}
				}
				
				html = HtmlCleaner.stripTags(html);
				html = HtmlCleaner.stripEntities(html);
				
				
				String[] words = TextParser.parse(html);
				
				Stemmer stemmer = new SnowballStemmer(ENGLISH);
				
				InvertedIndex local = new InvertedIndex();
				
				int i = 1;
				
				for (String word : words) {
					local.add(stemmer.stem(word).toString(), urlString, i);
					i++;
				}
				
				index.addAll(local);
			} catch (MalformedURLException e) {
				System.out.println("MalformedURLException");
			} catch (URISyntaxException e) {
				System.out.println("URISyntaxException");
			}
		}
	}
	
	/**
	 * Contains data for each page crawled
	 */
	public class Page {
		/** The normalized text file path. */
		private final String location;
		
		/** The snippet. */
		private String snippet;
		
		/** The title. */
		private String title;
		
		/** The length. */
		private final String length;
		
		/** The timestamp. */
		private final String timestamp;
		
		/**
		 * Data from each page is stored
		 * 
		 * @param location the location
		 * @param snippet the snippet
		 * @param title the title
		 * @param length the length
		 * @param timestamp the timestamp
		 */
		public Page(String location, String snippet, String title, String length, String timestamp) {
			this.location = location;
			this.snippet = snippet;
			this.title = title;
			this.length = length;
			this.timestamp = timestamp;
		}
		
		/**
		 * Gets the location
		 * 
		 * @return location the location
		 */
		public String getLocation() {
			return location;
		}
		
		/**
		 * Gets the snippet
		 * 
		 * @return snippet the snippet
		 */
		public String getSnippet() {
			return snippet;
		}
		
		/**
		 * Gets the title
		 * 
		 * @return title the title
		 */
		public String getTitle() {
			return title;
		}
		
		/**
		 * Sets the snippet
		 * @param snippet the snippet
		 */
		public void setSnippet(String snippet) {
			this.snippet = snippet;
		}
		
		/**
		 * Sets the title
		 * @param title the title
		 */
		public void setTitle(String title) {
			this.title = title;
		}
		
		/**
		 * Gets the length
		 * 
		 * @return length the length
		 */
		public String getLength() {
			return length;
		}
		
		/**
		 * Gets the timestamp
		 * 
		 * @return timestamp the timestamp
		 */
		public String getTimeStamp() {
			return timestamp;
		}
	}
}