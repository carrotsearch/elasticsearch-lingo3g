
package com.carrotsearch.elasticsearch;

import static org.elasticsearch.test.ESIntegTestCase.Scope.SUITE;

import com.carrotsearch.lingo3g.integrations.elasticsearch.Lingo3GPlugin;
import java.util.Arrays;
import java.util.Collection;
import org.assertj.core.api.Assertions;
import org.carrot2.elasticsearch.ClusteringPlugin;
import org.carrot2.elasticsearch.ListAlgorithmsAction.ListAlgorithmsActionRequestBuilder;
import org.carrot2.elasticsearch.ListAlgorithmsAction.ListAlgorithmsActionResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;

@ClusterScope(scope = SUITE, transportClientRatio = 0)
public class ListAlgorithmsActionIT extends ESIntegTestCase {
  @Override
  protected Collection<Class<? extends Plugin>> nodePlugins() {
    return Arrays.asList(ClusteringPlugin.class, Lingo3GPlugin.class);
  }

  @Override
  protected Collection<Class<? extends Plugin>> transportClientPlugins() {
    return nodePlugins();
  }

  public void testAlgorithmsAreListed() throws Exception {
    Client client = client();

    ListAlgorithmsActionResponse response = new ListAlgorithmsActionRequestBuilder(client).get();
    Assertions.assertThat(response.getAlgorithms())
        .describedAs("A list of algorithms")
        .containsOnly("Lingo3G", "Lingo", "STC", "Bisecting K-Means");
  }
}
