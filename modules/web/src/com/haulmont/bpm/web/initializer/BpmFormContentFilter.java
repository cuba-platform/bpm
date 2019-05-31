/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.bpm.web.initializer;

import org.springframework.http.HttpMethod;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BpmFormContentFilter implements Filter {
    protected static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    protected static final Charset FORM_CHARSET = StandardCharsets.UTF_8;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            throw new ServletException("BpmFormContentFilter just supports HTTP requests");
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpServletRequest wrappedRequest = httpRequest;
        if (isFormPut(httpRequest) && !(request instanceof ContentCachingRequestWrapper)) {
            wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        }

        chain.doFilter(wrappedRequest, response);
    }

    private static boolean isFormPut(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && contentType.contains(FORM_CONTENT_TYPE) &&
                HttpMethod.PUT.matches(request.getMethod()));
    }

    @Override
    public void destroy() {
    }

    protected static class ContentCachingRequestWrapper extends HttpServletRequestWrapper {
        public ContentCachingRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ContentCachingInputStream(getBodyFromServletRequestParameters((HttpServletRequest) getRequest()));
        }

        protected ByteArrayInputStream getBodyFromServletRequestParameters(HttpServletRequest request) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            Writer writer = new OutputStreamWriter(bos, FORM_CHARSET);

            Map<String, String[]> form = request.getParameterMap();
            for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext(); ) {
                String name = nameIterator.next();
                List<String> values = Arrays.asList(form.get(name));
                for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
                    String value = valueIterator.next();
                    writer.write(URLEncoder.encode(name, FORM_CHARSET.name()));
                    if (value != null) {
                        writer.write('=');
                        writer.write(URLEncoder.encode(value, FORM_CHARSET.name()));
                        if (valueIterator.hasNext()) {
                            writer.write('&');
                        }
                    }
                }
                if (nameIterator.hasNext()) {
                    writer.append('&');
                }
            }
            writer.flush();

            return new ByteArrayInputStream(bos.toByteArray());
        }
    }

    protected static class ContentCachingInputStream extends ServletInputStream {
        protected ByteArrayInputStream inputStream;

        public ContentCachingInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public int read() {
            return inputStream.read();
        }

        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }
    }
}
