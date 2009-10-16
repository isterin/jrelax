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

import com.buycentives.jrelax.utils.JsonUtils;
import org.codehaus.jackson.node.ObjectNode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ilya Sterin
 * @version 1.0
 */
public class DesignDocument implements CouchJsonResource, Serializable {

  private final String databaseName;
  private String id;
  private String revision;
  private String language = "javascript";
  private List<View> views;

  private DesignDocument(String databaseName, String id, String revision, View... views) {
    this(databaseName, id, revision, Arrays.asList(views));
  }

  private DesignDocument(String databaseName, String id, String revision, List<View> views) {
    this.databaseName = databaseName;
    this.id = id;
    this.revision = revision;
    this.views = views;
  }

  public static DesignDocument createDesignDocumentForTemporaryView(String databaseName, String map, String reduce) {
    return new DesignDocument(databaseName, null, null, new View(map, reduce));
  }

  public static DesignDocument createDesignDocument(String databaseName, String id, String revision, List<View> views) {
    if (id == null)
      throw new IllegalArgumentException("You must provide a design document id in order to create a non-temporary view.");
    if (views == null || views.size() == 0)
      throw new IllegalArgumentException("You must provide at least one view for the design document.");  
    return new DesignDocument(databaseName, id, revision, views);
  }

  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getRevision() {
    return revision;
  }

  private boolean isTemporary() {
    return getId() == null;
  }

  public String getLanguage() {
    return language;
  }

  public List<View> getViews() {
    return views; 
  }

  @Override
  public String asJson() {
    ObjectNode node = JsonUtils.createJsonObjectNode();
    if (this.isTemporary()) {
      View view = this.views.get(0);
      node.put("map", view.map);
      if (view.reduce != null)
        node.put("reduce", view.reduce);
    }
    else {
      if (getId() != null) node.put("_id", getId());
      if (getRevision() != null) node.put("_rev", getRevision());
      node.put("language", language);
      node.put("views", JsonUtils.createJsonObjectNode());
      for (View v : views) {
        ObjectNode viewNode = JsonUtils.createJsonObjectNode();
        if (v.map != null) viewNode.put("map", v.map);
        if (v.reduce != null) viewNode.put("reduce", v.reduce);
        ((ObjectNode) node.get("views")).put(v.name, viewNode);
      }
    }

    return node.toString();
  }

  public static class View implements Serializable {
    private final String name;
    private final String map;
    private final String reduce;

    public View(String name, String map, String reduce) {
      if (map == null)
            throw new IllegalArgumentException("You must provide a view for the temporary design document view.");      
      this.name = name;
      this.map = map;
      this.reduce = reduce;
    }

    public View(String map, String reduce) {
      this(null, map, reduce);
    }

  }


}
