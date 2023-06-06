package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Colin Bindi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class Driver {
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		ArgumentParser parser = new ArgumentParser(args);
		
		ThreadSafeInvertedIndex safeIndex = null;
		
		InvertedIndex index = null;
		
		QueryFileParserInterface queryFileParser = null;
		
		WorkQueue workQueue = null;
		
		if (parser.hasFlag("-threads") || parser.hasFlag("-html") || parser.hasFlag("-server")) {
			int num = parser.getInteger("-threads", 5);
			
			if (num < 1) {
				num = 5;
			}
			
			safeIndex = new ThreadSafeInvertedIndex();
			index = safeIndex;
			workQueue = new WorkQueue(num);
			queryFileParser = new ThreadSafeQueryFileParser(safeIndex, workQueue);
		} else {
			index = new InvertedIndex();
			queryFileParser = new QueryFileParser(index);
		}
		
		if (parser.hasFlag("-text")) {
			if (parser.hasValue("-text")) {
				Path textPath = parser.getPath("-text");
				try {
					if (safeIndex != null && workQueue != null) {
						ThreadSafeInvertedIndexBuilder.build(textPath, safeIndex, workQueue);
					} else {
						InvertedIndexBuilder.build(textPath, index);
					}
				} catch (IOException e) {
					System.out.println("Unable to build the inverted index from path: " + textPath.toString());
				}
			} else {
				System.out.println("The text file is missing.");
			}
		}
		
		if (workQueue != null && parser.hasFlag("-html")) {
			String htmlPath = parser.getString("-html", "");
			
			int max = 1;
			
			if (parser.hasFlag("-max")) {
				max = parser.getInteger("-max", 1);
			}
			
			try {
				WebCrawler webCrawler = new WebCrawler(safeIndex, max, workQueue);
				webCrawler.build(htmlPath);
				workQueue.finish();
			} catch (IOException e) {
				System.out.println("Unable to build the inverted index from URL: " + htmlPath.toString());
			}
		}
		
		if (workQueue != null && parser.hasFlag("-server")) {
			int port = parser.getInteger("-server", 8080);
			WebServer webServer = new WebServer(port, safeIndex, workQueue);
			try {
				webServer.startServer();
			} catch (Exception e) {
				System.out.println("Unable to start a server from the port: " + port);
			}
		}
		
		if (parser.hasFlag("-query")) {
			Path queryPath = parser.getPath("-query");
			
			try {
				queryFileParser.parseFile(queryPath, parser.hasFlag("-exact"));
			} catch (NullPointerException e) {
				System.out.println("Unable to search the results");
			} catch (IOException e) {
				System.out.println("Unable to search the results: " + queryPath.toString());
			}
		}
		
		if (workQueue != null) {
			workQueue.join();
		}
			
		if (parser.hasFlag("-counts")) {
			Path countsPath = parser.getPath("-counts", Path.of("counts.json"));
			
			try {
				index.wordCountToJson(countsPath);
			} catch (IOException e) {
				System.out.println("Unable to output the count to path: " + countsPath.toString());
			}
		}
		
		if (parser.hasFlag("-results")) {
			Path resultsPath = parser.getPath("-results", Path.of("results.json"));
			
			try {
				queryFileParser.resultsMapToJson(resultsPath);
			} catch (Exception e) {
				System.out.println("Unable to output the results: " + resultsPath.toString());
			}
		}
			
		if (parser.hasFlag("-index")) {
			Path indexPath = parser.getPath("-index", Path.of("index.json"));

			try {
				index.toJson(indexPath);
			} catch (IOException e) {
				System.out.println("Unable to output the inverted index to path: " + indexPath.toString());
			}
		}
	}
}
