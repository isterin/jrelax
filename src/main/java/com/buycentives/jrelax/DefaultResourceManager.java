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

import com.buycentives.jrelax.CouchResponse;
import static com.buycentives.jrelax.utils.JsonUtils.*;
import static com.buycentives.jrelax.utils.StringUtils.*;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Status;

import java.util.*;

/**
 * @author Ilya Sterin
 * @version 1.0
 */
public class DefaultResourceManager implements ResourceManager {

  private static final String CREATE_DB_URI = "/${name}/";
  private static final String LIST_DBS_URI = "/_all_dbs";

  private static final String DOC_URI = "/${dbName}/${name}";
  private static final String REVISION_DOC_URI = "/${dbName}/${name}?rev=${rev}";

  private static final String CREATE_TEMP_VIEW_URI = "/${dbName}/_temp_view";
  private static final String CREATE_VIEW_URI = "/${dbName}/_design/${name}";
  private static final String EXECUTE_VIEW_URI = "/${dbName}/_design/${docName}/_view/${viewName}";


  private Session session;

  public DefaultResourceManager(String baseUrl) {
    this(new Session(baseUrl));
  }

  public DefaultResourceManager(Session session) {
    this.session = session;
  }

  /**
   * Create a couchdb database.  Method throws a RuntimeException
   *
   * @param name name of the database
   */
  @Override
  public void createDatabase(String name) {
    try {
      CouchResponse<Map> response =
          session.put(interpolate(CREATE_DB_URI, Collections.singletonMap("name", name)), null);
      if (!response.isStatusEqualsAndOk(Status.SUCCESS_CREATED)) {
        throw couchDbExceptionInstance("Couldn't create database: (" + name + ").", response);
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Couldn't create database: (" + name + ").", e);
    }
  }

  @Override
  public boolean databaseExists(String name) {
    CouchResponse<Map> response =
        session.get(interpolate(CREATE_DB_URI, Collections.singletonMap("name", name)), Map.class);
    if (response.isStatusEquals(Status.SUCCESS_OK)) {
      Map dbProps = response.getResponseObject();
      if (name.equals(dbProps.get("db_name"))) {
        return true;
      }
      return false;
    }
    throw couchDbExceptionInstance("Database lookup failed for (" + name + ")", response);
  }

  @Override
  public void deleteDatabase(String name) {
    try {
      CouchResponse<Map> response =
          session.delete(interpolate(CREATE_DB_URI, Collections.singletonMap("name", name)));
      if (!response.isStatusEqualsAndOk(Status.SUCCESS_OK)) {
        throw couchDbExceptionInstance("Couldn't delete database: (" + name + ").", response);
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Couldn't delete database: " + name, e);
    }
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public List<String> listDatabases() {
    CouchResponse<List> response = session.get(LIST_DBS_URI, List.class);
    if (response.isStatusEquals(Status.SUCCESS_OK)) {
      return response.getResponseObject();
    }
    return Collections.emptyList();
  }

  @Override
  public Document saveDocument(Document doc) {
    Map<String, String> args = new HashMap<String, String>();
    args.put("dbName", doc.getDatabaseName());
    args.put("name", doc.getId());
    System.err.println("Creating document: " + interpolate(DOC_URI, args));
    CouchResponse<Map> response = session.put(interpolate(DOC_URI, args), doc);
    if (response.isStatusEqualsAndOk(Status.SUCCESS_CREATED)) {
      Map docInfo = response.getResponseObject();
      return new Document(doc.getDatabaseName(), (String) docInfo.get("id"), (String) docInfo.get("rev"), doc.getData());
    }
    throw couchDbExceptionInstance(
        "Couldn't create document: (/" + doc.getDatabaseName() + "/" + doc.getId() + ").", response);
  }

  @Override
  public Document saveDocument(String dbName, String name, String jsonData) {
    return saveDocument(new Document(dbName, name, jsonData));
  }

  @Override
  public Document getDocument(String dbName, String name) {
    return getDocument(dbName, name, null);
  }

  @Override
  public Document getDocument(String dbName, String name, String revisionId) {
    String uri = revisionId != null ?
        interpolate(REVISION_DOC_URI, createMapFor(
            "dbName", dbName,
            "name", name,
            "rev", revisionId
        )) :
        interpolate(DOC_URI, createMapFor(
            "dbName", dbName,
            "name", name
        ));
    try {
      CouchResponse<JsonNode> response = session.get(uri, JsonNode.class);
      if (response.isStatusEquals(Status.SUCCESS_OK)) {
        JsonNode jsonDoc = response.getResponseObject();
        return new Document(dbName, jsonDoc.get("_id").getTextValue(), jsonDoc.get("_rev").getTextValue(), jsonDoc.toString());
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Couldn't retrieve document: (" + uri + ").", e);
    }
    return null;
  }

  @Override
  public void deleteDocument(Document doc) {
    deleteDocument(doc.getDatabaseName(), doc.getId(), doc.getRevision());
  }

  @Override
  public void deleteDocument(String dbName, String name, String revisionId) {
    Map<String, String> args = new HashMap<String, String>();
    args.put("dbName", dbName);
    args.put("name", name);
    args.put("rev", revisionId);
    String uri = interpolate(REVISION_DOC_URI, args);
    try {
      CouchResponse<Map> response = session.delete(uri);
      if (!response.isStatusEqualsAndOk(Status.SUCCESS_OK)) {
        throw couchDbExceptionInstance("Couldn't delete document : (" + uri + ")", response);
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Couldn't delete document: (" + uri + ")", e);
    }
  }

  @Override
  public <K, V> ViewResult<K, V> executeTemporaryView(String databaseName, String map, String reduce,
                                                      Class<K> keyType, Class<V> valueType) {
    return executeTemporaryView(
        DesignDocument.createDesignDocumentForTemporaryView(databaseName, map, reduce), keyType, valueType
    );
  }

  @Override
  public <K, V> ViewResult<K, V> executeTemporaryView(DesignDocument viewDoc, Class<K> keyType, Class<V> valueType) {
    String uri = interpolate(CREATE_TEMP_VIEW_URI, Collections.singletonMap("dbName", viewDoc.getDatabaseName()));
    try {
      CouchResponse<JsonNode> response = session.post(uri, viewDoc, JsonNode.class);
      if (!response.isStatusEquals(Status.SUCCESS_OK)) {
        throw couchDbExceptionInstance("Couldn't create temporary view: (" + uri + ")", response);
      }
      JsonNode result = response.getResponseObject();
      List<ViewResult.ViewResultRow<K, V>> rows = new ArrayList<ViewResult.ViewResultRow<K, V>>();
      for (JsonNode node : result.path("rows")) {
        rows.add(new ViewResult.ViewResultRow<K, V>(
            node.path("id").getTextValue(),
            node.path("key").toString(), keyType,
            node.path("value").toString(), valueType
        ));
      }
      return new ViewResult<K, V>(result.path("total_rows").getIntValue(), result.path("offset").getIntValue(), rows);
    }
    catch (Exception e) {
      throw new RuntimeException("Couldn't create temporary view: (" + uri + ")", e);
    }
  }


  @Override
  public DesignDocument createView(DesignDocument view) {
    String uri = interpolate(CREATE_VIEW_URI, createMapFor("dbName", view.getDatabaseName(), "name", view.getId()));
    try {
      CouchResponse<Map> response = session.put(uri, view);
      if (response.isStatusEqualsAndOk(Status.SUCCESS_CREATED)) {
        Map docInfo = response.getResponseObject();
        return DesignDocument.createDesignDocument(
            view.getDatabaseName(), ((String) docInfo.get("id")).replace("_design/", ""), (String) docInfo.get("rev"), view.getViews());
      }
      throw couchDbExceptionInstance("Couldn't delete document : (" + uri + ")", response);
    }
    catch (Exception e) {
      throw new RuntimeException("Couldn't create view: (" + uri + ")", e);
    }
  }


  @Override
  public <K, V> ViewResult<K, V> executeView(String dbName, String docName, String viewName, Class<K> keyType, Class<V> valueType) {
    String uri = interpolate(EXECUTE_VIEW_URI,
        createMapFor(
            "dbName", dbName,
            "docName", docName,
            "viewName", viewName
        ));
    try {
      CouchResponse<JsonNode> response =
          session.get(uri, JsonNode.class);
      if (!response.isStatusEquals(Status.SUCCESS_OK)) {
        throw couchDbExceptionInstance("Couldn't execute view: (" + uri + ")", response);
      }
      JsonNode result = response.getResponseObject();
      List<ViewResult.ViewResultRow<K, V>> rows = new ArrayList<ViewResult.ViewResultRow<K, V>>();
      for (JsonNode node : result.path("rows")) {
        rows.add(new ViewResult.ViewResultRow<K, V>(
            node.path("id").getTextValue(),
            node.path("key").toString(), keyType,
            node.path("value").toString(), valueType
        ));
      }
      return new ViewResult<K, V>(result.path("total_rows").getIntValue(), result.path("offset").getIntValue(), rows);
    }
    catch (CouchDbException e) {
      throw new RuntimeException("Couldn't execute view: (" + uri + ")", e);
    }
  }


  @Override
  public void deleteView(DesignDocument view) {
    deleteView(view.getDatabaseName(), view.getId(), view.getRevision());
  }

  @Override
  public void deleteView(String dbName, String viewName, String revisionId) {
    deleteDocument(dbName, "_design/" + viewName, revisionId);
  }

  private CouchDbException couchDbExceptionInstance(String msg, CouchResponse response) {
    throw new CouchDbException(msg + "\nReturned: " +
        response.getStatus() + " - " + response.getStatusDescription(), response.getStatus());
  }

}
