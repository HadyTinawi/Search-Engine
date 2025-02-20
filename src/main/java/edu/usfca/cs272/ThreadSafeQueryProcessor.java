package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * 
 */
public class ThreadSafeQueryProcessor {

	/**
	 * Stores search results mapped from query strings to lists of search results.
	 * Each key is a unique query string, and the value is a list of
	 * {@link InvertedIndex.SearchResult} objects that represent the search outcomes
	 * for that query.
	 */
	private final Map<String, List<SearchResult>> searchResults;
	// private final Map<String, List<ThreadSafeInvertedIndex.SearchResult>> result;

	/**
	 * The inverted index used to perform searches. This index is a complex data
	 * structure that maps words to their locations in documents, allowing for
	 * efficient search and retrieval operations.
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * The boolean used to help determine which search is to be done.
	 */
	private final boolean isPartial;

	/**
	 * The stemmer used for reducing words to their base or root form. This aids in
	 * normalizing the search queries to increase the effectiveness of matching
	 * terms in the inverted index.
	 */
	private final Stemmer stemmer;

	private WorkQueue Queue;

	private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();
	/**
	 * Workqueue queue
	 */
	// private WorkQueue Queue;

	/**
	 * Initializes a new QueryProcessor with a specific inverted index. It sets up
	 * the stemmer to the English language using the Snowball stemming algorithm,
	 * which is effective for processing and normalizing English text.
	 *
	 * @param index The inverted index to be used for processing search queries.
	 * @param isPartial boolean if partial search is requireWo
	 * @param Queue The workqueue
	 */
	public ThreadSafeQueryProcessor(ThreadSafeInvertedIndex index, boolean isPartial, WorkQueue Queue) {
		if (Queue == null) {
			throw new IllegalArgumentException("WorkQueue cannot be null\n");
		}
		this.index = index;
		this.isPartial = isPartial;
		this.Queue = Queue;
		this.searchResults = new TreeMap<>();
		this.stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
	}

	/**
	 * 
	 * Processes a query file and uses the provided inverted index to perform
	 * searches based on the content of the file. Results are stored in the inverted
	 * index.
	 *
	 * @param queryPath the path to the query file
	 * @throws IOException if an error occurs reading from the file
	 */
	public void processQueryFile(Path queryPath) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(queryPath, StandardCharsets.UTF_8)) {
			String line;
			// int i = 0;
			while ((line = reader.readLine()) != null) {
				Queue.execute(new QueryTask(line));
			}
		}
		Queue.finish();
	}

	/**
	 * Processes a single line of query text, performs a search using the inverted
	 * index, and stores the results. This method uses a stemmer to process words,
	 * forms a query string from the stemmed words, and avoids redundant searches by
	 * checking if the query has already been processed.
	 *
	 * @param line The line of text to be processed as a search query.
	 */
	// CITE: Param
	public void processQueryLine(String line) {
		TreeSet<String> queryWords = FileStemmer.uniqueStems(line, stemmer);
		String query = String.join(" ", queryWords);

		synchronized (searchResults) {
			if (searchResults.containsKey(query) || query.isEmpty()) {
				return;
			}
		}

		List<ThreadSafeInvertedIndex.SearchResult> results = index.search(queryWords, isPartial);

		synchronized (searchResults) {
			searchResults.put(query, results);
		}
	}

	/**
	 * Writes the stored search results to a file in JSON format. This method
	 * utilizes the {@link JsonWriter} class to output the search results, where
	 * each query is linked to its corresponding search results.
	 *
	 * @param path The path of the file where the search results will be written.
	 * @throws IOException If an I/O error occurs while writing to the file.
	 */
	public void writeResults(Path path) throws IOException {
		synchronized (searchResults) {
			JsonWriter.writeSearchResultsMap(searchResults, path);
		}
	}

	/**
	 * Provides an unmodifiable view of the search results for a specific query
	 * after stemming the query. This allows clients to read details about the
	 * search outcomes for a particular query without the ability to modify the list
	 * of results.
	 *
	 * @param query The raw, unstemmed query string whose results are to be viewed.
	 * @return an unmodifiable list of SearchResult objects associated with the
	 *   stemmed version of the given query, or an empty list if the query is not
	 *   found.
	 */
	public List<SearchResult> viewSearchResultsForQuery(String query) {
		TreeSet<String> stemmedWords = FileStemmer.uniqueStems(query, stemmer);
		String processedQuery = String.join(" ", stemmedWords);

		synchronized (searchResults) {
			if (searchResults.containsKey(processedQuery)) {
				return Collections.unmodifiableList(searchResults.get(processedQuery));
			}
			return Collections.emptyList();
		}
	}

	/**
	 * Provides an unmodifiable view of all query strings that have search results
	 * stored in the processor. This method allows safe read-only access to the keys
	 * of the search results map, ensuring no external modifications.
	 *
	 * @return an unmodifiable set of query strings for which there are search
	 *   results.
	 */
	public Set<String> viewQueries() {
		synchronized (searchResults) {
			return Collections.unmodifiableSet(searchResults.keySet());
		}
	}

	/**
	 * Returns a string representation of the search results currently stored in the
	 * QueryProcessor. This can be useful for logging or debugging purposes to see
	 * the contents of the search results map.
	 *
	 * @return A string representation of the search results.
	 */
	@Override
	public String toString() {
		return searchResults.toString();
	}

	private class QueryTask implements Runnable {
		private final String queryLine;

		public QueryTask(String queryLine) {
			this.queryLine = queryLine;
//			log.debug("QueryProc:Created task for query line: {}", queryLine);
		}

		// CITE: Param
		@Override
		public void run() {
			try {
				if (queryLine == null || queryLine.isEmpty()) {
					return;
				}
				Stemmer localStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
				TreeSet<String> queryWords = FileStemmer.uniqueStems(queryLine, localStemmer);
				
				String query = String.join(" ", queryWords);
				synchronized (searchResults) {
					if (searchResults.containsKey(query) || query.isEmpty()) {
						return;
					}
				}
				List<ThreadSafeInvertedIndex.SearchResult> results = index.search(queryWords, isPartial);

				synchronized (searchResults) {
					searchResults.put(query, results);
				}
			}
			catch (Exception e) {
				System.err.println("Error in QueryTask: " + e.getMessage());
				throw new RuntimeException(e);

			}
		}
	}
}
