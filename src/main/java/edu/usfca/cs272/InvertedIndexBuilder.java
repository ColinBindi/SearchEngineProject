package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * This class demonstrates how to use a {@link DirectoryStream} to create a
 * recursive file listing.
 *
 * @see java.nio.file.Path
 * @see java.nio.file.Files
 * @see java.nio.file.DirectoryStream
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class InvertedIndexBuilder {
	/**
	 * Checks whether a path is a text file
	 * 
	 * @param path the path to check
	 * @return true if the path is a text file or false if the path is not a text file
	 */
	public static boolean isTextFile(Path path) {
		String pathString = path.toString().toLowerCase();
		return (Files.isRegularFile(path) && pathString.endsWith(".txt") || pathString.endsWith(".text"));
	}
	
	/**
	 * Traverses through the directory and its sub-directories, outputting all
	 * paths to the console. For files, also includes the file size in bytes.
	 *
	 * @param directory the directory to traverse
	 * @param elements Nested data structure
	 * @throws IOException if an I/O error occurs
	 */
	private static void traverseDirectory(Path directory, InvertedIndex elements) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
			for (Path path : listing) {
				if (Files.isDirectory(path)) {
					traverseDirectory(path, elements);
				}
				else if (isTextFile(path)) {
					parseFile(path, elements);
				}
			}
		}
	}
	
	/**
	 * Traverses through a single file.
	 *
	 * @param start the initial path to traverse
	 * @param elements Nested data structure
	 * @throws IOException if an I/O error occurs
	 */
	public static void parseFile(Path start, InvertedIndex elements) throws IOException {
		String location = start.toString();
		
		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		
		try (BufferedReader reader = Files.newBufferedReader(start, UTF_8)) {
			String line = null;
			int index = 1;
			
			while ((line = reader.readLine()) != null) {
				String[] words = TextParser.parse(line);
				
				for (String word : words) {
					elements.add(stemmer.stem(word).toString(), location, index);
					index++;
				}
			}
		}
	}
	
	/**
	 * Traverses through the directory and its sub-directories, outputting all
	 * paths to the console. For files, also includes the file size in bytes.
	 *
	 * @param start the initial path to traverse
	 * @param elements Nested data structure
	 * @throws IOException if an I/O error occurs
	 */
	public static void build(Path start, InvertedIndex elements) throws IOException {
		if (Files.isDirectory(start)) {
			traverseDirectory(start, elements);
		}
		else {
			parseFile(start, elements);
		}
	}
}