/*
 * Copyright (C) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.explorer.client.base;

import com.google.api.explorer.client.base.Schema.Property.PropertyWrapper;
import com.google.common.collect.HashBasedTable;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanFactory.Category;

import java.util.Map;

/**
 * Represents an API service containing methods that can be called.
 * 
 * @author jasonhall@google.com (Jason Hall)
 */
public interface ApiService extends HasMethodsAndResources {

  /** Name of this service. */
  String getName();

  /** Version of this service. */
  String getVersion();

  /** Short description of this service. */
  String getDescription();

  /** Base path of all methods in this service; used for REST requests. */
  String getBasePath();

  /**
   * Returns the {@link ApiMethod} identified by the given method identifier,
   * belonging to the given {@link ApiService}, or {@code null} if no such
   * method exists.
   */
  ApiMethod method(String methodIdentifier);

  /**
   * Returns a {@link Map} of all the {@link ApiMethod}s available in this
   * service, keyed by the method's unique identifier.
   */
  Map<String, ApiMethod> allMethods();

  /**
   * Returns a {@link Map} of {@link AuthInformation} keyed by its auth type.
   * Currently, the only key will be "oauth2", describing OAuth 2.0
   * authentication information.
   */
  Map<String, AuthInformation> getAuth();

  /**
   * Represents an API service resource containing methods to call and possibly
   * other nested resources.
   */
  static interface ApiResource extends HasMethodsAndResources {
  }

  /** Represents information about authentication options for a service. */
  static interface AuthInformation {
    /**
     * Map of auth scope information, where the key is the auth scope URL and
     * the value is a {@link AuthScope} further describing that scope.
     */
    Map<String, AuthScope> getScopes();
  }

  /** Represents information about authentication scopes for a service. */
  static interface AuthScope {
    /** Returns the description of this authentication scope. */
    String getDescription();
  }

  /** Returns a mapping of all schemas used by this service. */
  Map<String, Schema> getSchemas();

  /**
   * Returns the request schema used by the given method, or {@code null} if
   * none is required.
   */
  Schema requestSchema(ApiMethod method);

  /**
   * Returns the response schema used by the given method, or {@code null} if
   * none is required.
   */
  Schema responseSchema(ApiMethod method);

  /**
   * Returns the schema used by the given schema property, or {@code null} if
   * the property does not require a schema (e.g., it is a simple type).
   */
  Schema schemaForProperty(Schema.Property property);

  /**
   * Wrapper class used by the AutoBeanFactory to provide the implementation of
   * some methods.
   *
   * <p>
   * All of these methods map to a method in {@link ApiService} which delegates
   * to the method in this class to provide its implementation.
   * </p>
   */
  class ApiServiceWrapper {
    private static final HashBasedTable<ApiService, String, ApiMethod> TABLE = HashBasedTable
        .create();

    private static void populateTable(String prefix, ApiService service,
        HasMethodsAndResources hasMethodsAndResources) {
      if (hasMethodsAndResources.getMethods() != null) {
        for (Map.Entry<String, ApiMethod> entry : hasMethodsAndResources.getMethods().entrySet()) {
          TABLE.put(service, prefix + entry.getKey(), entry.getValue());
        }
      }

      if (hasMethodsAndResources.getResources() != null) {
        for (Map.Entry<String, ApiService.ApiResource> entry : hasMethodsAndResources
            .getResources().entrySet()) {
          populateTable(prefix + entry.getKey() + ".", service, entry.getValue());
        }
      }
    }

    public static ApiMethod method(AutoBean<ApiService> instance, String methodIdentifier) {
      ApiService service = instance.as();
      if (!TABLE.containsColumn(service)) {
        populateTable("", service, service);
      }
      return TABLE.get(service, methodIdentifier);
    }

    public static Map<String, ApiMethod> allMethods(AutoBean<ApiService> instance) {
      ApiService service = instance.as();
      if (!TABLE.containsColumn(service)) {
        populateTable("", service, service);
      }
      return TABLE.row(service);
    }

    private static final String REF_KEY = "$ref";

    public static Schema requestSchema(AutoBean<ApiService> instance, ApiMethod method) {
      return method.getRequest() == null ? null : instance.as().getSchemas()
          .get(method.getRequest().get(REF_KEY));
    }

    public static Schema responseSchema(AutoBean<ApiService> instance, ApiMethod method) {
      return method.getResponse() == null ? null : instance.as().getSchemas()
          .get(method.getResponse().get(REF_KEY));
    }

    public static Schema schemaForProperty(AutoBean<ApiService> instance,
        Schema.Property property) {
      return property.getRef() == null ? null : instance.as().getSchemas().get(property.getRef());
    }
  }

  /**
   * Useful helper class to facilitate instantiation of {@link ApiService} s
   * from JSON strings.
   */
  public abstract static class Helper {
    private Helper() {
    } // Not instantiable.

    static ApiService fromString(String string) {
      Factory factory = GWT.create(Factory.class);
      return AutoBeanCodex.decode(factory, ApiService.class, string).as();
    }

    /**
     * {@link AutoBeanFactory} class for {@link ApiService}s.
     */
    @Category({ApiServiceWrapper.class, PropertyWrapper.class})
    public interface Factory extends AutoBeanFactory {
      AutoBean<ApiService> service();
    }
  }
}
