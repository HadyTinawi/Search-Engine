package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Includes logic of an inverted index for efficient word searching in text
 * files. It maps a word to its locations (file paths and positions within those
 * files).
 */

public class InvertedIndex {
	/**
	 * A map of words to the number of times they appear across all files.
	 *
	 */
	private final TreeMap<String, Integer> wordCounts;

	/**
	 * A nested map where each word maps to another map, which in turn maps a file
	 * path to a set of positions where the word occurs.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/**
	 * Initializes a new inverted index with empty structures.
	 */

	public InvertedIndex() {
		this.wordCounts = new TreeMap<>();
		this.invertedIndex = new TreeMap<>();
	}
// CITE: Tutoring Center
	public void merge(InvertedIndex other) {
		for (var entry : other.invertedIndex.entrySet()) {
			var thisLocations = this.invertedIndex.get(entry.getKey());
			if (thisLocations == null) {
				this.invertedIndex.put(entry.getKey(), entry.getValue());
			} else {
				for (var locationEntry : entry.getValue().entrySet()) {
					var thisIndex = thisLocations.get(locationEntry.getKey());
					if (thisIndex == null) {
						thisLocations.put(locationEntry.getKey(), locationEntry.getValue());
					} else {
						thisIndex.addAll(locationEntry.getValue());
					}
				}
			}
		}
		for (var entry : other.wordCounts.entrySet()) {
			this.wordCounts.merge(entry.getKey(), entry.getValue(),
					(current, newer) -> (newer > current) ? newer : current);
		}
	}

	/**
	 * Writes the inverted index to a file in JSON format.
	 *
	 * @param path The path of the file to write the inverted index to.
	 * @throws IOException If an I/O error occurs writing to or creating the file.
	 */
	public void writeIndex(Path path) throws IOException {
		if (path != null) {
			JsonWriter.writeNestedMap(this.invertedIndex, path);
		}
	}

	/**
	 * Writes the word counts to a file in JSON format.
	 *
	 * @param path The path of the file to write the word counts to.
	 * @throws IOException If an I/O error occurs writing to or creating the file.
	 */
	public void writeCounts(Path path) throws IOException {
		if (path != null) {
			JsonWriter.writeObject(this.wordCounts, path);
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
	public List<SearchResult> search(Set<String> queryWords, boolean isPartial) {
		return (isPartial) ? partialSearch(queryWords) : exactSearch(queryWords);
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
	public List<SearchResult> exactSearch(Set<String> queryWords) {
		Map<String, SearchResult> lookup = new HashMap<>();
		List<SearchResult> results = new ArrayList<>();
		for (String word : queryWords) {
			if (invertedIndex.containsKey(word)) {
				updateSearchResults(lookup, results, invertedIndex.get(word));
			}
		}
		Collections.sort(results);
		return results;
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
	 */
	public List<SearchResult> partialSearch(Set<String> queryWords) {
		Map<String, SearchResult> lookup = new HashMap<>();
		List<SearchResult> results = new ArrayList<>();

		for (String queryWord : queryWords) {
			Map.Entry<String, TreeMap<String, TreeSet<Integer>>> entry = invertedIndex.ceilingEntry(queryWord);
			while (entry != null && entry.getKey().startsWith(queryWord)) {
				updateSearchResults(lookup, results, entry.getValue());
				entry = invertedIndex.higherEntry(entry.getKey());
			}
		}
		Collections.sort(results);
		return results;
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
	protected void updateSearchResults(Map<String, SearchResult> lookup, List<SearchResult> results,
			TreeMap<String, TreeSet<Integer>> files) {
		for (Map.Entry<String, TreeSet<Integer>> entry : files.entrySet()) {
			String path = entry.getKey();
			TreeSet<Integer> positions = entry.getValue();
			SearchResult result = lookup.get(path);
			if (result == null) {
				result = new SearchResult(path);
				lookup.put(path, result);
				results.add(result);
			}
			result.updateCount(positions.size());
		}
	}

	/**
	 * Checks if the specified word is present in the index.
	 *
	 * @param word the word to check
	 * @return true if the word is present, false otherwise
	 */
	public boolean hasWord(String word) {
		return viewWords().contains(word);
	}

	/**
	 * Checks if a specific word is found in a given location.
	 *
	 * @param word the word to check
	 * @param location the file location
	 * @return true if the word is present in the specified location
	 */
	public boolean hasLocation(String word, String location) {
		return viewLocations(word).contains(location);
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
	public boolean hasPosition(String word, String location, Integer position) {
		return viewPositions(word, location).contains(position);
	}

	/**
	 * Checks if the word counts map contains a count for the specified location.
	 *
	 * @param location the location to check
	 * @return true if there is a count for the location
	 */

	public boolean hasCount(String location) {
		return viewCounts().containsKey(location);
	}

	/**
	 * Adds a word along with its file location and position within that file to the
	 * inverted index.
	 * 
	 * @param word the word to add
	 * @param location the file path where the word was found
	 * @param position the position of the word in the file
	 */
	public void addWord(String word, String location, int position) {
		Map<String, TreeSet<Integer>> locationMap = invertedIndex.computeIfAbsent(word, k -> new TreeMap<>());
		Set<Integer> positionsSet = locationMap.computeIfAbsent(location, k -> new TreeSet<>());
		boolean isAdded = positionsSet.add(position);
		if (isAdded) {
			wordCounts.put(location, wordCounts.getOrDefault(location, 0) + 1);
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
	public void addWords(String[] words, String location, int startPosition) {
		int position = startPosition;
		for (String word : words) {
			addWord(word, location, position);
			position++; // Increment position for each word
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
	public int numPositions(String word, String location) {
		return viewPositions(word, location).size();
	}

	/**
	 * Gets the number of words in the inverted index.
	 *
	 * @return the number of words
	 */

	public int numWords() {
		return viewWords().size();
	}

	/**
	 * Counts the locations a word appears in the index.
	 *
	 * @param word The word to check.
	 * @return Number of locations where the word is found.
	 */
	public int numLocations(String word) {
		return viewLocations(word).size();
	}

	/**
	 * returns the wordCounts map
	 * 
	 * @return map containing words
	 */

	public int numCounts() {
		return viewCounts().size();
	}

	/**
	 * Returns an unmodifiable view of the words in the inverted index.
	 *
	 * @return an unmodifiable set of the index keys (words).
	 */

	public Set<String> viewWords() {
		return Collections.unmodifiableSet(invertedIndex.keySet());
	}

	/**
	 * Returns an unmodifiable view of file paths for a given word in the inverted
	 * index.
	 *
	 * @param word the word to get the file paths for.
	 * @return an unmodifiable set of the inner map keys (file paths) for the given
	 *   word.
	 */

	public Set<String> viewLocations(String word) {
		TreeMap<String, TreeSet<Integer>> locations = invertedIndex.get(word);
		if (locations != null) {
			return Collections.unmodifiableSet(locations.keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of positions of a word in a specific file.
	 *
	 * @param word the word to get positions for.
	 * @param location the location (file path) to get positions in.
	 * @return an unmodifiable set of the positions where the word appears in the
	 *   specified file.
	 */
	public Set<Integer> viewPositions(String word, String location) {
		TreeMap<String, TreeSet<Integer>> locations = invertedIndex.get(word);
		if (locations != null) {
			TreeSet<Integer> positions = locations.get(location);
			if (positions != null) {
				return Collections.unmodifiableSet(positions);
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of the counts in the word counts map.
	 *
	 * @return an unmodifiable view of the word counts map.
	 */

	public Map<String, Integer> viewCounts() {
		return Collections.unmodifiableMap(wordCounts);
	}

	/**
	 * Gets the total count of words for a given file path.
	 *
	 * @param path the file path
	 * @return the total count of words in the file
	 */
	public int getWordCount(String path) {
		return wordCounts.getOrDefault(path, 0);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Entry<String, TreeMap<String, TreeSet<Integer>>> entry : invertedIndex.entrySet()) {
			builder.append(entry.getKey()).append(": ");
			builder.append(entry.getValue().toString());
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}

	/**
	 * Represents a search result, encapsulating the file path where the search term
	 * was found, the number of occurrences of the search term, and the score based
	 * on search relevance.
	 */
	public class SearchResult implements Comparable<SearchResult> {
		/**
		 * The file path where the search term was found.
		 */
		private final String where;
		/**
		 * The number of occurrences of the search term found at the file path.
		 */
		private int count;
		/**
		 * The relevance score based on the number of occurrences relative to the total
		 * number of words in the document.
		 */
		private double score;

		/**
		 * Constructs a SearchResult object for a given file path.
		 *
		 * @param where the file path of the search result
		 */
		public SearchResult(String where) {
			this.where = where;
			this.count = 0;
			this.score = 0.0;
		}

		/**
		 * Updates the number of occurrences and recalculates the score.
		 *
		 * @param additionalCount the additional number of occurrences to add
		 */
		private void updateCount(int additionalCount) {
			this.count += additionalCount;
			this.score = (double) this.count / wordCounts.get(where);
		}

		/**
		 * Gets the file path of the search result.
		 *
		 * @return the file path where the search term was found
		 */
		public String getWhere() {
			return where;
		}

		/**
		 * Gets the number of occurrences of the search term.
		 *
		 * @return the count of the search term in the document
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Gets the score of the search result.
		 *
		 * @return the relevance score of the search result
		 */
		public double getScore() {
			return score;
		}

		@Override
		public int compareTo(SearchResult other) {
			int score = Double.compare(other.score, this.score);
			if (score != 0) {
				return score;
			}
			int count = Double.compare(other.count, this.count);
			if (count != 0) {
				return count;
			}
			return where.compareToIgnoreCase(other.where);
		}

	}
}
