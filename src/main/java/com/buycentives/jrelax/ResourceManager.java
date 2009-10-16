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

import java.util.List;

/**
 * @author Ilya Sterin
 * @version 1.0
 */
public interface ResourceManager {

  void createDatabase(String name);

  boolean databaseExists(String name);

  void deleteDatabase(String name);

  List<String> listDatabases();

  Document saveDocument(Document doc);

  Document saveDocument(String dbName, String name, String jsonData);

  Document getDocument(String dbName, String name);

  Document getDocument(String dbName, String name, String revisionId);

  void deleteDocument(Document doc);

  void deleteDocument(String dbName, String name, String revisionId);

  DesignDocument createView(DesignDocument view);

  <K, V> ViewResult<K, V> executeView(String dbName, String docName, String viewName, Class<K> keyType, Class<V> valueType);

  void deleteView(DesignDocument view);

  void deleteView(String dbName, String viewName, String revisionId);

  <K, V> ViewResult<K, V> executeTemporaryView(DesignDocument viewDoc, Class<K> keyType, Class<V> valueType);

  <K, V> ViewResult<K, V> executeTemporaryView(String databaseName, String map, String reduce, Class<K> keyType, Class<V> valueType);

}
