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
public class QueryFileParser implements QueryFileParserInterface {
	/**
	 * Map data structure for map of results
	 */
	private final TreeMap<String, ArrayList<Result>> resultsMap;
	
	/**
	 * The inverted index
	 */
	private final InvertedIndex index;
	
	/**
	 * Constructor that creates a new TreeMap
	 * @param index the inverted index
	 */
	public QueryFileParser(InvertedIndex index) {
		this.resultsMap = new TreeMap<String, ArrayList<Result>>();
		this.index = index;
	}
	
	@Override
	public void parseFile(Path path, boolean exact) throws IOException {
		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				parseLine(line, exact, stemmer);
			}
		}
	}
	
	@Override
	public void parseLine(String line, boolean exact, Stemmer stemmer) {
		Set<String> set = TextFileStemmer.uniqueStems(line, stemmer);
		
		if (!set.isEmpty()) {
			String key = String.join(" ", set);
			
			if (!resultsMap.containsKey(key)) {
				ArrayList<Result> list = index.search(set, exact);
				
				resultsMap.put(key, list);
			}
		}
	}
	
	@Override
	public void resultsMapToJson(Path path) throws IOException {
		SimpleJsonWriter.writeSearchWords(resultsMap, path);
	}
}
