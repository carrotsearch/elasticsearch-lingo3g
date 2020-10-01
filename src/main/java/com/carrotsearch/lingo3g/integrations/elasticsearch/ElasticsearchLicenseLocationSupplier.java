/*
 * Copyright (C) 2006-2020, Carrot Search s.c.
 * All rights reserved.
 *
 * This source code is confidential and proprietary.
 * Do not redistribute.
 */
package com.carrotsearch.lingo3g.integrations.elasticsearch;

import com.carrotsearch.licensing.LicenseLocation;
import com.carrotsearch.lingo3g.impl.OptionalLicenseLocationSupplier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ElasticsearchLicenseLocationSupplier extends OptionalLicenseLocationSupplier {
  private static final AtomicReference<Path[]> globalLocations = new AtomicReference<>();
  private final AtomicReference<Path[]> locations = new AtomicReference<>();

  public ElasticsearchLicenseLocationSupplier() {}

  @Override
  public List<LicenseLocation> licenses() {
    Path[] global = globalLocations.get();
    if (locations.compareAndSet(null, global)) {
      if (locations.get() == null) {
        throw new RuntimeException("Expected scan location to be present but it was null.");
      } else {
        for (Path p : locations.get()) {
          addScanLocation(p);
        }
      }
    }

    return super.licenses();
  }

  public static void setGlobalLocations(Path[] locations) {
    Objects.requireNonNull(locations);
    if (!globalLocations.compareAndSet(null, locations)) {
      throw new RuntimeException(
          "Scan locations already set ("
              + Arrays.toString(globalLocations.get())
              + ") while attempting to change it to: "
              + Arrays.toString(locations));
    }
  }
}
