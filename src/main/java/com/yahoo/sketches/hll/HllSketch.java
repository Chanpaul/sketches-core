/*
 * Copyright 2015-16, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.hll;

import static com.yahoo.sketches.hash.MurmurHash3.hash;
import static java.nio.charset.StandardCharsets.UTF_8;
import static com.yahoo.sketches.Util.*;

/**
 * Top-level class for the HLL family of sketches. This class should not be constructed directly.
 * Use the HllSketchBuilder instead.
 * 
 * @author Kevin Lang
 */
public class HllSketch {
  private static final double HLL_REL_ERROR_NUMER = 1.04;

  public static HllSketchBuilder builder() {
    return new HllSketchBuilder();
  }

  private Fields.UpdateCallback updateCallback;
  private final Preamble preamble;

  private Fields fields;

  public HllSketch(Fields fields) {
    this.fields = fields;
    this.updateCallback = new Fields.UpdateCallback() {
      @Override
      public void bucketUpdated(int bucket, byte oldVal, byte newVal) {
        //intentionally empty
      }
    };
    this.preamble = fields.getPreamble();
  }

  /**
   * Present this sketch with a long.
   * 
   * @param datum The given long datum.
   */
  public void update(long datum) {
    long[] data = { datum };
    updateWithHash(hash(data, DEFAULT_UPDATE_SEED));
  }
  
  /**
   * Present this sketch with the given double (or float) datum. 
   * The double will be converted to a long using Double.doubleToLongBits(datum), 
   * which normalizes all NaN values to a single NaN representation. 
   * Plus and minus zero will be normalized to plus zero. 
   * The special floating-point values NaN and +/- Infinity are treated as distinct.
   * 
   * @param datum The given double datum.
   */
  public void update(double datum) {
    double d = (datum == 0.0) ? 0.0 : datum; // canonicalize -0.0, 0.0
    long[] data = { Double.doubleToLongBits(d) };// canonicalize all NaN forms
    updateWithHash(hash(data, DEFAULT_UPDATE_SEED));
  }
  
  /**
   * Present this sketch with the given String. 
   * The string is converted to a byte array using UTF8 encoding. 
   * If the string is null or empty no update attempt is made and the method returns.
   * 
   * @param datum The given String.
   */
  public void update(String datum) {
    if (datum == null || datum.isEmpty()) {
      return; 
    }
    byte[] data = datum.getBytes(UTF_8);
    updateWithHash(hash(data, DEFAULT_UPDATE_SEED));
  }
  
  /**
   * Present this sketch with the given byte array. 
   * If the byte array is null or empty no update attempt is made and the method returns.
   * 
   * @param data The given byte array.
   */
  public void update(byte[] data) {
    if ((data == null) || (data.length == 0)) {
      return;
    }
    updateWithHash(hash(data, DEFAULT_UPDATE_SEED));
  }
  
  /**
   * Present this sketch with the given integer array. 
   * If the integer array is null or empty no update attempt is made and the method returns.
   * 
   * @param data The given int array.
   */
  public void update(int[] data) {
    if ((data == null) || (data.length == 0)) {
      return;
    }
    updateWithHash(hash(data, DEFAULT_UPDATE_SEED));
  }
  
  /**
   * Present this sketch with the given long array. 
   * If the long array is null or empty no update attempt is made and the method returns.
   * 
   * @param data The given long array.
   */
  public void update(long[] data) {
    if ((data == null) || (data.length == 0)) {
      return;
    }
    updateWithHash(hash(data, DEFAULT_UPDATE_SEED));
  }
  
  /**
   * Gets the unique count estimate.
   * @return the sketch's best estimate of the cardinality of the input stream.
   */
  public double getEstimate() {
    double rawEst = getRawEstimate();
    int logK = preamble.getLogConfigK();

    double[] x_arr = Interpolation.interpolation_x_arrs[logK - Interpolation.INTERPOLATION_MIN_LOG_K];
    double[] y_arr = Interpolation.interpolation_y_arrs[logK - Interpolation.INTERPOLATION_MIN_LOG_K];

    if (rawEst < x_arr[0]) {
      return 0;
    }
    if (rawEst > x_arr[x_arr.length - 1]) {
      return rawEst;
    }

    double adjEst = Interpolation.cubicInterpolateUsingTable(x_arr, y_arr, rawEst);
    int configK = preamble.getConfigK();

    if (adjEst > 3.0 * configK) {
      return adjEst;
    }

    double linEst = getLinearEstimate();
    double avgEst = (adjEst + linEst) / 2.0;

    // The following constant 0.64 comes from empirical measurements (see below) of the crossover
    //   point between the average error of the linear estimator and the adjusted hll estimator
    if (avgEst > 0.64 * configK) {
      return adjEst;
    }
    return linEst;
  }

  public double getUpperBound(double numStdDevs) {
    return getEstimate() / (1.0 - eps(numStdDevs));
  }

  public double getLowerBound(double numStdDevs) {
    double lowerBound = getEstimate() / (1.0 + eps(numStdDevs));
    double numNonZeros = preamble.getConfigK();
    numNonZeros -= numBucketsAtZero();
    if (lowerBound < numNonZeros) {
      return numNonZeros;
    }
    return lowerBound;
  }

  private double getRawEstimate() {
    int numBuckets = preamble.getConfigK();
    double correctionFactor = 0.7213 / (1.0 + 1.079 / numBuckets);
    correctionFactor *= numBuckets * numBuckets;
    correctionFactor /= inversePowerOf2Sum();
    return correctionFactor;
  }

  private double getLinearEstimate() {
    int configK = preamble.getConfigK();
    long longV = numBucketsAtZero();
    if (longV == 0) {
      return configK * Math.log(configK / 0.5);
    }
    return (configK * (HarmonicNumbers.harmonicNumber(configK) - HarmonicNumbers.harmonicNumber(longV)));
  }

  public HllSketch union(HllSketch that) {
    fields = that.fields.unionInto(fields, updateCallback);
    return this;
  }

  private void updateWithHash(long[] hash) {
    byte newValue = (byte) (Long.numberOfLeadingZeros(hash[1]) + 1);
    int slotno = (int) hash[0] & (preamble.getConfigK() - 1);
    fields = fields.updateBucket(slotno, newValue, updateCallback);
  }

  private double eps(double numStdDevs) {
    return numStdDevs * HLL_REL_ERROR_NUMER / Math.sqrt(preamble.getConfigK());
  }

  public byte[] toByteArray() {
    int numBytes = (preamble.getPreambleSize() << 3) + fields.numBytesToSerialize();
    byte[] retVal = new byte[numBytes];

    fields.intoByteArray(retVal, preamble.intoByteArray(retVal, 0));

    return retVal;
  }

  public byte[] toByteArrayNoPreamble() {
    byte[] retVal = new byte[fields.numBytesToSerialize()];
    fields.intoByteArray(retVal, 0);
    return retVal;
  }

  public HllSketch asCompact() {
    return new HllSketch(fields.toCompact());
  }

  public int numBuckets() {
    return preamble.getConfigK();
  }

  public Preamble getPreamble() {
    return preamble;
  }

  /**
   * Set the update callback.  This is protected because it is intended that only children might *call*
   * the method.  It is not expected that this would be overridden by a child class.  If someone overrides
   * it and weird things happen, the bug lies in the fact that it was overridden.
   *
   * @param updateCallback the update callback for the HllSketch to use when talking with its Fields
   */
  protected void setUpdateCallback(Fields.UpdateCallback updateCallback) {
    this.updateCallback = updateCallback;
  }

  //Helper methods that are potential extension points for children

  /**
   * The sum of the inverse powers of 2
   *
   * @return the sum of the inverse powers of 2
   */
  protected double inversePowerOf2Sum() {
    return HllUtils.computeInvPow2Sum(numBuckets(), fields.getBucketIterator());
  }

  protected int numBucketsAtZero() {
    int retVal = 0;
    int count = 0;

    BucketIterator bucketIter = fields.getBucketIterator();
    while (bucketIter.next()) {
      if (bucketIter.getValue() == 0) {
        ++retVal;
      }
      ++count;
    }

    // All skipped buckets are 0.
    retVal += fields.getPreamble().getConfigK() - count;

    return retVal;
  }
}
