
package com.carrotsearch.elasticsearch;

public final class Fields {
  static final String SEARCH_RESPONSE = "search_response";
  static final String CLUSTERS = "clusters";
  static final String INFO = "info";

  // from SearchResponse
  static final String _SCROLL_ID = "_scroll_id";
  static final String _SHARDS = "_shards";
  static final String TOTAL = "total";
  static final String SUCCESSFUL = "successful";
  static final String FAILED = "failed";
  static final String FAILURES = "failures";
  static final String STATUS = "status";
  static final String INDEX = "index";
  static final String SHARD = "shard";
  static final String REASON = "reason";
  static final String TOOK = "took";
  static final String TIMED_OUT = "timed_out";

  static final class Info {
    public static final String ALGORITHM = "algorithm";
    public static final String SEARCH_MILLIS = "search-millis";
    public static final String CLUSTERING_MILLIS = "clustering-millis";
    public static final String TOTAL_MILLIS = "total-millis";
    public static final String INCLUDE_HITS = "include-hits";
    public static final String MAX_HITS = "max-hits";
    public static final String LANGUAGES = "languages";
  }
}
