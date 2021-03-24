
package com.carrotsearch.elasticsearch;

import com.carrotsearch.lingo3g.Lingo3GClusteringAlgorithm;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.carrot2.attrs.Attrs;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.stc.STCClusteringAlgorithm;
import org.carrot2.elasticsearch.ClusteringAction;
import org.carrot2.elasticsearch.ClusteringActionRequestBuilder;
import org.carrot2.elasticsearch.ClusteringActionResponse;
import org.carrot2.elasticsearch.DocumentGroup;
import org.carrot2.elasticsearch.ListAlgorithmsAction.ListAlgorithmsActionRequestBuilder;
import org.carrot2.elasticsearch.ListAlgorithmsAction.ListAlgorithmsActionResponse;
import org.carrot2.elasticsearch.LogicalField;
import org.carrot2.language.LanguageComponentsLoader;
import org.carrot2.language.LoadedLanguages;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ObjectPath;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

/** API tests for {@link ClusteringAction}. */
public class ClusteringActionIT extends SampleIndexTestCase {
  public void testComplexQuery() throws IOException {
    ClusteringActionResponse result =
        new ClusteringActionRequestBuilder(client)
            .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
            .setQueryHint("data mining")
            .addSourceFieldMapping("title", LogicalField.TITLE)
            .addHighlightedFieldMapping("content", LogicalField.CONTENT)
            .setDefaultLanguage("English")
            .setSearchRequest(
                client
                    .prepareSearch()
                    .setIndices(INDEX_TEST)
                    .setSize(100)
                    .setQuery(QueryBuilders.termQuery("content", "data"))
                    .highlighter(new HighlightBuilder().preTags("").postTags(""))
                    .setFetchSource(new String[] {"title"}, null)
                    .highlighter(new HighlightBuilder().field("content")))
            .execute()
            .actionGet();

    checkValid(result);
    checkJsonSerialization(result);
  }

  @AwaitsFix(bugUrl = "https://github.com/carrotsearch/elasticsearch-lingo3g/issues/2")
  public void testDefaultLanguage() throws IOException {
    LinkedHashMap<String, List<String>> labelsByLanguage = new LinkedHashMap<>();
    LoadedLanguages languages =
        new LanguageComponentsLoader().limitToAlgorithms(new Lingo3GClusteringAlgorithm()).load();
    for (String lang : languages.languages()) {
      ClusteringActionResponse english =
          new ClusteringActionRequestBuilder(client)
              .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
              .setQueryHint("data mining")
              .addSourceFieldMapping("title", LogicalField.TITLE)
              .addHighlightedFieldMapping("content", LogicalField.CONTENT)
              .setDefaultLanguage(lang)
              .setSearchRequest(
                  client
                      .prepareSearch()
                      .setIndices(INDEX_TEST)
                      .setSize(100)
                      .setQuery(QueryBuilders.termQuery("content", "data"))
                      .setFetchSource(new String[] {"title"}, null))
              .execute()
              .actionGet();

      checkValid(english);
      checkJsonSerialization(english);

      labelsByLanguage.put(
          lang,
          Arrays.stream(english.getDocumentGroups())
              .map(DocumentGroup::getLabel)
              .collect(Collectors.toList()));
    }

    Assertions.assertThat(labelsByLanguage.keySet()).containsOnlyElementsOf(languages.languages());
  }

  public void testAttributes() throws IOException {
    Lingo3GClusteringAlgorithm algorithm = new Lingo3GClusteringAlgorithm();
    algorithm.clusters.allowOneDocumentClusters.set(true);
    algorithm.hierarchy.maxHierarchyDepth.set(1);

    Map<String, Object> extract = Attrs.extract(algorithm);
    Attrs.populate(algorithm, extract);

    ClusteringActionResponse result =
        new ClusteringActionRequestBuilder(client)
            .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
            .setQueryHint("data mining")
            .addSourceFieldMapping("title", LogicalField.TITLE)
            .addSourceFieldMapping("content", LogicalField.CONTENT)
            .addAttributes(Attrs.extract(algorithm))
            .setSearchRequest(
                client
                    .prepareSearch()
                    .setIndices(INDEX_TEST)
                    .setSize(100)
                    .setQuery(QueryBuilders.matchAllQuery())
                    .setFetchSource(new String[] {"title", "content"}, null))
            .execute()
            .actionGet();

    checkValid(result);
    checkJsonSerialization(result);
  }

  @AwaitsFix(bugUrl = "https://github.com/carrotsearch/elasticsearch-lingo3g/issues/3")
  public void testLanguageField() throws IOException {
    Map<String, Object> attrs = new HashMap<>();

    ClusteringActionResponse result =
        new ClusteringActionRequestBuilder(client)
            .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
            .setQueryHint("data mining")
            .addSourceFieldMapping("title", LogicalField.TITLE)
            .addSourceFieldMapping("content", LogicalField.CONTENT)
            .addSourceFieldMapping("rndlang", LogicalField.LANGUAGE)
            .addAttributes(attrs)
            .setSearchRequest(
                client
                    .prepareSearch()
                    .setIndices(INDEX_TEST)
                    .setSize(100)
                    .setQuery(QueryBuilders.termQuery("content", "data"))
                    .setFetchSource(new String[] {"title", "content", "rndlang"}, null))
            .get();

    checkValid(result);
    checkJsonSerialization(result);

    // We should receive groups for multiple languages
    String[] languages = result.getInfo().get(Fields.Info.LANGUAGES).split(",");

    Assertions.assertThat(languages)
        .describedAs(
            "Expected a lot of languages to appear in top groups: " + Arrays.toString(languages))
        .hasSizeGreaterThan(5);

    DocumentGroup[] groups = result.getDocumentGroups();
    List<String> groupLabels =
        Arrays.stream(groups)
            .map(grp -> grp.getLabel() + " (" + grp.getDocumentReferences().length + ")")
            .collect(Collectors.toList());
    Assertions.assertThat(groupLabels).hasSizeGreaterThan(5);
  }

  public void testListAlgorithms() {
    ListAlgorithmsActionResponse response = new ListAlgorithmsActionRequestBuilder(client).get();

    List<String> algorithms = response.getAlgorithms();
    Assertions.assertThat(algorithms)
        .isNotEmpty()
        .contains(
            Lingo3GClusteringAlgorithm.NAME,
            LingoClusteringAlgorithm.NAME,
            STCClusteringAlgorithm.NAME,
            BisectingKMeansClusteringAlgorithm.NAME);
  }

  public void testNonexistentFields() throws IOException {
    ClusteringActionResponse result =
        new ClusteringActionRequestBuilder(client)
            .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
            .setQueryHint("data mining")
            .addSourceFieldMapping("_nonexistent_", LogicalField.TITLE)
            .addSourceFieldMapping("_nonexistent_", LogicalField.CONTENT)
            .setCreateUngroupedDocumentsCluster(true)
            .setSearchRequest(
                client
                    .prepareSearch()
                    .setIndices(INDEX_TEST)
                    .setSize(100)
                    .setQuery(QueryBuilders.termQuery("content", "data"))
                    .setFetchSource(new String[] {"title", "content"}, null))
            .execute()
            .actionGet();

    // There should be no clusters, but no errors.
    checkValid(result);
    checkJsonSerialization(result);

    // Top level groups should be input documents' languages (aggregation strategy above).
    DocumentGroup[] documentGroups = result.getDocumentGroups();
    for (DocumentGroup group : documentGroups) {
      if (!group.isUngroupedDocuments()) {
        fail("Expected no clusters for non-existent fields.");
      }
    }
  }

  public void testNonexistentAlgorithmId() {
    // The query should result in an error.
    try {
      new ClusteringActionRequestBuilder(client)
          .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
          .setQueryHint("")
          .addSourceFieldMapping("_nonexistent_", LogicalField.TITLE)
          .setAlgorithm("_nonexistent_")
          .setSearchRequest(
              client
                  .prepareSearch()
                  .setIndices(INDEX_TEST)
                  .setSize(100)
                  .setQuery(QueryBuilders.termQuery("content", "data"))
                  .setFetchSource(new String[] {"title", "content"}, null))
          .execute()
          .actionGet();
      throw new RuntimeException("Unreachable code assertion hit.");
    } catch (IllegalArgumentException e) {
      Assertions.assertThat(e).hasMessageContaining("No such algorithm:");
    }
  }

  public void testPropagatingAlgorithmException() {
    // The query should result in an error.
    try {
      // Out of allowed range (should cause an exception).
      Map<String, Object> attrs = new HashMap<>();
      attrs.put("documents", Map.of("maxWordDf", 10));

      new ClusteringActionRequestBuilder(client)
          .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
          .setQueryHint("")
          .addSourceFieldMapping("title", LogicalField.TITLE)
          .addSourceFieldMapping("content", LogicalField.CONTENT)
          .setAlgorithm(STCClusteringAlgorithm.NAME)
          .addAttributes(attrs)
          .setSearchRequest(
              client
                  .prepareSearch()
                  .setIndices(INDEX_TEST)
                  .setSize(100)
                  .setQuery(QueryBuilders.termQuery("content", "data"))
                  .setFetchSource(new String[] {"title", "content"}, null))
          .execute()
          .actionGet();
      throw new RuntimeException("Unreachable code assertion hit.");
    } catch (ElasticsearchException e) {
      Assertions.assertThat(e).hasMessageContaining("Clustering error:");
    }
  }

  public void testIncludeHits() throws IOException {
    // same search with and without hits
    SearchRequestBuilder req =
        client
            .prepareSearch()
            .setIndices(INDEX_TEST)
            .setSize(2)
            .setQuery(QueryBuilders.termQuery("content", "data"))
            .setFetchSource(new String[] {"content"}, null);

    // with hits (default)
    ClusteringActionResponse resultWithHits =
        new ClusteringActionRequestBuilder(client)
            .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
            .setQueryHint("data mining")
            .setAlgorithm(STCClusteringAlgorithm.NAME)
            .addSourceFieldMapping("title", LogicalField.TITLE)
            .setCreateUngroupedDocumentsCluster(true)
            .setSearchRequest(req)
            .execute()
            .actionGet();
    checkValid(resultWithHits);
    checkJsonSerialization(resultWithHits);

    var asMap = asMap(resultWithHits);
    Assertions.assertThat(ObjectPath.<Object>eval("hits.total.value", asMap)).isEqualTo(96);
    Assertions.assertThat((List<?>) ObjectPath.eval("hits.hits", asMap)).isNotEmpty();

    // without hits
    ClusteringActionResponse resultWithoutHits =
        new ClusteringActionRequestBuilder(client)
            .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
            .setQueryHint("data mining")
            .setMaxHits(0)
            .setAlgorithm(STCClusteringAlgorithm.NAME)
            .addSourceFieldMapping("title", LogicalField.TITLE)
            .setCreateUngroupedDocumentsCluster(true)
            .setSearchRequest(req)
            .execute()
            .actionGet();
    checkValid(resultWithoutHits);
    checkJsonSerialization(resultWithoutHits);

    asMap = asMap(resultWithoutHits);
    Assertions.assertThat(ObjectPath.<Object>eval("hits.total.value", asMap)).isEqualTo(96);
    Assertions.assertThat((List<?>) ObjectPath.eval("hits.hits", asMap)).isEmpty();
  }

  public void testMaxHits() throws IOException {
    // same search with and without hits
    SearchRequestBuilder req =
        client
            .prepareSearch()
            .setIndices(INDEX_TEST)
            .setSize(2)
            .setQuery(QueryBuilders.termQuery("content", "data"))
            .setFetchSource(new String[] {"content"}, null);

    // Limit the set of hits to just top 2.
    ClusteringActionResponse limitedHits =
        new ClusteringActionRequestBuilder(client)
            .setAlgorithm(Lingo3GClusteringAlgorithm.NAME)
            .setQueryHint("data mining")
            .setMaxHits(2)
            .setAlgorithm(STCClusteringAlgorithm.NAME)
            .addSourceFieldMapping("title", LogicalField.TITLE)
            .setCreateUngroupedDocumentsCluster(true)
            .setSearchRequest(req)
            .execute()
            .actionGet();
    checkValid(limitedHits);
    checkJsonSerialization(limitedHits);

    Assertions.assertThat(limitedHits.getSearchResponse().getHits().getHits()).hasSize(2);

    var asMap = asMap(limitedHits);
    Assertions.assertThat(ObjectPath.<Object>eval("hits.total.value", asMap)).isEqualTo(96);
    Assertions.assertThat((List<?>) ObjectPath.eval("hits.hits", asMap)).hasSize(2);
  }

  private Map<String, Object> asMap(ClusteringActionResponse resultWithHits) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
    builder.startObject();
    resultWithHits.toXContent(builder, ToXContent.EMPTY_PARAMS);
    builder.endObject();

    Map<String, Object> responseJson;
    try (XContentParser parser =
        XContentHelper.createParser(
            NamedXContentRegistry.EMPTY,
            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            new BytesArray(Strings.toString(builder)),
            XContentType.JSON)) {
      responseJson = parser.mapOrdered();
    }
    return responseJson;
  }
}
