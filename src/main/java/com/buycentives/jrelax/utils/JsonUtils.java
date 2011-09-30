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
package com.buycentives.jrelax.utils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ilya Sterin
 * @version 1.0
 */
public class JsonUtils {

  private static ObjectMapper mapper = new ObjectMapper();

  private JsonUtils() {
  }

  public static <T> T parseToJson(String jsonString, Class<T> clazz) {
    try {
      return mapper.readValue(jsonString, clazz);
    }
    catch (IOException e) {
      throw new IllegalArgumentException("Couldn't parse json string into (" + clazz.getName() + "): " + jsonString);
    }
  }

  public static ObjectNode createJsonObjectNode() {
    return new ObjectNode(mapper.getNodeFactory());
  }

  public static Map<String, String> createMapFor(String... args) {
    if (args.length % 2 > 0)
      throw new IllegalArgumentException("The argument count must be event to account for key/value pairs.  You provided " + args.length + " arguments.");
    Map<String, String> argMap = new HashMap<String, String>();
    for (int i = 0; i < args.length; i = i + 2) {
      argMap.put(args[i], args[i + 1]);
    }
    return argMap;
  }

}
