/*
 * Copyright 2014 the original author or authors.
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

package ratpack.http.client;

import io.netty.buffer.ByteBuf;
import ratpack.func.Action;
import ratpack.http.HttpUrlSpec;
import ratpack.http.MutableHeaders;

import java.io.OutputStream;

public interface RequestSpec {

  /**
   * @return {@link ratpack.http.MutableHeaders} that can be used to configure the headers that will be used for the request.
   */
  MutableHeaders getHeaders();

  RequestSpec headers(Action<? super MutableHeaders> action) throws Exception;

  /**
   * Set the HTTP verb to use.
   * @param method which HTTP verb to use
   * @return this
   */
  RequestSpec method(String method);

  HttpUrlSpec getUrl();

  RequestSpec url(Action<? super HttpUrlSpec> action) throws Exception;

  Body getBody();

  RequestSpec body(Action<? super Body> action) throws Exception;

  interface Body {

    Body type(String contentType);

    Body stream(Action<? super OutputStream> action) throws Exception;

    Body buffer(ByteBuf byteBuf);

    Body bytes(byte[] bytes);

  }

}
