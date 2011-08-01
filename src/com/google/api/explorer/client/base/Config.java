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

import com.google.common.base.Strings;
import com.google.gwt.user.client.Window;

/**
 * Class containing constants and configuration values for the library.
 *
 * @author jasonhall@google.com (Jason Hall)
 */
public abstract class Config {

  public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
  public static final String CLIENT_ID = "835264079878.apps.googleusercontent.com";
  public static final String DEFAULT_BASE_URL = "https://www.googleapis.com";

  /** Path to the Directory listing all APIs. */
  public static final String DIRECTORY_REQUEST_PATH =
      "/discovery/" + ApiServiceFactory.DISCOVERY_VERSION + "/apis";

  private static final String VERSION = "0.1-alpha";
  private static final String PRIVATE_API_KEY = "tt";

  private static String baseUrl = DEFAULT_BASE_URL;
  private static String userAgent = "google-api-gwt-client/" + VERSION;
  private static String apiKey = "";
  private static String discoveryAuthToken = null;

  private Config() {
  } // Not instantiable.

  /** Set the name of this application. */
  public static void setApplicationName(String applicationName) {
    userAgent = applicationName + " " + userAgent;
  }

  static String getUserAgent() {
    return userAgent;
  }

  /** Set the base URL to use when making requests. */
  public static void setBaseUrl(String baseUrl) {
    Config.baseUrl = baseUrl;
  }

  /**
   * Returns the base URL to use when making requests.
   *
   * <p>
   * By default, this is "https://www.googleapis.com"
   * </p>
   */
  public static String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Set the API key to use for requests from this application.
   *
   * <p>
   * For more information about API keys, and to get your API key, see
   * {@link "https://code.google.com/apis/console/"}
   * </p>
   */
  public static void setApiKey(String apiKey) {
    Config.apiKey = apiKey;
  }

  /** Get the API key to use for requests from this application. */
  public static String getApiKey() {
    return Strings.nullToEmpty(apiKey);
  }

  /** Set the OAuth2 token to use when making Discovery requests. */
  public static void setDiscoveryAuthToken(String token) {
    Config.discoveryAuthToken = token;
  }

  /**
   * Returns the OAuth2 token to use when making Discovery requests, or
   * {@code null} if none was specified.
   */
  public static String getDiscoveryAuthToken() {
    return Config.discoveryAuthToken;
  }

  /**
   * Returns whether or not the Private APIs UI is enabled in the Explorer.
   * @return
   */
  public static boolean isPrivateApiEnabled() {
    return Window.Location.getParameter(PRIVATE_API_KEY) != null;
  }
}
