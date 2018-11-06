/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.controller;

import com.haulmont.cuba.core.global.FileTypesHelper;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;

/**
 * Controller returns static content necessary for activiti modeler
 */
@Controller("bpm_ContentController")
@RequestMapping(value = "/modeler/**")
public class ContentController {

    public interface LookupResult {
        void respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException;

        void respondHead(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    }

    public static class Error implements LookupResult {
        protected final int statusCode;
        protected final String message;

        public Error(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        @Override
        public void respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.sendError(statusCode, message);
        }

        @Override
        public void respondHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    public static class StaticFile implements LookupResult {
        protected final String mimeType;
        protected final int contentLength;
        protected final boolean acceptsDeflate;
        protected final URL url;

        public StaticFile(String mimeType, int contentLength, boolean acceptsDeflate, URL url) {
            this.mimeType = mimeType;
            this.contentLength = contentLength;
            this.acceptsDeflate = acceptsDeflate;
            this.url = url;
        }

        protected boolean willDeflate() {
            return acceptsDeflate && deflatable(mimeType) && contentLength >= deflateThreshold;
        }

        protected void setHeaders(HttpServletResponse resp) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(mimeType);
            if (contentLength >= 0 && !willDeflate())
                resp.setContentLength(contentLength);
        }

        @Override
        public void respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            setHeaders(resp);
            final OutputStream os;
            if (willDeflate()) {
                resp.setHeader("Content-Encoding", "gzip");
                os = new GZIPOutputStream(resp.getOutputStream(), bufferSize);
            } else
                os = resp.getOutputStream();
            InputStream is = url.openStream();
            try {
                IOUtils.copy(is, os);
            } finally {
                is.close();
                os.close();
            }
        }

        @Override
        public void respondHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            if (willDeflate())
                throw new UnsupportedOperationException();
            setHeaders(resp);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            lookup(request).respondGet(request, response);
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            return handleGetRequest(request, response);
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public String handleHeadRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            try {
                lookup(request).respondHead(request, response);
            } catch (UnsupportedOperationException e) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        }
        return null;
    }

    protected LookupResult lookup(HttpServletRequest req) {
        LookupResult r = (LookupResult) req.getAttribute("lookupResult");
        if (r == null) {
            r = lookupNoCache(req);
            req.setAttribute("lookupResult", r);
        }
        return r;
    }

    protected LookupResult lookupNoCache(HttpServletRequest req) {
        final String path = getPath(req);
        if (isForbidden(path))
            return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");

        ServletContext context = req.getSession().getServletContext();

        final URL url;
        try {
            url = context.getResource(path);
        } catch (MalformedURLException e) {
            return new Error(HttpServletResponse.SC_BAD_REQUEST, "Malformed path");
        }
        if (url == null)
            return new Error(HttpServletResponse.SC_NOT_FOUND, "Not found");

        final String mimeType = getMimeType(path);

        final String realpath = context.getRealPath(path);
        if (realpath != null) {
            // Try as an ordinary file
            File f = new File(realpath);
            if (!f.isFile())
                return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            else {
                return createLookupResult(req, mimeType, (int) f.length(), acceptsDeflate(req), url);
            }
        } else {
            try {
                // Try as a JAR Entry
                final ZipEntry ze = ((JarURLConnection) url.openConnection()).getJarEntry();
                if (ze != null) {
                    if (ze.isDirectory())
                        return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                    else
                        return createLookupResult(req, mimeType, (int) ze.getSize(), acceptsDeflate(req), url);
                } else
                    // Unexpected?
                    return new StaticFile(mimeType, -1, acceptsDeflate(req), url);
            } catch (ClassCastException e) {
                // Unknown resource type
                return createLookupResult(req, mimeType, -1, acceptsDeflate(req), url);
            } catch (IOException e) {
                return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            }
        }
    }

    protected LookupResult createLookupResult(
            HttpServletRequest req, String mimeType, int contentLength, boolean acceptsDeflate, URL url) {
        return new StaticFile(mimeType, contentLength, acceptsDeflate, url);
    }

    protected boolean checkUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserSession userSession = ControllerUtils.getUserSession(request);
        if (userSession != null) {
            return true;
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }

    protected String getPath(HttpServletRequest req) {
        String path = ControllerUtils.getControllerPath(req);
        String pathInfo = coalesce(req.getPathInfo(), "");
        return path + pathInfo;
    }

    protected boolean isForbidden(String path) {
        String lpath = path.toLowerCase();
        return lpath.startsWith("/web-inf/") || lpath.startsWith("/meta-inf/");
    }

    protected String getMimeType(String path) {
        return coalesce(FileTypesHelper.getMIMEType(path), "application/octet-stream");
    }

    protected static boolean acceptsDeflate(HttpServletRequest req) {
        final String ae = req.getHeader("Accept-Encoding");
        return ae != null && ae.contains("gzip");
    }

    protected static boolean deflatable(String mimetype) {
        return mimetype.startsWith("text/")
                || mimetype.equals("application/postscript")
                || mimetype.startsWith("application/ms")
                || mimetype.startsWith("application/vnd")
                || mimetype.endsWith("xml");
    }

    public static <T> T coalesce(T... ts) {
        for (T t : ts)
            if (t != null)
                return t;
        return null;
    }

    protected static final int deflateThreshold = 4 * 1024;

    protected static final int bufferSize = 4 * 1024;
}