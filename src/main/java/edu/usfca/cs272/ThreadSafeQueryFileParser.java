package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import edu.usfca.cs272.InvertedIndex.Result;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for parsing the query and creating the results of a search
 *
 * @author Colin Bindi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class ThreadSafeQueryFileParser implements QueryFileParserInterface {
	/**
	 * Map data structure for map of results
	 */
	private final TreeMap<String, ArrayList<Result>> resultsMap;
	
	/**
	 * The inverted index
	 */
	private final ThreadSafeInvertedIndex index;
	
	/**
	 * The work queue
	 */
	private final WorkQueue workQueue;
	
	/**
	 * Constructor that creates a new TreeMap
	 * @param index the inverted index
	 * @param workQueue the work queue
	 */
	public ThreadSafeQueryFileParser(ThreadSafeInvertedIndex index, WorkQueue workQueue) {
		this.resultsMap = new TreeMap<String, ArrayList<Result>>();
		this.index = index;
		this.workQueue = workQueue;
	}
	
	@Override
	public void parseFile(Path query, boolean exact) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(query, UTF_8)) {
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				Stemmer stemmer = new SnowballStemmer(ENGLISH);
				Task task = new Task(line, exact, stemmer);
				workQueue.execute(task);
			}
		}
		workQueue.finish();
	}
	
	@Override
	public void parseLine(String line, boolean exact, Stemmer stemmer) {
		Set<String> set = TextFileStemmer.uniqueStems(line, stemmer);
		String key = String.join(" ", set);
		
		synchronized (resultsMap) {
			if (set.isEmpty() || resultsMap.containsKey(key)) {
				return;
			}
		}
		
		ArrayList<Result> list = index.search(set, exact);
		
		synchronized (resultsMap) {
			resultsMap.put(key, list);
		}
	}
	
	/**
	 * The non-static task class that will update the shared set
	 * using the increment number.
	 */
	private class Task implements Runnable {
		/** Query line. */
		private final String line;
		
		/** True if exact search and false if partial search. */
		private final boolean exact;
		
		/** The stemmer. */
		private final Stemmer stemmer;

		/**
		 * Initializes this task.
		 * 
		 * @param line a query line
		 * @param exact true if exact search and false if partial search
		 * @param stemmer the stemmer
		 *
		 */
		public Task(String line, boolean exact, Stemmer stemmer) {
			this.line = line;
			this.stemmer = stemmer;
			this.exact = exact;
		}

		@Override
		public void run() {
			parseLine(line, exact, stemmer);
		}
	}
	
	@Override
	public void resultsMapToJson(Path path) throws IOException {
		synchronized (resultsMap) {
			SimpleJsonWriter.writeSearchWords(resultsMap, path);
		}
	}
}
