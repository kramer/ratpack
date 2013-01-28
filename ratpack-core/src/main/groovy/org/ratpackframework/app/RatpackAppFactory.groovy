/*
 * Copyright 2012 the original author or authors.
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

package org.ratpackframework.app

import groovy.transform.CompileStatic
import org.vertx.java.core.Vertx
import org.ratpackframework.routing.internal.ScriptBackedRouter
import org.ratpackframework.templating.TemplateRenderer

@CompileStatic
public class RatpackAppFactory {

  RatpackApp create(Vertx vertx, Config config) {
    def publicDir = new File(config.baseDir, config.publicDir)
    def templateRenderer = new TemplateRenderer(vertx, new File(config.baseDir, config.templatesDir), config.templatesCacheSize)
    def router = new ScriptBackedRouter(vertx, new File(config.baseDir, config.routes), templateRenderer)
    new RatpackApp(vertx, config.port, "/", router, templateRenderer, publicDir)
  }

}