/*
 * Copyright 2015-2020 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin2.storage.cassandra.v1;

import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;
import static zipkin2.storage.cassandra.v1.Tables.SERVICE_SPAN_NAME_INDEX;

// QueryRequest.spanName
final class IndexTraceIdBySpanName extends IndexTraceId.Factory {
  IndexTraceIdBySpanName(CassandraStorage storage, int indexTtl) {
    super(storage, SERVICE_SPAN_NAME_INDEX, indexTtl);
  }

  @Override RegularInsert declarePartitionKey(RegularInsert insert) {
    return insert.value("service_span_name", bindMarker());
  }

  @Override void bindPartitionKey(BoundStatementBuilder bound, String partitionKey) {
    bound.setString(2, partitionKey);
  }
}
