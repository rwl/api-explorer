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

package com.google.api.explorer.client.history;

import com.google.api.explorer.client.ExplorerConfig;
import com.google.api.explorer.client.Resources;
import com.google.api.explorer.client.base.ApiRequest;
import com.google.api.explorer.client.base.ApiResponse;
import com.google.api.explorer.client.base.Config;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

public class HistoryItem extends Composite {

  private static HistoryItemUiBinder uiBinder = GWT.create(HistoryItemUiBinder.class);

  interface HistoryItemUiBinder extends UiBinder<Widget, HistoryItem> {
  }

  @UiField public FocusPanel titleBar;
  @UiField public SpanElement title;
  @UiField public SpanElement time;
  @UiField public DivElement collapseDiv;
  @UiField public SimplePanel errorPanel;
  @UiField public PreElement requestDiv;
  @UiField public PreElement requestBodyDiv;
  @UiField public PreElement statusDiv;
  @UiField public Label showHideHeaders;
  @UiField public PreElement responseHeadersDiv;
  @UiField public PreElement responseBodyDiv;

  protected HistoryItem(String methodIdentifier, long timeMillis, ApiRequest request,
      ApiResponse response) {
    initWidget();
    time.setInnerText("time to execute: " + timeMillis + " ms");

    String prefix = methodIdentifier + " executed ";
    PrettyDate.keepMakingPretty(new Date(), prefix, title);

    String dateString =
        DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(new Date());
    title.setTitle(dateString);

    requestDiv.setInnerText(getRequestString(request));

    statusDiv.setInnerText(response.status + " " + response.statusText);

    // Headers are hidden by default.
    UIObject.setVisible(responseHeadersDiv, false);
    responseHeadersDiv.setInnerText(getResponseHeadersString(response));
    JsonPrettifier.syntaxHighlight(responseBodyDiv, response.body);
    JsonPrettifier.syntaxHighlight(requestBodyDiv, request.body);
  }
  
  protected void initWidget() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("showHideHeaders")
  public void showHide(ClickEvent event) {
    showHideHeaders.setText(
        UIObject.isVisible(responseHeadersDiv) ? "- Show headers -" : "- Hide headers -");
    UIObject.setVisible(responseHeadersDiv, !UIObject.isVisible(responseHeadersDiv));
  }

  private static String getRequestString(ApiRequest request) {
    StringBuilder sb = new StringBuilder()
        .append(request.httpMethod.name())
        .append(' ')
        .append(Config.getBaseUrl())
        // If the standard API key is being used, mask it in the UI.
        // The URL is already URL-escaped before making the request, so we don't
        // want to double-escape it.
        .append(request.getRequestPath().replace("key=" + ExplorerConfig.API_KEY,
            "key={YOUR_API_KEY}"));

    // Display headers that were set on the request.
    // TODO(jasonhall): This can be prettier.
    sb.append('\n');
    for (Map.Entry<String, String> entry : request.headers.entrySet()) {
      sb.append('\n').append(entry.getKey()).append(":  ").append(entry.getValue());
    }

    return sb.toString();
  }

  private static String getResponseHeadersString(ApiResponse response) {
    StringBuilder sb = new StringBuilder();

    SortedMap<String, String> sorted = Maps.newTreeMap(Ordering.natural());
    sorted.putAll(response.headers);

    for (Map.Entry<String, String> entry : sorted.entrySet()) {
      sb.append(entry.getKey()).append(":  ").append(entry.getValue()).append('\n');
    }

    return sb.toString();
  }

  @UiHandler("titleBar")
  public void expandCollapse(ClickEvent event) {
    if (UIObject.isVisible(collapseDiv)) {
      collapse();
    } else {
      expand();
    }
  }

  void expand() {
    UIObject.setVisible(collapseDiv, true);
    titleBar.removeStyleName(Resources.INSTANCE.style().collapsed());
    titleBar.addStyleName(Resources.INSTANCE.style().expanded());
  }

  void collapse() {
    UIObject.setVisible(collapseDiv, false);
    titleBar.removeStyleName(Resources.INSTANCE.style().expanded());
    titleBar.addStyleName(Resources.INSTANCE.style().collapsed());
  }

  public void clear() {
    PrettyDate.stopMakingPretty(title);
  }

  void setErrorMessage(Widget prettyMessage) {
    errorPanel.setVisible(true);
    errorPanel.add(prettyMessage);
  }
}
