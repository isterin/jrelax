/*
Copyright (c) 2007, Ilya Sterin
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.buycentives.jrelax;

import com.buycentives.jrelax.*;
import org.codehaus.jackson.map.ObjectMapper;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Ilya Sterin
 * @version 1.0
 */
public class DefaultResourceManagerTests {

  private ResourceManager resourceMgr;
  private ObjectMapper jsonMapper = new ObjectMapper();

  @BeforeClass
  public void setUp() {
    resourceMgr = new DefaultResourceManager("http://localhost:5984");
  }

  @Test
  public void createDatabase() {
    resourceMgr.createDatabase("ilya_test");
    assertTrue(resourceMgr.databaseExists("ilya_test"));
  }

  @Test(dependsOnMethods = "createDatabase")
  public void listDatabases() {
    List<String> dbs = resourceMgr.listDatabases();
    assertNotNull(dbs);
    assertTrue(dbs.size() > 0);
    assertTrue(dbs.contains("ilya_test"));
  }

  @Test(expectedExceptions = RuntimeException.class, dependsOnMethods = "createDatabase")
  public void createDuplicateDatabase() {
    resourceMgr.createDatabase("ilya_test");
  }

  @Test(dependsOnMethods = "createDatabase")
  public void createAndDeleteDocuments() throws IOException {
    Document doc =
        resourceMgr.saveDocument(new Document(
            "ilya_test", "ilyas_doc", "{\"test\": 1, \"test2\": { \"val\": 1 }}"
        ));
    assertNotNull(doc);
    assertEquals(doc.getDatabaseName(), "ilya_test");
    assertEquals(doc.getId(), "ilyas_doc");
    resourceMgr.deleteDocument(doc);

    doc = resourceMgr.saveDocument("ilya_test", "ilyas_doc2", "{\"test\": 1, \"test2\": { \"val\": 1 }}");
    assertNotNull(doc);
    assertEquals(doc.getDatabaseName(), "ilya_test");
    assertEquals(doc.getId(), "ilyas_doc2");
    resourceMgr.deleteDocument("ilya_test", "ilyas_doc2", doc.getRevision());
  }

  @Test
  public void retrieveDocuments() {
    Document doc =
        resourceMgr.saveDocument("ilya_test", "ilyas_doc3", "{\"test\": 1, \"test2\": { \"val\": 1 }}");
    Document doc2 = resourceMgr.getDocument("ilya_test", "ilyas_doc3");
    assertNotNull(doc2);
    assertEquals(doc2.getId(), "ilyas_doc3");

    Document doc3 = resourceMgr.getDocument("ilya_test", "ilyas_doc3", doc.getRevision());
    assertNotNull(doc3);
    assertEquals(doc3.getId(), "ilyas_doc3");
    assertEquals(doc3.getRevision(), doc.getRevision());
  }

  @Test(dependsOnMethods = "createDatabase")
  public void executeTemporaryView() {
    Document doc1 = createDoc("ilya_test", "ilyas_doc", "{\"value\": 1, \"test2\": { \"val\": 1 }}");
    Document doc2 = createDoc("ilya_test", "ilyas_doc2", "{\"value\": 2, \"test2\": { \"val\": 1 }}");

    ViewResult<Object, Integer> result =
        resourceMgr.executeTemporaryView("ilya_test", "function(doc) { emit(null, doc.value); }",
            null, Object.class, Integer.class);
    assertEquals(result.getOffset(), 0);
    assertEquals(result.getTotalRows(), 2);
    for (ViewResult.ViewResultRow<Object, Integer> resultRow : result.getResultRows()) {
      if (resultRow.getId().equals(doc1.getId())) {
        assertNull(resultRow.getKey());
        assertEquals(resultRow.getValue().intValue(), 1);
      }
      else if (resultRow.getId().equals(doc2.getId())) {
        assertNull(resultRow.getKey());
        assertEquals(resultRow.getValue().intValue(), 2);
      }
    }

    resourceMgr.deleteDocument(doc1);
    resourceMgr.deleteDocument(doc2);
  }

  @Test(dependsOnMethods = "createDatabase")
  public void createAndExecuteDesignDocumentView() {
    Document doc1 = createDoc("ilya_test", "ilyas_doc", "{\"value\": 1, \"test2\": { \"val\": 1 }}");
    Document doc2 = createDoc("ilya_test", "ilyas_doc2", "{\"value\": 2, \"test2\": { \"val\": 1 }}");

    DesignDocument view = DesignDocument.createDesignDocument("ilya_test", "get_vals", null,
        Collections.<DesignDocument.View>singletonList(new DesignDocument.View("all", "function(doc) { emit(null, doc.value); }", null)));
    view = resourceMgr.createView(view);
    assertNotNull(view);
    assertEquals(view.getId(), "get_vals");

    ViewResult<Object, Integer> result =
        resourceMgr.executeView("ilya_test", "get_vals", "all", Object.class, Integer.class);
    assertNotNull(result);
    assertEquals(result.getOffset(), 0);
    assertEquals(result.getTotalRows(), 2);
    for (ViewResult.ViewResultRow<Object, Integer> resultRow : result.getResultRows()) {
      if (resultRow.getId().equals(doc1.getId())) {
        assertNull(resultRow.getKey());
        assertEquals(resultRow.getValue().intValue(), 1);
      }
      else if (resultRow.getId().equals(doc2.getId())) {
        assertNull(resultRow.getKey());
        assertEquals(resultRow.getValue().intValue(), 2);
      }
    }

    resourceMgr.deleteView(view);

    resourceMgr.deleteDocument(doc1);
    resourceMgr.deleteDocument(doc2);
  }

  @AfterClass
  public void cleanUp() {
    resourceMgr.deleteDatabase("ilya_test");
  }

  /* Support methods */

  private Document createDoc(String dbName, String name, String data) {
    Document doc = resourceMgr.saveDocument(new Document(
        dbName, name, data
    ));
    assertNotNull(doc);
    assertEquals(doc.getDatabaseName(), dbName);
    assertEquals(doc.getId(), name);
    return doc;
  }

}
