package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import opennlp.tools.stemmer.Stemmer;

/**
 * Interface responsible for parsing the query file
 *
 * @author Colin Bindi
 */
public interface QueryFileParserInterface {	
	/**
	 * Parses the query file and builds a map of results
	 * 
	 * @param path the path where the query of words is located
	 * @param exact true if exact search or false for partial search
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void parseFile(Path path, boolean exact) throws IOException;
	
	/**
	 * Parses a query line and builds a map of results
	 * 
	 * @param line a line of the query file
	 * @param exact true if exact search or false for partial search
	 * @param stemmer the stemmer to use
	 */
	public void parseLine(String line, boolean exact, Stemmer stemmer);
	
	/**
	 * Writes pretty json to a specific path
	 *
	 * @param path the path to write to
	 * @throws IOException if an I/O error occurs
	 */
	public void resultsMapToJson(Path path) throws IOException;
}
