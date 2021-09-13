package com.carrotsearch.lingo3g.integrations.elasticsearch;

import com.carrotsearch.licensing.LicenseLocation;
import com.carrotsearch.lingo3g.impl.OptionalLicenseLocationSupplier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;

/**
 * An ES-specific license location supplier resolving licenses from plugin configuration folders.
 */
public class LicenseLocationSupplier extends OptionalLicenseLocationSupplier {
  private static final AtomicReference<Path[]> globalLocations = new AtomicReference<>();
  private final AtomicReference<Path[]> locations = new AtomicReference<>();

  public LicenseLocationSupplier() {}

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

    LogManager.getLogger(LicenseLocationSupplier.class)
        .info(
            "Lingo3G license lookup locations at: {}",
            Stream.of(locations)
                .map(path -> path.toAbsolutePath().toString())
                .collect(Collectors.joining(", ")));
  }
}
