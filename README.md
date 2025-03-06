# Multithreaded Search Engine

A high-performance Java-based search engine utilizing multithreading for efficient text indexing and searching. This project implements a robust inverted index data structure with support for exact and partial search capabilities, web crawling, and thread-safe operations.

## Overview

This search engine is designed to efficiently index and search through large collections of text documents and web pages. It employs a multithreaded architecture to maximize performance on modern hardware, capable of handling concurrent indexing and search operations.

Key features include:
- **Multithreaded Indexing**: Efficiently processes documents using a thread pool to maximize CPU utilization
- **Thread-Safe Data Structures**: Custom implementations ensure data integrity during concurrent operations
- **Web Crawling**: Support for HTML document processing and web link traversal
- **Query Processing**: Fast exact and partial search capabilities with relevance ranking
- **JSON Output**: Search results and index data can be exported in a clean JSON format

## Architecture

The system is built around these core components:

### Inverted Index
- Maps words to their locations (file paths and positions within those files)
- Maintains word frequency counts 
- Supports search operations with relevance ranking

### Thread Safety Components
- `ThreadSafeInvertedIndex`: Thread-safe version of the inverted index using read-write locks
- `MultiReaderLock`: Custom implementation of a read-write lock allowing multiple concurrent readers
- `WorkQueue`: Thread pool implementation for managing worker threads

### Builders
- `InvertedIndexBuilder`: Single-threaded implementation for document processing
- `MultithreadedInvertedIndexBuilder`: Parallel implementation using worker threads

### Query Processing
- `QueryProcessor`: Processes search queries for single-threaded operations
- `ThreadSafeQueryProcessor`: Thread-safe implementation for concurrent query processing
- Support for both exact and partial matching search algorithms

### Web Components
- `WebCrawler`: Processes HTML pages and extracts links
- `HtmlFetcher`: Fetches web content with redirect handling
- `HtmlCleaner`: Strips HTML tags and cleans content for indexing
- `LinkFinder`: Identifies and normalizes links in HTML content

## Usage

The system is configured and run via command-line arguments:

```
java -cp ".:lib/*" edu.usfca.cs272.Driver [arguments]
```

### Command-Line Arguments

| Flag        | Description                                      | Default      |
|-------------|--------------------------------------------------|--------------|
| `-text`     | Path to the file or directory to index           | Required     |
| `-index`    | Path for the JSON file to output inverted index  | `index.json` |
| `-counts`   | Path for the JSON file to output word counts     | `counts.json`|
| `-results`  | Path for the JSON file to output search results  | `results.json`|
| `-query`    | Path to the file containing search queries       | None         |
| `-threads`  | Number of worker threads to use                  | 5            |
| `-html`     | Seed URL for web crawling                        | None         |
| `-partial`  | Use partial search instead of exact search       | False        |

### Examples

Index a directory of text files using 8 threads:
```
java -cp ".:lib/*" edu.usfca.cs272.Driver -text /path/to/texts -threads 8 -index index.json
```

Perform searches from a query file using previous index:
```
java -cp ".:lib/*" edu.usfca.cs272.Driver -query /path/to/queries.txt -results results.json
```

Crawl a website and build an index:
```
java -cp ".:lib/*" edu.usfca.cs272.Driver -html https://example.com -index web-index.json
```

## Performance Considerations

- The multithreaded implementation shows significant performance improvements over single-threaded operations, especially for large document collections
- The optimal number of threads depends on your hardware (typically matching the number of available CPU cores)
- Web crawling performance depends heavily on network conditions and the target website's responsiveness

## Dependencies

- Java 17 or higher
- Apache Commons Text
- Apache Log4j2
- OpenNLP Snowball Stemmer

## License

[MIT License](LICENSE)
