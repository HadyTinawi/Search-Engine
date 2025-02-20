package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Builds an inverted index from text files. This class contains methods to
 * process individual files as well as to traverse directories to process
 * multiple files.
 */
public class InvertedIndexBuilder {

	/**
	 * Builds an inverted index from a given path. If the path is a directory, it
	 * will traverses the directory, otherwise it processes the file.
	 *
	 * @param textPath the path to the file or directory
	 * @param invertedIndex the inverted index to build
	 * @throws IOException if an I/O error occurs (reading from the file or
	 *   directory)
	 */

	public static void build(Path textPath, InvertedIndex invertedIndex) throws IOException {
		if (Files.isDirectory(textPath)) {
			traverseDirectory(textPath, invertedIndex);
		}
		else {
			processFile(textPath, invertedIndex);
		}
	}

	/**
	 * Processes a single file and adds its content to the provided inverted index.
	 *
	 * @param file the file to process
	 * @param invertedIndex the inverted index to add content to
	 * @throws IOException if an I/O error occurs reading from the file
	 */
	public static void processFile(Path file, InvertedIndex invertedIndex) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			String line;
			int position = 0;
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			String pathOfFile = file.toString();

			while ((line = reader.readLine()) != null) {
				String[] words = FileStemmer.parse(line);

				for (String word : words) {
					if (!word.isEmpty()) {
						String stem = stemmer.stem(word).toString();
						invertedIndex.addWord(stem, pathOfFile, ++position);
					}
				}
			}
		}
	}

	/**
	 * Traverses a directory and processes all text files found within it using a
	 * directory stream
	 *
	 * @param directory the directory to traverse
	 * @param index the inverted index to add content to
	 * @throws IOException if an I/O error occurs reading from the directory
	 */

	public static void traverseDirectory(Path directory, InvertedIndex index) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					traverseDirectory(entry, index);
				}
				else if (isTextFile(entry)) {
					processFile(entry, index);
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

}
