/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.nyc.integration_tests;

import org.junit.After;
import org.junit.Before;
import org.onebusaway.nyc.integration_tests.CustomDefaultSelenium;

public class WebTestSupport extends CustomDefaultSelenium {

  private String _prefix = "/onebusaway-webapp";

  public WebTestSupport() {
    super("http://localhost:"
        + System.getProperty("org.onebusaway.webapp.port", "9000") + "/",
        "*firefox");
  }

  public WebTestSupport(String prefix) {
    this();
    setPrefix(prefix);
  }

  public void setPrefix(String prefix) {
    _prefix = prefix;
  }

  @Before
  public void setup() {
    start();
  }

  @After
  public void tearDown() {
    stop();
  }

  @Override
  public void open(String message) {
    open(url(message), true);
  }

  protected String url(String url) {
    StringBuilder b = new StringBuilder();
    if (_prefix != null)
      b.append(_prefix);
    if (b.length() > 0 && !url.startsWith("/"))
      b.append('/');
    b.append(url);
    return b.toString();
  }
}
