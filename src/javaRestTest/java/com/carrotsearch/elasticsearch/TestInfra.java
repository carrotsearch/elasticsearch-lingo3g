
package com.carrotsearch.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.carrot2.clustering.Document;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;

/** Facade loading sample document data. */
final class TestInfra {

  public static final class TestDocument implements Document {
    private final ArrayList<Map.Entry<String, String>> fieldValues = new ArrayList<>();

    TestDocument(Map<String, String> fields) {
      this(fields.entrySet());
    }

    TestDocument(Collection<Map.Entry<String, String>> fieldValues) {
      this.fieldValues.addAll(fieldValues);
    }

    public TestDocument cloneWith(Map<String, String> fields) {
      ArrayList<Map.Entry<String, String>> cloned = new ArrayList<>(fieldValues);
      cloned.addAll(fields.entrySet());
      return new TestDocument(cloned);
    }

    @Override
    public void visitFields(BiConsumer<String, String> fieldConsumer) {
      fieldValues.forEach(e -> fieldConsumer.accept(e.getKey(), e.getValue()));
    }

    public XContentBuilder toXContent() {
      try {
        var xc = XContentFactory.jsonBuilder().prettyPrint().startObject();
        for (var e : fieldValues) {
          xc.field(e.getKey(), e.getValue());
        }
        return xc.endObject();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    public List<Map.Entry<String, String>> fields() {
      return Collections.unmodifiableList(fieldValues);
    }

    public String toJson() {
      return Strings.toString(toXContent());
    }
  }

  public static List<TestDocument> load(String resource) throws IOException {
    var json = jsonResource(TestInfra.class, resource);

    try (XContentParser parser =
        JsonXContent.jsonXContent.createParser(
            NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, json)) {
      return parser.list().stream()
          .map(
              entry -> {
                @SuppressWarnings("unchecked")
                var fields = (Map<String, String>) entry;
                return new TestDocument(fields);
              })
          .collect(Collectors.toList());
    }
  }

  public static String jsonResource(Class<?> clazz, String resourceName) throws IOException {
    return new String(resource(clazz, resourceName), StandardCharsets.UTF_8);
  }

  public static byte[] resource(Class<?> clazz, String resourceName) throws IOException {
    var resName = "_" + clazz.getSimpleName() + "/" + resourceName;
    try (InputStream is = clazz.getResourceAsStream(resName)) {
      if (is == null) {
        throw new IOException(
            "Resource " + resName + " does not exist relative to class: " + clazz.getName());
      }
      return is.readAllBytes();
    }
  }
}
