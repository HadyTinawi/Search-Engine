package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class JsonWriter {
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
			writer.write("  ");
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
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
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

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	// CITE: Had Malik help me understand how to tackle the problem
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("[\n");
		var iterator = elements.iterator();

		if (iterator.hasNext()) {
			Number firstElement = iterator.next();
			writeIndent(firstElement.toString(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",\n");
			Number element = iterator.next();
			writeIndent(element.toString(), writer, indent + 1);
		}

		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
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
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */

	// CITE: The lectures and Asynchronous lessons
	// CITE: The choice of enhanced for loops (Sophie's advice in class)

	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("{\n");
		var iterator = elements.entrySet().iterator();

		if (iterator.hasNext()) {
			var entry = iterator.next();
			writeIndent("\"" + entry.getKey() + "\": " + entry.getValue(), writer, indent + 1);

			while (iterator.hasNext()) {
				writer.write(",\n");
				entry = iterator.next();
				writeIndent("\"" + entry.getKey() + "\": " + entry.getValue(), writer, indent + 1);
			}
			writer.write("\n");
		}
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */

	// CITE: had GPT help with the if condition (getting used to working with
	// generic collections)
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("{\n");
		var iterator = elements.entrySet().iterator();

		if (iterator.hasNext()) {
			var entry = iterator.next();
			writeIndent("\"" + entry.getKey() + "\": ", writer, indent + 1);
			writeArray(entry.getValue(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",\n");
			var entry = iterator.next();
			writeIndent("\"" + entry.getKey() + "\": ", writer, indent + 1);
			writeArray(entry.getValue(), writer, indent + 1);
		}

		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("[\n");
		var iterator = elements.iterator();

		if (iterator.hasNext()) {
			writer.write("{\n");
			Map<String, ? extends Number> map = iterator.next();
			var mapIterator = map.entrySet().iterator();

			if (mapIterator.hasNext()) {
				var entry = mapIterator.next();
				writeIndent("\"" + entry.getKey() + "\": " + entry.getValue(), writer, indent + 2);
			}

			while (mapIterator.hasNext()) {
				writer.write(",\n");
				var entry = mapIterator.next();
				writeIndent("\"" + entry.getKey() + "\": " + entry.getValue(), writer, indent + 2);
			}

			writer.write("\n");
			writeIndent("}", writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",\n");
			writer.write("{\n");
			Map<String, ? extends Number> map = iterator.next();

			var mapIterator = map.entrySet().iterator();
			if (mapIterator.hasNext()) {
				var entry = mapIterator.next();
				writeIndent("\"" + entry.getKey() + "\": " + entry.getValue(), writer, indent + 2);
			}

			while (mapIterator.hasNext()) {
				writer.write(",\n");
				var entry = mapIterator.next();
				writeIndent("\"" + entry.getKey() + "\": " + entry.getValue(), writer, indent + 2);
			}

			writer.write("\n");
			writeIndent("}", writer, indent + 1);
		}

		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the nested map as a pretty JSON object with nested objects to file.
	 *
	 * @param index the nested map to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeNestedMap(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> index,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeNestedMap(index, writer, 0);
		}
	}

	/**
	 * Writes the nested map as a pretty JSON object with nested objects. This
	 * method is used to write a more complex structure where each key in the map
	 * contains another map, and each of those maps contain a set of integers.
	 *
	 * @param elements the nested map to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeNestedMap(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements,
			Writer writer, int indent) throws IOException {

		if (elements.isEmpty()) {
			writer.write("{\n}");
			return;
		}

		writer.write("{\n");
		var outerIterator = elements.entrySet().iterator();

		while (outerIterator.hasNext()) {
			var outerEntry = outerIterator.next();
			writeQuote(outerEntry.getKey(), writer, indent + 1);
			writer.write(": ");

			if (outerEntry.getValue().isEmpty()) {
				writer.write("{}");
			}
			else {
				writer.write("{\n");
				var innerIterator = outerEntry.getValue().entrySet().iterator();

				if (innerIterator.hasNext()) {
					var innerEntry = innerIterator.next();
					writeQuote(innerEntry.getKey(), writer, indent + 2);
					writer.write(": ");
					writeArray(innerEntry.getValue(), writer, indent + 2);
				}

				while (innerIterator.hasNext()) {
					writer.write(",\n");
					var innerEntry = innerIterator.next();
					writeQuote(innerEntry.getKey(), writer, indent + 2);
					writer.write(": ");
					writeArray(innerEntry.getValue(), writer, indent + 2);
				}

				writer.write("\n");
				writeIndent("}", writer, indent + 1);
			}

			if (outerIterator.hasNext()) {
				writer.write(",\n");
			}
		}

		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes a list of {@link InvertedIndex.SearchResult} objects to a provided
	 * {@link BufferedWriter} in a JSON array format. The method formats each search
	 * result with proper indentation, adding commas between objects and handling
	 * the bracket closure correctly.
	 *
	 * @param results the list of {@link InvertedIndex.SearchResult} objects to
	 *   write
	 * @param writer the {@link BufferedWriter} to use for writing the data
	 * @param indent the initial indentation level to use for the array elements
	 * @throws IOException if an IO error occurs during writing
	 */
	public static void writeSearchResults(Collection<InvertedIndex.SearchResult> results, Writer writer, int indent)
			throws IOException {
		writer.write("[");
		var iterator = results.iterator();
		if (iterator.hasNext()) {
			writeSearchResult(iterator.next(), writer, indent + 1);
			while (iterator.hasNext()) {
				writer.write(",");
				writeSearchResult(iterator.next(), writer, indent + 1);
			}
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
		;
	}

	/**
	 * Writes a map of search queries to corresponding lists of
	 * {@link InvertedIndex.SearchResult} objects into a JSON file. The keys
	 * represent search queries and are sorted alphabetically, each associated with
	 * a list of results, which are also sorted primarily by score, then count, and
	 * finally lexicographically by location.
	 *
	 * @param searchResults the map containing search queries and their
	 *   corresponding search results
	 * @param path the file path to write the JSON formatted results
	 * @throws IOException if an IO error occurs during file writing
	 */
	public static void writeSearchResultsMap(Map<String, ? extends Collection<InvertedIndex.SearchResult>> searchResults,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writer.write("{\n");
			var iterator = searchResults.entrySet().iterator();
			while (iterator.hasNext()) {
				var entry = iterator.next();
				writeQuote(entry.getKey(), writer, 1);
				writer.write(": ");
				writeSearchResults(entry.getValue(), writer, 1);
				if (iterator.hasNext()) {
					writer.write(",");
				}
				writer.write("\n");
			}
			writer.write("}\n");
		}
	}

	/**
	 * Writes a list of {@link InvertedIndex.SearchResult} objects to a provided
	 * {@link BufferedWriter} in a JSON array format. The method formats each search
	 * result with proper indentation, adding commas between objects and handling
	 * the bracket closure correctly.
	 * 
	 * @param result the list of {@link InvertedIndex.SearchResult} objects to write
	 * @param writer the {@link BufferedWriter} to use for writing the data
	 * @param indent the initial indentation level to use for the array elements
	 * @throws IOException if an IO error occurs during writing
	 */
	public static void writeSearchResult(InvertedIndex.SearchResult result, Writer writer, int indent)
			throws IOException {
		writer.write("\n");
		writeIndent("{\n", writer, indent);
		writeIndent(String.format("\"count\": %d,", result.getCount()), writer, indent + 1);
		writer.write("\n");
		writeIndent(String.format("\"score\": %.8f", result.getScore()), writer, indent + 1);
		writer.write(",\n");
		writeIndent(String.format("\"where\": \"%s\"", result.getWhere()), writer, indent + 1);
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/** Prevent instantiating this class of static methods. */
	private JsonWriter() {
	}
}
