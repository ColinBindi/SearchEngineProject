package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A thread-safe version of {@link InvertedIndex} using a read/write lock.
 *
 * @see InvertedIndex
 * @see SimpleReadWriteLock
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	/** The lock used to protect concurrent access to the underlying set. */
	private final SimpleReadWriteLock lock;

	/**
	 * Initializes a thread-safe indexed set.
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}

	/**
	 * Returns the identity hashcode of the lock object. Not particularly useful.
	 *
	 * @return the identity hashcode of the lock object
	 */
	public int lockCode() {
		return System.identityHashCode(lock);
	}
	
	@Override
	public void add(String word, String location, int index) {
		lock.writeLock().lock();

		try {
			super.add(word, location, index);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(List<String> words, String location) {
		lock.writeLock().lock();

		try {
			super.addAll(words, location);
		}
		finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void addAll(InvertedIndex index) {
		lock.writeLock().lock();

		try {
			super.addAll(index);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> exactSearch(Set<String> queries) {
		lock.readLock().lock();

		try {
			return super.exactSearch(queries);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public ArrayList<Result> partialSearch(Set<String> queries) {
		lock.readLock().lock();

		try {
			return super.partialSearch(queries);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<String> getWords() {
		lock.readLock().lock();

		try {
			return super.getWords();
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<String> getLocations(String word) {
		lock.readLock().lock();

		try {
			return super.getLocations(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<Integer> getIndices(String word, String location) {
		lock.readLock().lock();

		try {
			return super.getIndices(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void toJson(Path path) throws IOException {
		lock.readLock().lock();

		try {
			super.toJson(path);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void wordCountToJson(Path path) throws IOException {
		lock.readLock().lock();

		try {
			super.wordCountToJson(path);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean contains(String word) {
		lock.readLock().lock();

		try {
			return super.contains(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location) {
		lock.readLock().lock();

		try {
			return super.contains(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean contains(String word, String location, int index) {
		lock.readLock().lock();

		try {
			return super.contains(word, location, index);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size() {
		lock.readLock().lock();

		try {
			return super.size();
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public int size(String word) {
		lock.readLock().lock();

		try {
			return super.size(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public int size(String word, String location) {
		lock.readLock().lock();

		try {
			return super.size(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String toString() {
		lock.readLock().lock();

		try {
			return super.toString();
		}
		finally {
			lock.readLock().unlock();
		}
	}
}
