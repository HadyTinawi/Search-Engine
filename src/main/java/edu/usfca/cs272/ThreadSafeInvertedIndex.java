package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	private static final Logger log = LogManager.getLogger();
	/**
	 * 
	 */
	private final MultiReaderLock lock;

	/**
	 * Includes logic of an inverted index for efficient word searching in text
	 * files. It maps a word to its locations (file paths and positions within those
	 * files).
	 */

	/**
	 * Initializes a new inverted index with empty structures.
	 */

	public ThreadSafeInvertedIndex() {
		this.lock = new MultiReaderLock();
	}

	// CITE: Tutoring Center
	@Override
	public void merge(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.merge(other);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Writes the inverted index to a file in JSON format.
	 *
	 * @param path The path of the file to write the inverted index to.
	 * @throws IOException If an I/O error occurs writing to or creating the file.
	 */
	@Override
	public void writeIndex(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeIndex(path);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Writes the word counts to a file in JSON format.
	 *
	 * @param path The path of the file to write the word counts to.
	 * @throws IOException If an I/O error occurs writing to or creating the file.
	 */
	@Override
	public void writeCounts(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeCounts(path);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs a search on the inverted index using the provided query words. This
	 * method can perform either an exact or partial search based on the boolean
	 * flag provided.
	 *
	 * @param queryWords A set of words to be searched within the index.
	 * @param isPartial If true, the method performs a partial search, where search
	 *   terms need only start with the query words. If false, the method performs
	 *   an exact search, where search terms must exactly match the query words.
	 * @return A list of {@link SearchResult} objects, each representing a search
	 *   result with details about where and how frequently the search terms were
	 *   found.
	 */

	@Override
	public List<SearchResult> search(Set<String> queryWords, boolean isPartial) {
		return isPartial ? partialSearch(queryWords) : exactSearch(queryWords);
	}

	/**
	 * Performs an exact search on the inverted index using the given query words.
	 * It searches for exact matches of the query words within the indexed
	 * documents. If a word from the query is found, it updates or adds a new
	 * SearchResult based on whether the file path is already present in the result
	 * list.
	 *
	 * @param queryWords Set of query words to search for exact matches
	 * @return List of search results, each containing file path, number of
	 *   occurrences, and relevance score, sorted by relevance score, then number of
	 *   occurrences, and finally by file path lexicographically.
	 */
	@Override
	public List<SearchResult> exactSearch(Set<String> queryWords) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queryWords);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs a partial search on the inverted index using the given query words.
	 * This method matches any indexed words that start with any of the provided
	 * query words. Updates existing search results if they are found in the result
	 * list or adds new results if not found.
	 *
	 * @param queryWords Set of query words to search for partial matches
	 * @return List of search results, each containing file path, number of
	 *   occurrences, and relevance score, sorted by relevance score, then number of
	 *   occurrences, and finally by file path lexicographically.
	 *   System.out.println("Exiting search, now Index: in partialSearch");
	 *   System.out.println("Index: attempting to acquire read lock for partial
	 *   search"); System.out.println("Index:acquired read lock for partial
	 *   search"); System.out.println("Returning super.partialSearch()");
	 *   System.out.println("Unlocking locks, exiting partialSearch");
	 */
	@Override
	public List<SearchResult> partialSearch(Set<String> queryWords) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queryWords);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Updates the search results map with new or existing search results based on
	 * the provided file path and positions. This method checks if a search result
	 * for a given file path already exists in the lookup map. If it exists, it
	 * updates the occurrence count; if not, it creates a new SearchResult and adds
	 * it to the map and the results list.
	 *
	 * @param lookup The map used for looking up and storing {@link SearchResult}
	 *   instances by file path.
	 * @param results The list of search results that might be updated with new
	 *   SearchResult instances.
	 * @param files The map of files to positions for a specific word.
	 */
	@Override
	protected void updateSearchResults(Map<String, SearchResult> lookup, List<SearchResult> results,
			TreeMap<String, TreeSet<Integer>> files) {
		super.updateSearchResults(lookup, results, files);
	}

	/**
	 * Checks if the specified word is present in the index.
	 *
	 * @param word the word to check
	 * @return true if the word is present, false otherwise
	 */
	@Override
	public boolean hasWord(String word) {
		lock.readLock().lock();
		try {
			return super.hasWord(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks if a specific word is found in a given location.
	 *
	 * @param word the word to check
	 * @param location the file location
	 * @return true if the word is present in the specified location
	 */
	@Override
	public boolean hasLocation(String word, String location) {
		lock.readLock().lock();
		try {
			return super.hasLocation(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks if a specific word at a given location has the specified position.
	 *
	 * @param word the word to check
	 * @param location the file location
	 * @param position the position in the file
	 * @return true if the word is found at the specified position in the given
	 *   location
	 */
	@Override
	public boolean hasPosition(String word, String location, Integer position) {
		lock.readLock().lock();
		try {
			return super.hasPosition(word, location, position);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks if the word counts map contains a count for the specified location.
	 *
	 * @param location the location to check
	 * @return true if there is a count for the location
	 */
	@Override
	public boolean hasCount(String location) {
		lock.readLock().lock();
		try {
			return super.hasCount(location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Thread-safe version of addWords that locks once for the entire operation
	 *
	 * @param stems List of stems to add
	 * @param location The file path where the words are found
	 * @param startPosition The position of the first word in the file
	 */
	public void addAllStems(ArrayList<String> stems, String location, int startPosition) {
		lock.writeLock().lock();
		try {
			int position = startPosition;
			for (String word : stems) {
				super.addWord(word, location, position);
				position++; // Increment position for each word
			}
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Adds a word along with its file location and position within that file to the
	 * inverted index.
	 * 
	 * @param word the word to add
	 * @param location the file path where the word was found
	 * @param position the position of the word in the file
	 */
	@Override
	public void addWord(String word, String location, int position) {
		lock.writeLock().lock();
		try {
			super.addWord(word, location, position);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Adds multiple words to the inverted index at sequential positions within the
	 * same file.
	 *
	 * @param words An array of words to add.
	 * @param location The file path where the words are found.
	 * @param startPosition The position of the first word in the file.
	 */
	@Override
	public void addWords(String[] words, String location, int startPosition) {
		lock.writeLock().lock();
		try {
			super.addWords(words, location, startPosition);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Counts the total number of positions a word appears in the index across all
	 * files.
	 *
	 * @param word The word to check.
	 * @param location the location
	 * @return The total number of positions the word is found at across all files.
	 */
	@Override
	public int numPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.numPositions(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets the number of words in the inverted index.
	 *
	 * @return the number of words
	 */
	@Override
	public int numWords() {
		lock.readLock().lock();
		try {
			return super.numWords();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Counts the locations a word appears in the index.
	 *
	 * @param word The word to check.
	 * @return Number of locations where the word is found.
	 */
	@Override
	public int numLocations(String word) {
		lock.readLock().lock();
		try {
			return super.numLocations(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * returns the wordCounts map
	 * 
	 * @return map containing words
	 */
	@Override
	public int numCounts() {
		lock.readLock().lock();
		try {
			return super.numCounts();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of the words in the inverted index.
	 *
	 * @return an unmodifiable set of the index keys (words).
	 */
	@Override
	public Set<String> viewWords() {
		lock.readLock().lock();
		try {
			return super.viewWords();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of file paths for a given word in the inverted
	 * index.
	 *
	 * @param word the word to get the file paths for.
	 * @return an unmodifiable set of the inner map keys (file paths) for the given
	 *   word.
	 */
	@Override
	public Set<String> viewLocations(String word) {
		lock.readLock().lock();
		try {
			return super.viewLocations(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of positions of a word in a specific file.
	 *
	 * @param word the word to get positions for.
	 * @param location the location (file path) to get positions in.
	 * @return an unmodifiable set of the positions where the word appears in the
	 *   specified file.
	 */
	@Override
	public Set<Integer> viewPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.viewPositions(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of the counts in the word counts map.
	 *
	 * @return an unmodifiable view of the word counts map.
	 */
	@Override
	public Map<String, Integer> viewCounts() {
		lock.readLock().lock();
		try {
			return super.viewCounts();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets the total count of words for a given file path.
	 *
	 * @param path the file path
	 * @return the total count of words in the file
	 */
	@Override
	public int getWordCount(String path) {
		lock.readLock().lock();
		try {
			return super.getWordCount(path);
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
