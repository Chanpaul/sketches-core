/*
 * Copyright 2015-16, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.tuple;

import com.yahoo.sketches.BinomialBoundsN;

/**
 * This is an equivalent to com.yahoo.sketches.theta.Sketch with
 * addition of a user-defined Summary object associated with every unique entry
 * in the sketch.
 * @param <S> Type of Summary
 */
public abstract class Sketch<S extends Summary> {
  protected static final byte PREAMBLE_LONGS = 1;
  
  long[] keys_;
  S[] summaries_;
  long theta_;
  boolean isEmpty_ = true;

  Sketch() {}

  /**
   * Estimates the cardinality of the set (number of unique values presented to the sketch)
   * @return best estimate of the number of unique values
   */
  public double getEstimate() {
    if (!isEstimationMode()) return getRetainedEntries();
    return getRetainedEntries() / getTheta();
  }

  /**
   * Gets the approximate upper error bound given the specified number of Standard Deviations. 
   * This will return getEstimate() if isEmpty() is true.
   * 
   * @param numStdDev <a href="{@docRoot}/resources/dictionary.html#numStdDev">See Number of Standard Deviations</a>
   * @return the upper bound.
   */
  public double getUpperBound(final int numStdDev) {
    if (!isEstimationMode()) return getRetainedEntries();
    return BinomialBoundsN.getUpperBound(getRetainedEntries(), getTheta(), numStdDev, isEmpty_);
  }

  /**
   * Gets the approximate lower error bound given the specified number of Standard Deviations.
   * This will return getEstimate() if isEmpty() is true.
   * 
   * @param numStdDev <a href="{@docRoot}/resources/dictionary.html#numStdDev">See Number of Standard Deviations</a>
   * @return the lower bound.
   */
  public double getLowerBound(final int numStdDev) {
    if (!isEstimationMode()) return getRetainedEntries();
    return BinomialBoundsN.getLowerBound(getRetainedEntries(), getTheta(), numStdDev, isEmpty_);
  }

  /**
   * <a href="{@docRoot}/resources/dictionary.html#empty">See Empty</a>
   * @return true if empty.
   */
  public boolean isEmpty() {
    return isEmpty_;
  }

  /**
   * Returns true if the sketch is Estimation Mode (as opposed to Exact Mode).
   * This is true if theta &lt; 1.0 AND isEmpty() is false.
   * @return true if the sketch is in estimation mode.
   */
  public boolean isEstimationMode() {
    return ((theta_ < Long.MAX_VALUE) && !isEmpty());
  }

  /**
   * @return number of retained entries
   */
  public abstract int getRetainedEntries();

  /**
   * Gets the value of theta as a double between zero and one
   * @return the value of theta as a double
   */
  public double getTheta() {
    return theta_ / (double) Long.MAX_VALUE;
  }

  /**
   * @return an array of Summary objects from the sketch
   */
  public abstract S[] getSummaries();

  /**
   * This is to serialize an instance to a byte array.
   * For deserialization there must be a constructor, which takes a Memory object
   * @return serialized representation of the sketch
   */
  public abstract byte[] toByteArray();

  public SketchIterator<S> iterator() {
    return new SketchIterator<S>(keys_, summaries_);
  }

  long getThetaLong() {
    return theta_;
  }

}
