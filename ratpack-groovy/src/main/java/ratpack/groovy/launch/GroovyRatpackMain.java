/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.groovy.launch;

import groovy.lang.GroovySystem;
import ratpack.groovy.launch.internal.GroovyVersionCheck;
import ratpack.launch.LaunchConfigs;
import ratpack.launch.RatpackMain;

import java.util.Properties;

/**
 * The standard “main” entry point for Groovy script based apps.
 */
public class GroovyRatpackMain extends RatpackMain {

  public static void main(String[] args) {
    new GroovyRatpackMain().startOrExit();
  }

  @Override
  public void start() throws Exception {
    GroovyVersionCheck.ensureRequiredVersionUsed(GroovySystem.getVersion());
    super.start();
  }

  @Override
  protected void addImpliedDefaults(Properties properties) {
    properties.put(LaunchConfigs.Property.HANDLER_FACTORY, GroovyScriptFileHandlerFactory.class.getName());
    properties.put(GroovyScriptFileHandlerFactory.SCRIPT_PROPERTY_NAME, "ratpack.groovy");
  }
}
