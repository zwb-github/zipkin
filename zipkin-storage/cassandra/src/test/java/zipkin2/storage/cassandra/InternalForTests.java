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
package zipkin2.storage.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.TestInfo;
import zipkin2.DependencyLink;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static zipkin2.storage.cassandra.Schema.TABLE_SERVICE_REMOTE_SERVICES;

class InternalForTests {
  static CqlSession mockSession() {
    CqlSession session = mock(CqlSession.class);
    Metadata metadata = mock(Metadata.class);

    KeyspaceMetadata keyspaceMetadata = mock(KeyspaceMetadata.class);
    when(session.getMetadata()).thenReturn(metadata);
    when(metadata.getKeyspace("zipkin2")).thenReturn(Optional.of(keyspaceMetadata));

    when(keyspaceMetadata.getTable(TABLE_SERVICE_REMOTE_SERVICES))
      .thenReturn(Optional.of(mock(TableMetadata.class)));
    return session;
  }

  static void writeDependencyLinks(
    CassandraStorage storage, List<DependencyLink> links, long midnightUTC) {
    for (DependencyLink link : links) {
      SimpleStatement statement = QueryBuilder.insertInto(Schema.TABLE_DEPENDENCY)
        .value("day",
          literal(Instant.ofEpochMilli(midnightUTC).atZone(ZoneOffset.UTC).toLocalDate()))
        .value("parent", literal(link.parent()))
        .value("child", literal(link.child()))
        .value("calls", literal(link.callCount()))
        .value("errors", literal(link.errorCount())).build();
      storage.session().execute(statement);
    }
  }

  static String keyspace(TestInfo testInfo) {
    String result;
    if (testInfo.getTestMethod().isPresent()) {
      result = testInfo.getTestMethod().get().getName();
    } else {
      assert testInfo.getTestClass().isPresent();
      result = testInfo.getTestClass().get().getSimpleName();
    }
    result = result.toLowerCase();
    return result.length() <= 48 ? result : result.substring(result.length() - 48);
  }
}
