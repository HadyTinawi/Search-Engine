package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * 
 */
public class MultithreadedInvertedIndexBuilder {

	/**
	 * Builds an inverted index from a given path. If the path is a directory, it
	 * will traverses the directory, otherwise it processes the file.
	 *
	 * @param textPath the path to the file or directory
	 * @param invertedIndex the inverted index to build
	 * @throws IOException if an I/O error occurs (reading from the file or
	 *   directory)
	 */

	public static void build(Path textPath, ThreadSafeInvertedIndex threadSafeInvertedIndex, WorkQueue workQueue)
			throws IOException {
		if (Files.isDirectory(textPath)) {
			traverseDirectory(textPath, threadSafeInvertedIndex, workQueue);
		}
		else {
			processFile(textPath, threadSafeInvertedIndex, workQueue);
		}
		workQueue.finish();
	}

	/**
	 * Processes a single file and adds its content to the provided inverted index.
	 *
	 * @param file the file to process
	 * @param invertedIndex the inverted index to add content to
	 * @throws IOException if an I/O error occurs reading from the file
	 */
	public static void processFile(Path file, ThreadSafeInvertedIndex threadSafeInvertedIndex, WorkQueue workQueue)
			throws IOException {
		workQueue.execute(new Process(file, threadSafeInvertedIndex));
	}

	/**
	 * Traverses a directory and processes all text files found within it using a
	 * directory stream
	 *
	 * @param directory the directory to traverse
	 * @param index the inverted index to add content to
	 * @throws IOException if an I/O error occurs reading from the directory
	 */

	public static void traverseDirectory(Path directory, ThreadSafeInvertedIndex index, WorkQueue workQueue)
			throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					traverseDirectory(entry, index, workQueue);
				}
				else if (isTextFile(entry)) {
					processFile(entry, index, workQueue);
				}
			}
		}
	}

	/**
	 * Checks if a given path corresponds to a text file.
	 *
	 * @param path the path to check
	 * @return true if the path is a text file, false otherwise
	 */

	public static boolean isTextFile(Path path) {
		String pathString = path.toString().toLowerCase();
		return pathString.endsWith(".txt") || pathString.endsWith(".text");
	}

	private static class Process implements Runnable {
		private Path p;
		private ThreadSafeInvertedIndex threadSafeInvertedIndex;

		public Process(Path p, ThreadSafeInvertedIndex threadSafeInvertedIndex) {
			this.p = p;
			this.threadSafeInvertedIndex = threadSafeInvertedIndex;
		}

		@Override
		public void run() {
			try {
				ArrayList<String> stems = FileStemmer.listStems(p);
				String pathOfFile = p.toString();
				threadSafeInvertedIndex.addAllStems(stems, pathOfFile, 1);
			}
			catch (Exception e) {
				System.out.println("Error processing file: " + p.toString());
			}
		}
	}
}
