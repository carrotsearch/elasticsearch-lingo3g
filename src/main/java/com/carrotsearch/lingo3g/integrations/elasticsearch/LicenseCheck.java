
package com.carrotsearch.lingo3g.integrations.elasticsearch;

import com.carrotsearch.licensing.LicenseException;
import com.carrotsearch.lingo3g.Lingo3GClusteringAlgorithm;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.carrot2.language.LanguageComponents;
import org.carrot2.language.LanguageComponentsLoader;
import org.carrot2.language.LanguageComponentsProvider;

class LicenseCheck {
  /** Early license check. */
  void run() {
    try {
      Lingo3GClusteringAlgorithm algorithm = new Lingo3GClusteringAlgorithm();

      Map<String, List<LanguageComponentsProvider>> providers =
          AccessController.doPrivileged(
              (PrivilegedAction<Map<String, List<LanguageComponentsProvider>>>)
                  () ->
                      LanguageComponentsLoader.loadProvidersFromSpi(
                          getClass().getClassLoader(),
                          LanguageComponentsProvider.class.getClassLoader()));

      LanguageComponents english =
          LanguageComponents.loader()
              .limitToAlgorithms(algorithm)
              .limitToLanguages("English")
              .load(providers)
              .language("English");
      algorithm.cluster(Stream.empty(), english);
    } catch (IOException e) {
      throw new RuntimeException("Unexpected error testing for Lingo3G license.", e);
    } catch (LicenseException e) {
      throw new RuntimeException("Lingo3G license problem detected: " + e.getMessage());
    }
  }
}
