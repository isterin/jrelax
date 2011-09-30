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

import com.buycentives.jrelax.CouchResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ClientResource;

import java.util.Map;

/**
 * @author Ilya Sterin
 * @version 1.0
 */
public class Session {

  public static final String CREATE_DB_URI = "/${name}/";
  public static final String LIST_DBS_URI = "/_all_dbs";

  private final String baseUrl;

  private ObjectMapper jsonMapper = new ObjectMapper();

  public Session(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /*
  ------------- Package protected methods below, for use only with CouchDB implementation classes -------------
  */

  /**
   * @param uri            relative uri path to the resource.
   * @param jsonResultType The type of the result object.  It's usually JSONObject or JSONArray, but can be any type
   *                       that has a constructor which excepts a json string.
   * @param <T>            The response object type
   * @return Returns the CouchResponse who's responseObject property is set to the return JSON string parsed into the
   *         <T> jsonResultType object, using it's constructor which accepts a single String argument.
   */
  <T> CouchResponse<T> get(String uri, Class<T> jsonResultType) {
    try {
      ClientResource resource = new ClientResource(fullUrlFor(uri));
      resource.get();
      if (resource.getStatus().isSuccess()) {
        System.err.println("Response entity: " + resource.getStatus());
        T obj = jsonMapper.readValue(resource.getResponseEntity().getText(), jsonResultType);
        return new CouchResponse<T>(resource.getStatus(), obj);
      }
      return new CouchResponse<T>(resource.getStatus());
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to retrieve resource (" + fullUrlFor(uri) + ")", e);
    }
  }

  <T> CouchResponse<T> post(String uri, CouchJsonResource object, Class<T> jsonResultType) {
    System.err.println("POSTING TO: " + fullUrlFor(uri));
    ClientResource resource = new ClientResource(fullUrlFor(uri));
    try {
      if (object != null) {
        System.err.println("PUTTING: " + object.asJson());
        resource.post(new JsonRepresentation(object.asJson()));
      }
      else {
        resource.post(null);
      }
      if (resource.getStatus().isSuccess()) {
        String responseBody = resource.getResponseEntity().getText();
        System.err.println("RESPONSE: " + responseBody);
        return new CouchResponse<T>(resource.getStatus(),
            jsonMapper.readValue(responseBody, jsonResultType));
      }
      return new CouchResponse<T>(resource.getStatus()); 
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to post resource (" + fullUrlFor(uri) + ")", e);
    }
  }

  CouchResponse<Map> delete(String uri) {
    System.err.println("Deleting: " + fullUrlFor(uri));
    try {
      ClientResource resource = new ClientResource(fullUrlFor(uri));
      resource.delete();
      return new CouchResponse<Map>(resource.getStatus(),
          jsonMapper.readValue(resource.getResponseEntity().getText(), Map.class));
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to delete resource (" + fullUrlFor(uri) + ")", e);
    }
  }

  CouchResponse<Map> put(String uri, CouchJsonResource object) {
    System.err.println("PUTTING TO: " + fullUrlFor(uri));
    ClientResource resource = new ClientResource(fullUrlFor(uri));
    try {
      if (object != null) {
        System.err.println("PUTTING: " + object.asJson());
        resource.put(new JsonRepresentation(object.asJson()));
      }
      else
        resource.put(null);
      return new CouchResponse<Map>(resource.getStatus(),
          jsonMapper.readValue(resource.getResponseEntity().getText(), Map.class));
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to create resource (" + fullUrlFor(uri) + ")", e);
    }
  }

  /*
  ------------- Private methods below -------------
  */

  private String fullUrlFor(String uri) {
    return baseUrl + uri;
  }

}