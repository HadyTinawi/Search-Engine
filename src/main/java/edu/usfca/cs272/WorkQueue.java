package edu.usfca.cs272;

import java.util.LinkedList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple work queue implementation based on the IBM developerWorks article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href=
 *   "https://web.archive.org/web/20210126172022/https://www.ibm.com/developerworks/library/j-jtp0730/index.html">
 *   Java Theory and Practice: Thread Pools and Work Queues</a>
 */
public class WorkQueue {
	/** Workers that wait until work (or tasks) are available. */
	private final Worker[] workers;

	/** Queue of pending work (or tasks). */
	private final LinkedList<Runnable> tasks;

	/** Logger used for this class. */
	private static final Logger log = LogManager.getLogger();

	/** Used to signal the workers should terminate. */
	private volatile boolean shutdown;

	/** The default number of worker threads to use when not specified. */
	public static final int DEFAULT = 5;

	/** Tracks the number of pending tasks. */
	private int pending = 0;

	/**
	 * Initializes the work queue with the default number of worker threads.
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	// CITE: Help in CSLABS
	/**
	 * Increments the pending
	 */
	public synchronized void IncrementPending() {
		pending++;
	}

	/**
	 * Decrements the pending while notifying if there are 0 pending
	 */
	public synchronized void DecrementPending() {
		pending--;
		if (pending <= 0) {
			this.notifyAll();
		}
	}

	/**
	 * Initializes the work queue with the specified number of worker threads.
	 * 
	 * @param threads the number of worker threads; must be greater than 0
	 */
	public WorkQueue(int threads) {

		if (threads < 1) {
			throw new IllegalArgumentException("Thread count must be at least 1");
		}

		this.tasks = new LinkedList<>();
		this.workers = new Worker[threads];
		this.shutdown = false;

		for (int i = 0; i < threads; i++) {
			workers[i] = new Worker();
			workers[i].start();
		}
	}

	/**
	 * Adds a work task to the queue. A worker thread will process this task when
	 * available.
	 * 
	 * @param task the work task to be executed by a worker thread
	 */
	public void execute(Runnable task) {
		IncrementPending();
		synchronized (tasks) {
			tasks.addLast(task);
			tasks.notifyAll();
		}
	}

	/**
	 * Waits for all pending work tasks to be completed. Does not terminate worker
	 * threads, allowing the work queue to continue being used.
	 */
	public synchronized void finish() {
		try {
			while (pending > 0) {
				wait();
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are
	 * executed, but no new tasks will be accepted. Waits for all tasks to complete
	 * and then terminates the worker threads.
	 */
	// //CITE: Help in CSLABS
	public void shutdown() {
		shutdown = true;

		synchronized (tasks) {
			tasks.notifyAll();
		}
	}

	/**
	 * Awaits the completion of all tasks and worker threads before returning. The
	 * work queue cannot be reused after this call.
	 */
	public void join() {
		try {
			finish();
			shutdown();
			for (Worker worker : workers) {
				worker.join();
			}
		}
		catch (InterruptedException e) {
			System.err.println("Warning: Work queue interrupted while joining.");
			log.catching(Level.WARN, e);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return the number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * Worker threads that process submitted tasks.
	 */
	private class Worker extends Thread {
		/**
		 * Initializes a worker thread with a custom name.
		 */
		public Worker() {
			setName("Worker" + getName());
		}

		/**
		 * Runs in an infinite loop, processing tasks from the queue unless interrupted
		 * or the queue is shut down.
		 */
		@Override
		public void run() {
			Runnable task;
			try {
				while (true) {
					synchronized (tasks) {
						while (tasks.isEmpty() && !shutdown) {
							tasks.wait();
						}

						if (shutdown) {
							break;
						}

						task = tasks.removeFirst();
					}

					try {
						task.run();
					}
					catch (RuntimeException e) {
						// catch runtime exceptions to avoid leaking threads
						System.err.printf("Error: %s encountered an excpetion while running.%n", this.getName());
						log.catching(Level.ERROR, e);
					}
					finally {
						DecrementPending();
					}
				}
			}
			catch (InterruptedException e) {
				// causes early termination of worker threads
				System.err.printf("Warning %s interrupted while waiting.%n", this.getName());
				log.catching(Level.WARN, e);
				Thread.currentThread().interrupt();
			}
		}
	}
}
