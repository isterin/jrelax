/*
Copyright (c) 2011, Ilya Sterin
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
import org.codehaus.jackson.JsonNode;

/**
 * @author Ilya Sterin
 * @version 1.0
 */
public class Document implements CouchJsonResource {

  private final String databaseName;
  private final String id;
  private String revision;
  private JsonNode data;

  public Document(String databaseName, String id, String data) {
    this.databaseName = databaseName;
    this.id = id;
    this.data = JsonUtils.parseToJson(data, JsonNode.class);
  }

  public Document(String databaseName, String id, String revision, String data) {
    this(databaseName, id, data);
    this.revision = revision;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public String getData() {
    return data.toString();
  }

  @Override
  public String asJson() {
    return data.toString();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getRevision() {
    return revision;
  }
}
