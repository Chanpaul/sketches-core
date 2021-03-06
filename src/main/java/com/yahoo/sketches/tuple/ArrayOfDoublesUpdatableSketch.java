/*
 * Copyright 2015-16, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.tuple;

import com.yahoo.sketches.hash.MurmurHash3;
import com.yahoo.sketches.memory.Memory;
/**
 * The top level for updatable tuple sketches of type ArrayOfDoubles.
 */
public abstract class ArrayOfDoublesUpdatableSketch extends ArrayOfDoublesSketch {

  final long seed_;

  ArrayOfDoublesUpdatableSketch(final int numValues, final long seed) {
    super(numValues);
    seed_ = seed;
  }

  /**
   * Updates this sketch with a long key and double values.
   * The values will be stored or added to the ones associated with the key 
   * 
   * @param key The given long key
   * @param values The given values
   */
  public void update(final long key, final double[] values) {
    update(new long[] {key}, values);
  }

  /**
   * Updates this sketch with a double key and double values.
   * The values will be stored or added to the ones associated with the key 
   * 
   * @param key The given double key
   * @param values The given values
   */
  public void update(final double key, final double[] values) {
    update(Util.doubleToLongArray(key), values);
  }

  /**
   * Updates this sketch with a String key and double values.
   * The values will be stored or added to the ones associated with the key 
   * 
   * @param key The given String key
   * @param values The given values
   */
  public void update(final String key, final double[] values) {
    update(Util.stringToByteArray(key), values);
  }

  /**
   * Updates this sketch with a byte[] key and double values.
   * The values will be stored or added to the ones associated with the key 
   * 
   * @param key The given byte[] key
   * @param values The given values
   */
  public void update(final byte[] key, final double[] values) {
    if (key == null || key.length == 0) return;
    insertOrIgnore(MurmurHash3.hash(key, seed_)[0] >>> 1, values);
  }

  /**
   * Updates this sketch with a int[] key and double values.
   * The values will be stored or added to the ones associated with the key 
   * 
   * @param key The given int[] key
   * @param values The given values
   */
  public void update(final int[] key, final double[] values) {
    if (key == null || key.length == 0) return;
    insertOrIgnore(MurmurHash3.hash(key, seed_)[0] >>> 1, values);
  }

  /**
   * Updates this sketch with a long[] key and double values.
   * The values will be stored or added to the ones associated with the key 
   * 
   * @param key The given long[] key
   * @param values The given values
   */
  public void update(final long[] key, final double[] values) {
    if (key == null || key.length == 0) return;
    insertOrIgnore(MurmurHash3.hash(key, seed_)[0] >>> 1, values);
  }

  /**
   * Gets the configured nominal number of entries
   * @return nominal number of entries
   */
  public abstract int getNominalEntries();

  /**
   * Rebuilds reducing the actual number of entries to the nominal number of entries if needed
   */
  public abstract void trim();

  /**
   * Gets an on-heap compact representation of the sketch
   * @return compact sketch
   */
  public ArrayOfDoublesCompactSketch compact() {
    return compact(null);
  }

  /**
   * Gets an off-heap compact representation of the sketch using the given memory
   * @param dstMem memory for the compact sketch (can be null)
   * @return compact sketch (off-heap if memory is provided)
   */
  public ArrayOfDoublesCompactSketch compact(final Memory dstMem) {
    if (dstMem == null) return new HeapArrayOfDoublesCompactSketch(this);
    return new DirectArrayOfDoublesCompactSketch(this, dstMem);
  }

  long getSeed() {
    return seed_;
  }

  @Override
  short getSeedHash() {
    return Util.computeSeedHash(seed_);
  }

  abstract void insertOrIgnore(long key, double[] values);

}
