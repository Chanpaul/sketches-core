/*
 * Copyright 2015-16, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */
package com.yahoo.sketches.tuple;

import org.testng.annotations.Test;
import org.testng.Assert;

import com.yahoo.sketches.memory.NativeMemory;

public class ArrayOfDoublesQuickSelectSketchTest {
  @Test
  public void heapToDirectExactTwoDoubles() {
    ArrayOfDoublesUpdatableSketch sketch1 = new ArrayOfDoublesUpdatableSketchBuilder().setNumberOfValues(2).build();
    sketch1.update("a", new double[] {1, 2});
    sketch1.update("b", new double[] {1, 2});
    sketch1.update("c", new double[] {1, 2});
    sketch1.update("d", new double[] {1, 2});
    sketch1.update("a", new double[] {1, 2});
    ArrayOfDoublesUpdatableSketch sketch2 = (ArrayOfDoublesUpdatableSketch) ArrayOfDoublesSketches.wrapSketch(new NativeMemory(sketch1.toByteArray()));
    sketch2.update("b", new double[] {1, 2});
    sketch2.update("c", new double[] {1, 2});
    sketch2.update("d", new double[] {1, 2});
    Assert.assertFalse(sketch2.isEmpty());
    Assert.assertFalse(sketch2.isEstimationMode());
    Assert.assertEquals(sketch2.getEstimate(), 4.0);
    Assert.assertEquals(sketch2.getUpperBound(1), 4.0);
    Assert.assertEquals(sketch2.getLowerBound(1), 4.0);
    Assert.assertEquals(sketch2.getThetaLong(), Long.MAX_VALUE);
    Assert.assertEquals(sketch2.getTheta(), 1.0);
    double[][] values = sketch2.getValues();
    Assert.assertEquals(values.length, 4);
    for (double[] array: values) {
      Assert.assertEquals(array.length, 2);
      Assert.assertEquals(array[0], 2.0);
      Assert.assertEquals(array[1], 4.0);
    }
  }

  @Test
  public void directToHeapExactTwoDoubles() {
    ArrayOfDoublesUpdatableSketch sketch1 = new ArrayOfDoublesUpdatableSketchBuilder().setNumberOfValues(2).setMemory(new NativeMemory(new byte[1000000])).build();
    sketch1.update("a", new double[] {1, 2});
    sketch1.update("b", new double[] {1, 2});
    sketch1.update("c", new double[] {1, 2});
    sketch1.update("d", new double[] {1, 2});
    sketch1.update("a", new double[] {1, 2});
    ArrayOfDoublesUpdatableSketch sketch2 = (ArrayOfDoublesUpdatableSketch) ArrayOfDoublesSketches.heapifySketch(new NativeMemory(sketch1.toByteArray()));
    sketch2.update("b", new double[] {1, 2});
    sketch2.update("c", new double[] {1, 2});
    sketch2.update("d", new double[] {1, 2});
    Assert.assertFalse(sketch2.isEmpty());
    Assert.assertFalse(sketch2.isEstimationMode());
    Assert.assertEquals(sketch2.getEstimate(), 4.0);
    Assert.assertEquals(sketch2.getUpperBound(1), 4.0);
    Assert.assertEquals(sketch2.getLowerBound(1), 4.0);
    Assert.assertEquals(sketch2.getThetaLong(), Long.MAX_VALUE);
    Assert.assertEquals(sketch2.getTheta(), 1.0);
    double[][] values = sketch2.getValues();
    Assert.assertEquals(values.length, 4);
    for (double[] array: values) {
      Assert.assertEquals(array.length, 2);
      Assert.assertEquals(array[0], 2.0);
      Assert.assertEquals(array[1], 4.0);
    }
  }
}
