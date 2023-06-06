package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class SimpleJsonWriter {
	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArray(
			Collection<Integer> elements, Writer writer, int indent)
			throws IOException {
		
		writer.write("[");
		
		Iterator<Integer> iterator = elements.iterator();
		writer.write("\n");
		writeIndent(writer, indent-1);
		
		if (iterator.hasNext()) {
			writeIndent(Integer.toString(iterator.next()), writer, indent);
		}
		
		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			writeIndent(Integer.toString(iterator.next()), writer, indent+1);
		}

		writer.write("\n");
		writeIndent("]", writer, indent);
		writer.flush();
	}
	
	/**
	 * Writes the elements as pretty JSON listing each search result.
	 * The search result includes its count, score, and location.
	 * 
	 * @param list list of search objects
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @param entry Result object
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResult(
			ArrayList<Result> list, Writer writer, int indent, Result entry)
			throws IOException {

		writer.write("\n");
		writeIndent(writer, indent+2);
		writer.write("{");
		writer.write("\n");
		writeQuote("count", writer, indent+3);
		writer.write(": ");
		writeIndent(Integer.toString(entry.getMatches()), writer, indent);
		writer.write(",");
		writer.write("\n");
		writeQuote("score", writer, indent+3);
		writer.write(": ");
		writeIndent(String.format("%.8f",entry.getScore()), writer, indent);
		writer.write(",");
		writer.write("\n");
		writeQuote("where", writer, indent+3);
		writer.write(": ");
		writeQuote(entry.getLocation(), writer, indent);
		writer.write("\n");
		writeIndent(writer, indent+2);
		writer.write("}");
	}
	
	/**
	 * Writes the elements as pretty JSON listing each search result.
	 * 
	 * @param list list of search objects
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResults(
			ArrayList<Result> list, Writer writer, int indent)
			throws IOException {
		
		Iterator<Result> iterator = list.iterator();
		
		if (iterator.hasNext()) {
			var entry = iterator.next();
			
			writeSearchResult(list, writer, indent, entry);
		}
		
		while (iterator.hasNext()) {
			var entry = iterator.next();
			
			writer.write(",");
			writeSearchResult(list, writer, indent, entry);
			
		}
		
		writer.flush();
	}

	/**
	 * Writes the elements as pretty JSON listing each search result.
	 * The search result includes its count, score, and location.
	 * 
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchWords(
			TreeMap<String, ArrayList<Result>> elements, Writer writer, int indent)
			throws IOException {
		
		Iterator<Entry<String, ArrayList<Result>>> iterator = elements.entrySet().iterator();
		
		if (iterator.hasNext()) {
			var entry = iterator.next();
			
			writer.write("{");
			
			writer.write("\n");
			
			writeQuote(entry.getKey(), writer, indent+1);
			
			writer.write(": [");
			
			writeSearchResults(entry.getValue(), writer, indent);
		}

		while (iterator.hasNext()) {
			var entry = iterator.next();
			
			writer.write("\n");
			writeIndent(writer, indent+1);
			writer.write("]");
			writer.write(",");
			writer.write("\n");

			writeQuote(entry.getKey(), writer, indent+1);
			
			writer.write(": [");
			
			writeSearchResults(entry.getValue(), writer, indent);
		}

		writer.write("\n");
		writeIndent("]", writer, indent+1);
		writer.write("\n");
		writer.write("}");
		writer.flush();
	}
	
	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @param entry a map entry
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObject(
			Map<String, Integer> elements, Writer writer, int indent, Entry<String, Integer> entry)
			throws IOException {
		
		writer.write("\n");
		writeQuote(entry.getKey(), writer, indent+1);
		writer.write(": ");
		writer.write(Integer.toString(entry.getValue()));
	}
	
	/**
	 * Writes the elements as pretty JSON objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjects(
			Map<String, Integer> elements, Writer writer, int indent)
			throws IOException {
		
		writer.write("{");
		
		Iterator<Entry<String, Integer>> iterator = elements.entrySet().iterator();
		
		if (iterator.hasNext()) {
			var entry = iterator.next();
			writeObject(elements, writer, indent, entry);
		}
		
		while (iterator.hasNext()) {
			var entry = iterator.next();
			writer.write(",");
			writeObject(elements, writer, indent, entry);
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
		writer.flush();
	}
	
	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeMap(
			TreeMap<String, TreeSet<Integer>> elements, Writer writer, int indent)
			throws IOException {
		
		Iterator<Entry<String, TreeSet<Integer>>> iterator = elements.entrySet().iterator();
		
		if (iterator.hasNext()) {
			var entry = iterator.next();
			writer.write("\n");
			writeQuote(entry.getKey().toString(), writer, indent+2);

			writer.write(": ");
			
			writeArray(entry.getValue(), writer, indent+2);
		}
		
		while (iterator.hasNext()) {
			var entry = iterator.next();
			writer.write(",");
			writer.write("\n");
			writeQuote(entry.getKey().toString(), writer, indent+2);

			writer.write(": ");
			
			writeArray(entry.getValue(), writer, indent+2);
		}
		
		writer.flush();
	}
	
	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @param entry a map entry
	 * @throws IOException if an IO error occurs
	 */
	public static void writeMaps(
			TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer, int indent, Entry<String, TreeMap<String, TreeSet<Integer>>> entry)
			throws IOException {
		
		writer.write("\n");
		writeQuote(entry.getKey(), writer, indent+1);
		writer.write(": {");
		writeMap(entry.getValue(), writer, indent);
		writer.write("\n");
		writeIndent(writer, indent+1);
		writer.write("}");
		
	}
	
	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeNestedMap(
			TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer, int indent)
			throws IOException {
		
		writer.write("{");
		
		Iterator<Entry<String, TreeMap<String, TreeSet<Integer>>>> iterator = elements.entrySet().iterator();
		
		if (iterator.hasNext()) {
			var entry = iterator.next();
			writeMaps(elements, writer, indent, entry);
		}
		
		while (iterator.hasNext()) {
			var entry = iterator.next();
			writer.write(",");
			writeMaps(elements, writer, indent, entry);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
		writer.flush();
	}
	
	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of integer objects.
	 * 
	 * @param elements the elements to write
	 * @param path the path where the file will be written
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchWords(
			TreeMap<String, ArrayList<Result>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeSearchWords(elements, writer, 0);
		}
	}
	
	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(
			Collection<Integer> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #writeObjects(Map, Writer, int)
	 */
	public static void writeObjects(
			Map<String, Integer> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjects(elements, writer, 0);
		}
	}
	
	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #writeNestedMap(TreeMap, Writer, int)
	 */
	public static void writeNestedMap(
			TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeNestedMap(elements, writer, 0);
		}
	}
	
	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #writeObjects(Map, Writer, int)
	 */
	public static String writeObjects(Map<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #writeNestedMap(TreeMap, Writer, int)
	 */
	public static String writeNestedMap(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeNestedMap(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}
}