
package com.carrotsearch.lingo3g.integrations.elasticsearch;

import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.slf4j.LoggerFactory;

/**
 * Elasticsearch extension plugin adding Lingo3G clustering algorithm support to <a
 * href="https://github.com/carrot2/elasticsearch-carrot2">elasticsearch-carrot2</a>.
 */
public class Lingo3GPlugin extends Plugin {
  public static final String PLUGIN_NAME = "elasticsearch-lingo3g";

  @Override
  public Collection<Object> createComponents(
      Client client,
      ClusterService clusterService,
      ThreadPool threadPool,
      ResourceWatcherService resourceWatcherService,
      ScriptService scriptService,
      NamedXContentRegistry xContentRegistry,
      Environment environment,
      NodeEnvironment nodeEnvironment,
      NamedWriteableRegistry namedWriteableRegistry,
      IndexNameExpressionResolver indexNameExpressionResolver,
      Supplier<RepositoriesService> repositoriesServiceSupplier) {
    // A hack to initialize slf4j bindings properly.
    hackInitSlf4j();

    try {
      Path configPath = environment.configFile();
      Path pluginPath = environment.configFile().resolve(PLUGIN_NAME);
      LicenseLocationSupplier.setGlobalLocations(
          new Path[] {configPath.toAbsolutePath(), pluginPath.toAbsolutePath()});

      new LicenseCheck().run();
    } catch (NoClassDefFoundError e) {
      throw new RuntimeException(
          "Required Lingo3G classes not found (did you copy Lingo3G JARs to the plugin?): "
              + e.getMessage());
    }

    return Collections.emptyList();
  }

  private void hackInitSlf4j() {
    AccessController.doPrivileged(
        (PrivilegedAction<Void>)
            () -> {
              ClassLoader cl = Thread.currentThread().getContextClassLoader();
              try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                LoggerFactory.getLogger(this.getClass());
              } finally {
                Thread.currentThread().setContextClassLoader(cl);
              }
              return null;
            });
  }
}
