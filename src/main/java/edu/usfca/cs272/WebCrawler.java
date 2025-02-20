package edu.usfca.cs272;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Multithreaded web crawler that processes HTML pages and populates a
 * ThreadSafeInvertedIndex.
 */
/**
 * Multithreaded web crawler that processes HTML pages and populates a
 * ThreadSafeInvertedIndex.
 */
public class WebCrawler {
	/** The inverted index to store crawled information to. */
	private final InvertedIndex index;

	/** The visited links. */
	private final HashSet<URI> visited;

	private final WorkQueue queue;

	private int crawls;

	private static final Logger log = LogManager.getLogger();

	/**
	 * Initialize the web crawler.
	 *
	 * @param index the index to store crawled content
	 */
	public WebCrawler(ThreadSafeInvertedIndex index, WorkQueue queue, int crawls) {
		this.index = index;
		this.visited = new HashSet<>();
		this.queue = queue;
		this.crawls = crawls;
	}

	/**
	 * Scrape a page and add to the InvertedIndex.
	 * 
	 * @param html the html of the page to add
	 * @param uri the cleaned string to add as a location
	 * @param invIndex the InvertedIndex to add to
	 */
	private static void scrapePage(String html, String uri, InvertedIndex invIndex) {
		int position = 1;
		for (String word : FileStemmer.listStems(html)) {
			invIndex.addWord(word, uri, position);
			position++;
		}
	}

	/**
	 * Starts the web crawling from the provided seed URL.
	 *
	 * @param seed the seed URL to start crawling from
	 * @throws URISyntaxException if the seed URL is invalid
	 */
	public void crawl(String seed) throws URISyntaxException {
		URI uri = LinkFinder.toUri(seed);
		visited.add(uri);
		queue.execute(new CrawlTask(uri));
		queue.finish();
	}

	/**
	 * Returns an unmodifiable view of the visited URLs.
	 *
	 * @return unmodifiable set of visited URIs
	 */
	public Set<URI> getVisited() {
		return Collections.unmodifiableSet(visited);
	}

	private synchronized void decrementCrawls() {
		crawls--;
	}

	private class CrawlTask implements Runnable {
		private final URI uri;

		public CrawlTask(URI uri) {
			this.uri = uri;
		}

		@Override
		public void run() {
			String html = HtmlFetcher.fetch(uri, 3);
			if (html != null) {
				html = HtmlCleaner.stripBlockElements(html);

				for (URI link : LinkFinder.listUris(uri, html)) {
					if (crawls <= 1) {
						break;
					}
					synchronized (visited) {
						if (visited.contains(link)) {
							continue;
						}
						visited.add(link);
					}
					decrementCrawls();
					queue.execute(new CrawlTask(link));
				}

				InvertedIndex local = new InvertedIndex();
				String cleaned = HtmlCleaner.stripEntities(HtmlCleaner.stripTags(html));
				scrapePage(cleaned, LinkFinder.clean(uri).toString(), local);
				index.merge(local);

			}
		}
	}
}
