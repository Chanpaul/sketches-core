/*
 * Copyright 2016, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches;

import org.testng.annotations.Test;

public class SketchesExceptionTest {

  @Test(expectedExceptions = SketchesException.class)
  public void checkSketchesException() {
    throw new SketchesException("This is a test.");
  }
  
  @Test(expectedExceptions = SketchesArgumentException.class)
  public void checkSketchesArgumentException() {
    throw new SketchesArgumentException("This is a test.");
  }
  
  @Test(expectedExceptions = SketchesStateException.class)
  public void checkSketchesStateException() {
    throw new SketchesStateException("This is a test.");
  }
}
