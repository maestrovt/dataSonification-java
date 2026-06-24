package com.dataSonification.v2.util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Synchronized queue for producer-consumer synchronization.
 * @author Kimo Johnson
 */
public class SyncBuffer<T> {
	
	/**
	 * The fixed size of the queue.
	 */
	private final int size;

	/**
	 * Indicates which triage scheme to use.
	 */
	private final int triageScheme;
	
	/**
	 * The contents of the queue.
	 */
	private LinkedList<T> contents;

	/**
	 * Initializes the SyncBuffer with default capacity 10 and triage scheme 1.
	 */
	public SyncBuffer() {
		this(10, 1);
	}

	/**
	 * Initializes the SyncBuffer with the specified size and triage scheme 1.
	 * @param size
	 */
	public SyncBuffer(int size) {
		this(size, 1);
	}

	/**
	 * Initializes the SyncBuffer with the specified size and triage scheme.
	 * @param size size of the queue
	 * @param triageScheme the triage scheme to use (currently only 1)
	 */
	public SyncBuffer(int size, int triageScheme) {
		this.size = size;
		this.triageScheme = triageScheme;
		contents = new java.util.LinkedList<T>();
	}

	/**
	 * Gets an object from the queue.  Threads will block if no objects are available.
	 * @return the first object in the queue
	 * @throws InterruptedException if a blocking thread is interrupted
	 */
	public synchronized T get() throws InterruptedException {
		while (contents.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				contents.clear();
				throw e;
			}
		}

		return contents.remove();
	}

	/**
	 * Adds an object to the end of the queue.
	 * @param object the object to add to the queue
	 */
	public synchronized void put(T object) {
		if (contents.size() >= size) {
			// Run triage scheme
			switch (triageScheme) {
			default:
				triage1();
			}
		}

		contents.add(object);
		notify();
	}
	
	/**
	 * Remove all objects from the queue.
	 */
	public synchronized void clear() {
		contents.clear();
	}
	
	/**
	 * Triage scheme 1 clears all objects from the queue.
	 */
	private void triage1() {
		Log.println(Subsystem.CORE, ReturnCode.NO_CODE, "SyncBuffer: traige scheme 1", Log.P_INFO);
		contents.clear();
	}
	
	/**
	 * Remove a specific object from the queue.
	 * @param object the object to remove from the queue
	 */
	public synchronized void remove(T object) {
		for (Iterator<T> it = contents.iterator(); it.hasNext(); ) {
			T current = it.next();
			if (current.equals(object)) {
				it.remove();
			}
		}
	}
    
    /**
	 * Update a specific object in the queue.
	 * @param object the object to remove from the queue
	 */
	public synchronized void update(T object) {
		int position = contents.indexOf(object);
        if(position != -1)
        {
            contents.set(position, object);
        }
        else
        {
            put(object);
        }
	}
}