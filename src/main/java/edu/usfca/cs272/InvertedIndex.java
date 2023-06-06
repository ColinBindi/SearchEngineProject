package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * Class responsible for storing information about data structure
 *
 * @author Colin Bindi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class InvertedIndex {	
	/**
	 * Map data structure for inverted index
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements;
	
	/**
	 * Map data structure for word count in each path
	 */
	private final TreeMap<String, Integer> counts;
	
	/**
	 * Constructor that creates a new TreeMap
	 */
	public InvertedIndex() {
		elements = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		counts = new TreeMap<String, Integer>();
	}

	/**
	 * Adds the word and the provided path and index.
	 *
	 * @param word the word to be added
	 * @param location the location the word was found
	 * @param index the index of the word found at that location
	 */
	public void add(String word, String location, int index) {
		elements.putIfAbsent(word, new TreeMap<>());
		elements.get(word).putIfAbsent(location, new TreeSet<>());
		
		if (elements.get(word).get(location).add(index)) {
			counts.putIfAbsent(location, 0);
			counts.put(location, counts.get(location) + 1);
		}
	}
	
	/**
	 * Adds the location and all its words to the inverted index
	 * 
	 * @param words all words located at a given path
	 * @param location the path where the words were located
	 */
	public void addAll(List<String> words, String location) {
		int index = 1;
		for (String word : words) {
			add(word, location, index);
			index++;
		}
	}
	
	/**
	 * Adds all of the words, locations, and indices from the other index to this index.
	 *
	 * @param other the other index to add
	 */
	public void addAll(InvertedIndex other) {
		for (var wordMap : other.elements.entrySet()) {
			if (this.elements.containsKey(wordMap.getKey())) {
				for (var locationMap : wordMap.getValue().entrySet()) {
					if (this.elements.get(wordMap.getKey()).containsKey(locationMap.getKey())) {
						this.elements.get(wordMap.getKey()).get(locationMap.getKey()).addAll(locationMap.getValue());
					} else {
						this.elements.get(wordMap.getKey()).put(locationMap.getKey(), locationMap.getValue());
					}
				}
			}
			else {
				this.elements.put(wordMap.getKey(), wordMap.getValue());
			}
		}
		
		for (var countMap : other.counts.entrySet()) {
			if (this.counts.containsKey(countMap.getKey())) {
				this.counts.put(countMap.getKey(), (this.counts.get(countMap.getKey()) + other.counts.get(countMap.getKey())));
			} else {
				this.counts.put(countMap.getKey(), countMap.getValue());
			}
		}
	}
	
	/**
	 * Searches the inverted index to find the exact matches of all the words in the query.
	 * 
	 * @param queries the set of stems in a query line
	 * @return list the list of result objects
	 */
	public ArrayList<Result> exactSearch(Set<String> queries) {
		ArrayList<Result> list = new ArrayList<>();
		
		Map<String, Result> lookup = new HashMap<String, Result>();
			
		for (String stem : queries) {
			if (elements.containsKey(stem)) {
				addLocations(stem, lookup, list);
			}
		}

		Collections.sort(list);

		return list;
	}
	
	/**
	 * Searches the inverted index to find the partial matches of all the words in the query.
	 * 
	 * @param queries the set of stems in a query line
	 * @return list the list of result objects
	 */
	public ArrayList<Result> partialSearch(Set<String> queries) {
		ArrayList<Result> list = new ArrayList<>();
		
		Map<String, Result> lookup = new HashMap<String, Result>();
			
		for (String stem : queries) {
			for (String word : elements.tailMap(stem).keySet()) {
				if (!word.startsWith(stem)) {
					break;
				}
				addLocations(word, lookup, list);
			}
		}

		Collections.sort(list);

		return list;
	}
	
	/**
	 * Adds the locations of a stem
	 * 
	 * @param stem the word stem
	 * @param lookup map of current results
	 * @param list the list of result objects
	 */
	private void addLocations(String stem, Map<String, Result> lookup, ArrayList<Result> list) {
		for (String location : elements.get(stem).keySet()) {
			if (!lookup.containsKey(location)) {
				Result newResult = new Result(location);
				list.add(newResult);
				lookup.put(location, newResult);
			}
			
			lookup.get(location).update(stem);
		}
	}
	
	/**
	 * Searches an exact query line or a partial query line
	 * 
	 * @param queries the set of stems in a query line
	 * @param exactSearch true if exact search or false for partial search
	 * @return list the list of result objects
	 */
	public ArrayList<Result> search(Set<String> queries, boolean exactSearch) {
		ArrayList<Result> list;
		
		if (exactSearch) {
			list = exactSearch(queries);
		} else {
			list = partialSearch(queries);
		}
		
		return list;
	}
	
	/**
	 * Searches the locations for a partial match
	 * 
	 * @param location the partial location
	 * @return list the list of result objects
	 */
	public ArrayList<String> partialLocationSearch(String location) {
		ArrayList<String> list = new ArrayList<>();
		
		for (String match : counts.tailMap(location).keySet()) {
			if (!match.startsWith(location)) {
				break;
			}
			list.add(match);
		}

		Collections.sort(list);

		return list;
	}
	
	/**
	 * Returns an unmodifiable view of the words stored in the index.
	 *
	 * @return an unmodifiable view of the words stored in the index
	 */
	public Collection<String> getWords() {
		return Collections.unmodifiableSet(elements.keySet());
	}
	
	/**
	 * Returns an unmodifiable view of the locations stored in the index.
	 * @param word the word that is present in the locations
	 *
	 * @return an unmodifiable view of the locations stored in the index
	 */
	public Collection<String> getLocations(String word) {
		if (!contains(word)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(elements.get(word).keySet());
	}
	
	/**
	 * Returns an unmodifiable view of the indices stored in the index.
	 * @param word the word that is present in the locations
	 * @param location the location of a word in a single path
	 *
	 * @return an unmodifiable view of the indices stored in the index
	 */
	public Collection<Integer> getIndices(String word, String location) {
		if (!contains(word, location)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableCollection(elements.get(word).get(location));
	}
	
	/**
	 * Writes pretty json to a specific path
	 *
	 * @param path the path to write to
	 * @throws IOException if an I/O error occurs
	 */
	public void toJson(Path path) throws IOException {
		SimpleJsonWriter.writeNestedMap(elements, path);
	}
	
	/**
	 * Builds HTML to output
	 *
	 * @return the built HTML
	 * @throws IOException if an I/O error occurs
	 */
	public String toWeb() throws IOException {
		StringBuilder builder = new StringBuilder();
		
		Iterator<Entry<String, TreeMap<String, TreeSet<Integer>>>> iterator = elements.entrySet().iterator();
		
		while (iterator.hasNext()) {
			var entry = iterator.next();
			
			Iterator<Entry<String, TreeSet<Integer>>> innerIterator = entry.getValue().entrySet().iterator();
			
			builder.append("<strong>Stem:</strong> "+entry.getKey()+"<br>");
			
			while (innerIterator.hasNext()) {
				var innerEntry = innerIterator.next();
				builder.append("<a href=\""+innerEntry.getKey()+"\">"+innerEntry.getKey()+"</a><strong> - ");
				builder.append("Positions: </strong>"+innerEntry.getValue().size());
				builder.append("<br>");
			}
			
			builder.append("<br>");
		}
		
		return builder.toString();
	}
	
	/**
	 * Builds HTML to output
	 * 
	 * @return the built HTML
	 * @throws IOException if an I/O error occurs
	 */
	public String wordCountToWeb() throws IOException {
		StringBuilder builder = new StringBuilder();
		
		Iterator<Entry<String, Integer>> iterator = counts.entrySet().iterator();
		
		while (iterator.hasNext()) {
			var entry = iterator.next();
			builder.append("<a href=\""+entry.getKey()+"\">"+entry.getKey()+"</a>");
			builder.append("<strong> - Word Count: </strong>"+Integer.toString(entry.getValue()));
			builder.append("<br>");
		}
		
		return builder.toString();
	}
	
	/**
	 * Writes pretty json to a specific path
	 *
	 * @param path the path to write to
	 * @throws IOException if an I/O error occurs
	 */
	public void wordCountToJson(Path path) throws IOException {
		SimpleJsonWriter.writeObjects(counts, path);
	}
	
	/**
	 * Determines whether the location is stored in the index.
	 *
	 * @param location the location to lookup
	 * @return {@true} if the word is stored in the index
	 */
	public boolean containsPath(String location) {
		return counts.containsKey(location);
	}
	
	/**
	 * Gets the word count for a location.
	 *
	 * @param location the location to lookup
	 * @return the word count
	 */
	public int getWordCount(String location) {
		return counts.get(location);
	}
	
	/**
	 * Determines whether the word is stored in the index.
	 *
	 * @param word the word to lookup
	 * @return {@true} if the word is stored in the index
	 */
	public boolean contains(String word) {
		return elements.containsKey(word);
	}

	/**
	 * Determines whether the location is stored in the word.
	 *
	 * @param word the word to lookup
	 * @param location the location of the word
	 * @return {@true} if the location is stored in the word
	 */
	public boolean contains(String word, String location) {
		if (contains(word)) {
			return elements.get(word).containsKey(location);
		}
		return false;
	}
	
	/**
	 * Determines whether the index is stored in the location.
	 *
	 * @param word the word to lookup
	 * @param location the location of the word
	 * @param index the index of the word in the location
	 * @return {@true} if the index is stored in the location
	 */
	public boolean contains(String word, String location, int index) {
		if (contains(word, location)) {
			return elements.get(word).get(location).contains(index);
		}
		return false;
	}

	/**
	 * Returns the number of strings.
	 *
	 * @return 0 if the data structure is empty, otherwise the number of strings in the
	 *   data structure
	 */
	public int size() {
		return elements.size();
	}
	
	/**
	 * Returns the number of locations for a given string.
	 * @param word string representing word
	 *
	 * @return 0 if the data structure is empty, otherwise the number of strings in the
	 *   data structure
	 */
	public int size(String word) {
		if (contains(word)) {
			return elements.get(word).size();
		}
		return 0;
	}
	
	/**
	 * Returns the number of locations for a given string.
	 * @param word string representing word
	 * @param location path the word is present in
	 *
	 * @return 0 if the data structure is empty, otherwise the number of strings in the
	 *   data structure
	 */
	public int size(String word, String location) {
		if (contains(word, location)) {
			return elements.get(word).get(location).size();
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return elements.toString();
	}
	
	/**
	 * Class responsible for creating search objects.
	 *
	 * @author Colin Bindi
	 * @author CS 272 Software Development (University of San Francisco)
	 * @version Spring 2022
	 */
	public class Result implements Comparable<Result> {
		/** The normalized text file path. */
		private final String location;
		
		/** The word match count */
		private int matches;
		
		/** The word match count */
		private double score;
		
		/**
		 * @param path the path of the object
		 */
		public Result(String path) {
			this.location = path;
		}
		
		/**
		 * Gets the location
		 * 
		 * @return location the path
		 */
		public String getLocation() {
			return location;
		}
		
		/**
		 * Gets the count
		 * 
		 * @return count the number of words in the path
		 */
		public int getCount() {
			return counts.get(location);
		}
		
		/**
		 * Gets the matches
		 * 
		 * @return matches the number of matches in the path
		 */
		public int getMatches() {
			return matches;
		}
		
		/**
		 * Gets the score
		 * 
		 * @return score the score of the number of matches out of the count
		 */
		public double getScore() {
			return score;
		}
		
		/**
		 * Updates the number of matches and the score
		 * 
		 * @param stem the stemmed word
		 */
		private void update(String stem) {
			this.matches += elements.get(stem).get(location).size();
			this.score = Double.valueOf(matches) / counts.get(location);
		}
		
		@Override
		public String toString() {
			return location;
		}
		
		@Override
		public int compareTo(Result o) {
			if (Double.compare(o.getScore(), this.getScore()) == 0) {
				if (Integer.compare(o.getCount(), this.getCount()) == 0) {
					return this.getLocation().compareToIgnoreCase(o.getLocation());
				} else {
					return Integer.compare(o.getCount(), this.getCount());
				}
			} else {
				return Double.compare(o.getScore(), this.getScore());
			}
		}
	}
}
