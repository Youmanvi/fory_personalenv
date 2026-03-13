/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fory.format.row.binary;

import java.util.Arrays;
import org.apache.fory.format.row.binary.writer.BinaryRowWriter;
import org.apache.fory.format.type.DataTypes;
import org.apache.fory.format.type.Schema;
import org.testng.Assert;
import org.testng.annotations.Test;

// Golden-byte tests for cross-language row format compatibility.
// Each test encodes a row using the Java BinaryRowWriter and asserts the exact hex output,
// which is used as the reference fixture in Go (and future Swift/Dart/JS) implementations.
public class BinaryRowCrossLangTest {

  // Generates the canonical golden hex for {id=42, name="Alice"} (basic cross-language test).
  @Test
  public void testGoldenFile() {
    Schema schema =
        new Schema(
            Arrays.asList(
                DataTypes.field("id", DataTypes.int64(), false),
                DataTypes.field("name", DataTypes.utf8())));

    BinaryRowWriter w = new BinaryRowWriter(schema);
    w.reset();
    w.write(0, 42L);
    w.write(1, "Alice");
    BinaryRow row = w.getRow();

    byte[] bytes = row.toBytes();
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    String hex = sb.toString();
    System.out.println("Golden hex: " + hex);

    Assert.assertEquals(
        hex,
        "00000000000000002a000000000000000500000018000000416c696365000000",
        "Java golden bytes changed — update javaGoldenHex in go/fory/row/row_test.go");
  }

  // Generates the canonical golden hex for {id=1, value=null} (null-field cross-language test).
  @Test
  public void testGoldenNull() {
    Schema schema =
        new Schema(
            Arrays.asList(
                DataTypes.field("id", DataTypes.int64(), false),
                DataTypes.field("value", DataTypes.int64(), true)));

    BinaryRowWriter w = new BinaryRowWriter(schema);
    w.reset();
    w.write(0, 1L);
    w.setNullAt(1);
    BinaryRow row = w.getRow();

    byte[] bytes = row.toBytes();
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    String hex = sb.toString();
    System.out.println("Golden null hex: " + hex);

    Assert.assertEquals(
        hex,
        "020000000000000001000000000000000000000000000000",
        "Java null golden bytes changed — update javaGoldenNullHex in go/fory/row/row_test.go");
  }

}
