package edu.usfca.cs272;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main driver class. This class parses command-line arguments to build an
 * inverted index and outputs the index or word counts to JSON files.
 */
public class Driver {
	// private static final org.apache.logging.log4j.Logger log =
	// LogManager.getLogger();

	/**
	 * @param args command-line arguments that dictate the operation of the program.
	 *   Supported flags include: - "-text": Path to the file or directory to index.
	 *   - "-counts": Path for the JSON file to output word counts. - "-index": Path
	 *   for the JSON file to output the inverted index.
	 */

	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws URISyntaxException {

		// Store initial start time
		Instant start = Instant.now();
		ArgumentParser parser = new ArgumentParser(args);
		boolean isThreaded = parser.hasFlag("-threads") || parser.hasFlag("-html");

		// Use the new InvertedIndex class
		InvertedIndex invertedIndex;
		QueryProcessor processor = null;
		ThreadSafeQueryProcessor threadSafeProcessor = null;
//		WebCrawler crawler = null;

		if (isThreaded) {
			log.debug("Entering Multithreading");
			int numThreads = 5; // Default number of threads
			try {
				numThreads = parser.getInteger("-threads", 5);
				if (numThreads <= 0) {
					numThreads = 5;
				}
			}
			catch (NumberFormatException e) {
				numThreads = 5;
			}

			WorkQueue queue = new WorkQueue(numThreads);
			boolean isPartial = parser.hasFlag("-partial");
			invertedIndex = new ThreadSafeInvertedIndex();
			threadSafeProcessor = new ThreadSafeQueryProcessor((ThreadSafeInvertedIndex) invertedIndex, isPartial, queue);

			// Multi Threading
			try {
				// Handle web crawling if -html flag is present
				if (parser.hasFlag("-html")) {
					String seed = parser.getString("-html");
					int crawls = 1; // Default to 1
					if (parser.hasFlag("-crawl")) {
						crawls = parser.getInteger("-crawl", 1);
						if (crawls < 1) {
							crawls = 1;
						}
					}
					WebCrawler crawler = new WebCrawler((ThreadSafeInvertedIndex) invertedIndex, queue, crawls);
					if (!parser.hasValue("-html")) {
						System.out.println("-html flag present but has no value");
					}
					else {
						try {
							crawler.crawl(seed);
						}
						catch (Exception e) {
							System.out.println("Error crawling web. (-html flag)");
						}
					}

				}
				// Handle text processing
				if (parser.hasFlag("-text")) {
					Path textPath = parser.getPath("-text");
					if (textPath != null) {
						MultithreadedInvertedIndexBuilder.build(textPath, (ThreadSafeInvertedIndex) invertedIndex, queue);
					}
				}

				// Handle query processing
				if (parser.hasFlag("-query")) {
					Path queryPath = parser.getPath("-query");
					if (queryPath == null) {
						System.out.println("Error: Invalid or missing query path.");
						return;
					}
					threadSafeProcessor.processQueryFile(queryPath);
				}

				// Handle results writing
				if (parser.hasFlag("-results")) {
					Path resultsPath = parser.getPath("-results", Path.of("results.json"));
					threadSafeProcessor.writeResults(resultsPath);
				}

				// Handle counts output
				if (parser.hasFlag("-counts")) {
					Path countsPath = parser.getPath("-counts", Path.of("counts.json"));
					invertedIndex.writeCounts(countsPath);
				}

				// Handle index output
				if (parser.hasFlag("-index")) {
					Path indexPath = parser.getPath("-index", Path.of("index.json"));
					invertedIndex.writeIndex(indexPath);
				}
			}
			catch (Exception e) {
				System.err.println("Error during threaded processing: " + e.getMessage());
			}
			finally {
//			// Make sure to finish the work queue
				queue.join();
//			}
			}
		}
		// Single Threading
		else {
			invertedIndex = new InvertedIndex();
			boolean isPartial = parser.hasFlag("-partial");
			processor = new QueryProcessor(invertedIndex, isPartial);

			try {
				// Handle web crawling if -html flag is present
				if (parser.hasFlag("-html")) {
					String seed = parser.getString("-html");
					WorkQueue singleQueue = new WorkQueue(1);
					WebCrawler crawler = new WebCrawler((ThreadSafeInvertedIndex)invertedIndex, singleQueue, 1);
					if (!parser.hasValue("-html")) {
						System.out.println("-html flag present but has no value");
					}
					else {
						try {
							crawler.crawl(seed);
						}
						catch (Exception e) {
							System.out.println("Error crawling web. (-html flag)");
						}
					}
					singleQueue.shutdown();
				}
				// Handle text processing
				if (parser.hasFlag("-text")) {
					Path textPath = parser.getPath("-text");
					if (textPath != null) {
						InvertedIndexBuilder.build(textPath, invertedIndex);
					}
				}

				// Handle query processing
				if (parser.hasFlag("-query")) {
					Path queryPath = parser.getPath("-query");
					if (queryPath == null) {
						System.out.println("Error: Invalid or missing query path.");
						return;
					}
					processor.processQueryFile(queryPath);
				}

				// Handle results writing
				if (parser.hasFlag("-results")) {
					Path resultsPath = parser.getPath("-results", Path.of("results.json"));
					processor.writeResults(resultsPath);
				}

				// Handle counts output
				if (parser.hasFlag("-counts")) {
					Path countsPath = parser.getPath("-counts", Path.of("counts.json"));
					invertedIndex.writeCounts(countsPath);
				}

				// Handle index output
				if (parser.hasFlag("-index")) {
					Path indexPath = parser.getPath("-index", Path.of("index.json"));
					invertedIndex.writeIndex(indexPath);
				}
			}
			catch (IOException e) {
				System.err.println("Error during non-threaded processing: " + e.getMessage());
			}
		}

		// Calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);

	}
}
