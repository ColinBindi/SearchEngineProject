package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class ThreadSafeInvertedIndexBuilder extends InvertedIndexBuilder {
	/** Logger used for this class. */
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * Traverses through the directory and its sub-directories, outputting all
	 * paths to the console. For files, also includes the file size in bytes.
	 *
	 * @param directory the directory to traverse
	 * @param index Nested data structure
	 * @param workQueue the work queue
	 * @throws IOException if an I/O error occurs
	 */
	private static void traverseDirectory(Path directory, ThreadSafeInvertedIndex index, WorkQueue workQueue) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
			for (Path path : listing) {
				if (Files.isDirectory(path)) {
					traverseDirectory(path, index, workQueue);
				}
				else if (isTextFile(path)) {
					Task task = new Task(path, index);
					workQueue.execute(task);
				}
			}
		}
	}
	
	/**
	 * The non-static task class that will update the shared set
	 * using the increment number.
	 */
	public static class Task implements Runnable {
		/** The set of primes. */
		private final Path path;
		
		/** The inverted index. */
		private final ThreadSafeInvertedIndex index;

		/**
		 * Initializes this task.
		 *
		 * @param path the path of a text file
		 * @param index the inverted index
		 */
		public Task(Path path, ThreadSafeInvertedIndex index) {
			this.path = path;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.parseFile(path, local);
				index.addAll(local);
			} catch (IOException e) {
				log.debug("IOException error at path: {}.", path);
			}
		}
	}
	
	/**
	 * Traverses through the directory and its sub-directories, outputting all
	 * paths to the console. For files, also includes the file size in bytes.
	 *
	 * @param start the initial path to traverse
	 * @param index Nested data structure
	 * @param workQueue the work queue
	 * @throws IOException if an I/O error occurs
	 */
	public static void build(Path start, ThreadSafeInvertedIndex index, WorkQueue workQueue) throws IOException {
		if (Files.isDirectory(start)) {
			traverseDirectory(start, index, workQueue);
		}
		else {
			Task task = new Task(start, index);
			workQueue.execute(task);
		}
		workQueue.finish();
	}
}
