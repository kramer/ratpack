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

package ratpack.handling

import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.render.Renderer
import ratpack.test.internal.RatpackGroovyDslSpec

import static ratpack.registry.Registries.just

class ErrorHandlingSpec extends RatpackGroovyDslSpec {

  def "handles 404"() {
    when:
    handlers {}

    then:
    get().statusCode == 404
  }

  def "handles internal error"() {
    when:
    handlers {
      get { throw new RuntimeException('error here') }
    }

    then:
    get().statusCode == 500
  }

  def "can segment error handlers"() {
    given:
    def errorHandler1 = new ServerErrorHandler() {
      void error(Context exchange, Exception exception) {
        exchange.response.send("1: $exception.message")
      }
    }
    def errorHandler2 = new ServerErrorHandler() {
      void error(Context exchange, Exception exception) {
        exchange.response.send("2: $exception.message")
      }
    }

    when:
    bindings {
      bind ServerErrorHandler, errorHandler1
    }
    handlers {
      register(just(errorHandler2)) {
        get("a") {
          throw new Exception("1")
        }
      }
      get("b") {
        throw new Exception("2")
      }
    }

    then:
    getText("a") == "2: 1"
    getText("b") == "1: 2"
  }

  def "can use service handler"() {
    given:
    def errorHandler = new ServerErrorHandler() {
      void error(Context exchange, Exception exception) {
        exchange.response.send("Caught: $exception.message")
      }
    }

    when:
    bindings {
      bind ServerErrorHandler, errorHandler
    }
    handlers {
      get {
        throw new Exception("thrown")
      }
    }

    then:
    text == "Caught: thrown"
  }

  def "exceptions thrown by error handler are dealt with"() {
    when:
    bindings {
      bind ServerErrorHandler, new ServerErrorHandler() {
        @Override
        void error(Context context, Exception exception) {
          throw new RuntimeException("in error handler")
        }
      }
    }

    handlers {
      get {
        throw new RuntimeException("in handler")
      }
    }

    then:
    text == ""
    response.statusCode == 500
  }

  def "exceptions thrown by render in server error handler are dealt with deterministically"() {
    when:
    bindings {
      bind Renderer, new Renderer<Map>() {
        @Override
        Class<Map> getType() { Map }

        @Override
        void render(Context context, Map object) throws Exception {
          throw new RuntimeException("Error rendering map")
        }
      }

      bind ServerErrorHandler, new ServerErrorHandler() {
        @Override
        void error(Context context, Exception exception) {
          context.render([:])
        }
      }
    }

    handlers {
      get {
        throw new RuntimeException("in handler")
      }
    }

    then:
    text == ""
    response.statusCode == 500
  }

  def "exceptions thrown by render in server error handler while reloadable has response body"() {
    when:
    launchConfig { reloadable(true) }
    bindings {
      bind Renderer, new Renderer<Map>() {
        @Override
        Class<Map> getType() { Map }

        @Override
        void render(Context context, Map object) throws Exception {
          throw new RuntimeException("Error rendering map")
        }
      }

      bind ServerErrorHandler, new ServerErrorHandler() {
        @Override
        void error(Context context, Exception exception) {
          context.render([:])
        }
      }
    }
    handlers {
      get {
        throw new RuntimeException("in handler")
      }
    }

    then:
    def responseText = text
    response.statusCode == 500
    responseText.length() > 0
    responseText.contains("Exception thrown by error handler")
    responseText.contains("Original exception:")
    responseText.contains("Error handler exception:")
  }

  def "exceptions thrown by client error handler are dealt with deterministically from error prone server error handler"() {
    when:
    bindings {
      bind ClientErrorHandler, new ClientErrorHandler() {
        @Override
        void error(Context context, int statusCode) throws Exception {
          throw new RuntimeException("Error rendering client error")
        }
      }
      bind Renderer, new Renderer<Map>() {
        @Override
        Class<Map> getType() { Map }

        @Override
        void render(Context context, Map object) throws Exception {
          throw new RuntimeException("Error rendering map")
        }
      }
      bind ServerErrorHandler, new ServerErrorHandler() {
        @Override
        void error(Context context, Exception exception) {
          context.render([:])
        }
      }
    }

    handlers {
      get {
        clientError(400)
      }
    }

    then:
    text == ""
    response.statusCode == 500
  }

}
