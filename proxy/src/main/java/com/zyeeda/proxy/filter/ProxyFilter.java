package com.zyeeda.proxy.filter;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Formatter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class ProxyFilter implements Filter {
    private String proxyUrl;
    private String reverseCookie = "false";

    protected String proxyTo;
    protected HttpClient proxyClient;
    protected FilterConfig filterConfig;
    protected static Cookie cookie = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
//        this.filterConfig = filterConfig;
//        HttpParams hcParams = new BasicHttpParams();
//        if("true".equals(reverseCookie)) {
//            hcParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
//        }
//        readConfigParam(hcParams, ClientPNames.HANDLE_REDIRECTS, Boolean.class);
//        proxyClient = createHttpClient(hcParams);
    }

//    protected void readConfigParam(HttpParams hcParams, String hcParamName, Class<?> type) {
//        String val_str = this.filterConfig.getInitParameter(hcParamName);
//        if (val_str == null)
//            return;
//        Object val_obj;
//        if (type == String.class) {
//            val_obj = val_str;
//        } else {
//            try {
//                val_obj = type.getMethod("valueOf", String.class).invoke(type, val_str);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        hcParams.setParameter(hcParamName, val_obj);
//    }

    protected HttpClient createHttpClient(HttpParams hcParams) {
        try {
            Class<?> clientClazz = Class.forName("org.apache.http.impl.client.SystemDefaultHttpClient");
            Constructor<?> constructor = clientClazz.getConstructor(HttpParams.class);
            return (HttpClient) constructor.newInstance(hcParams);
        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new DefaultHttpClient(new ThreadSafeClientConnManager(), hcParams);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpParams hcParams = new BasicHttpParams();
        if("true".equals(reverseCookie)) {
            hcParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        }
        proxyClient = createHttpClient(hcParams);

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        proxyTo = proxyUrl + servletRequest.getServletPath();
        String method = servletRequest.getMethod();
        String proxyRequestUri = rewriteUrlFromRequest(servletRequest);
        HttpRequest proxyRequest;
        if (servletRequest.getHeader(HttpHeaders.CONTENT_LENGTH) != null
                || servletRequest.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(
                    method, proxyRequestUri);
            eProxyRequest.setEntity(new InputStreamEntity(servletRequest
                    .getInputStream(), servletRequest.getContentLength()));
            proxyRequest = eProxyRequest;
        } else
            proxyRequest = new BasicHttpRequest(method, proxyRequestUri);

        copyRequestHeaders(servletRequest, proxyRequest);

        setXForwardedForHeader(servletRequest, proxyRequest);

        HttpResponse proxyResponse = null;
        try {
            proxyResponse = proxyClient.execute(
                    URIUtils.extractHost(getTargetUriObj()), proxyRequest);

            int statusCode = proxyResponse.getStatusLine().getStatusCode();

            if (doResponseRedirectOrNotModifiedLogic(servletRequest,
                    servletResponse, proxyResponse, statusCode)) {
                return;
            }

            servletResponse.setStatus(statusCode, proxyResponse.getStatusLine()
                    .getReasonPhrase());
//            if("true".equals(reverseCookie)) {
//                Header header = proxyResponse.getFirstHeader("Set-Cookie");
//                if(header != null) {
//                    String setCookie = header.getValue();
//                    String JSESSIONID = setCookie.substring("JSESSIONID=".length(), setCookie.indexOf(";"));
//                    cookie = new Cookie("BSESSIONID", JSESSIONID);
//                    servletResponse.addCookie(cookie);
//                }
//            }

            copyResponseHeaders(proxyResponse, servletResponse);
            copyResponseEntity(proxyResponse, servletResponse);
        } catch (Exception e) {
            if (proxyRequest instanceof AbortableHttpRequest) {
                AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
                abortableHttpRequest.abort();
            }
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            if (e instanceof ServletException)
                throw (ServletException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw new RuntimeException(e);

        } finally {
            if (proxyResponse != null) {
                consumeQuietly(proxyResponse.getEntity());
            }
            closeQuietly(servletResponse.getOutputStream());
        }
    }

    protected boolean doResponseRedirectOrNotModifiedLogic(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, HttpResponse proxyResponse,
            int statusCode) throws ServletException, IOException {
        if (statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
                && statusCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
            Header locationHeader = proxyResponse
                    .getLastHeader(HttpHeaders.LOCATION);
            if (locationHeader == null) {
                throw new ServletException("Received status code: "
                        + statusCode + " but no " + HttpHeaders.LOCATION
                        + " header was found in the response");
            }
            String locStr = rewriteUrlFromResponse(servletRequest,
                    locationHeader.getValue());

            servletResponse.sendRedirect(locStr);
            return true;
        }
        if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
            servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
            servletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }
        return false;
    }

    protected void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            System.out.println(e.getMessage() + e);
        }
    }

    /**
     * HttpClient v4.1 doesn't have the
     * {@link org.apache.http.util.EntityUtils#consumeQuietly(org.apache.http.HttpEntity)}
     * method.
     */
    protected void consumeQuietly(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {// ignore
            System.out.println(e.getMessage() + e);
        }
    }

    protected static final HeaderGroup hopByHopHeaders;
    static {
        hopByHopHeaders = new HeaderGroup();
        String[] headers = new String[] { "Connection", "Keep-Alive",
                "Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailers",
                "Transfer-Encoding", "Upgrade" };
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }

    protected void copyRequestHeaders(HttpServletRequest servletRequest,
            HttpRequest proxyRequest) {
        Enumeration<?> enumerationOfHeaderNames = servletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = (String) enumerationOfHeaderNames.nextElement();
            if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH))
                continue;
            if (hopByHopHeaders.containsHeader(headerName))
                continue;

            Enumeration<?> headers = servletRequest.getHeaders(headerName);
            while (headers.hasMoreElements()) {// sometimes more than one value
                String headerValue = (String) headers.nextElement();
                if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                    HttpHost host = URIUtils.extractHost(this.getTargetUriObj());
                    headerValue = host.getHostName();
                    if (host.getPort() != -1)
                        headerValue += ":" + host.getPort();
                }
//                if("Cookie".equals(headerName)) {
//                    if(reverseCookie.equals("true")) {
//                        String[] cookies = headerValue.split(";");
//                        for(String c : cookies) {
//                            if(c.startsWith("BSESSIONID")) {
//                                String tempValue = "JSESSIONID=" + c.split("=")[1];
//                                proxyRequest.addHeader(headerName, tempValue);
//                                break;
//                            }
//                        }
//                    }
//                }else {
//                    proxyRequest.addHeader(headerName, headerValue);
//                }
                 proxyRequest.addHeader(headerName, headerValue);

            }
        }
    }

    private void setXForwardedForHeader(HttpServletRequest servletRequest,
            HttpRequest proxyRequest) {
        String headerName = "X-Forwarded-For";
        String newHeader = servletRequest.getRemoteAddr();
        String existingHeader = servletRequest.getHeader(headerName);
        if (existingHeader != null) {
            newHeader = existingHeader + ", " + newHeader;
        }
        proxyRequest.setHeader(headerName, newHeader);
    }

    /** Copy proxied response headers back to the servlet client. */
    protected void copyResponseHeaders(HttpResponse proxyResponse,
            HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            if (hopByHopHeaders.containsHeader(header.getName()))
                continue;
            servletResponse.addHeader(header.getName(), header.getValue());
        }
    }

    /**
     * Copy response body data (the entity) from the proxy to the servlet
     * client.
     */
    protected void copyResponseEntity(HttpResponse proxyResponse,
            HttpServletResponse servletResponse) throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            OutputStream servletOutputStream = servletResponse
                    .getOutputStream();
            entity.writeTo(servletOutputStream);
        }
    }

    /**
     * Reads the request URI from {@code servletRequest} and rewrites it,
     * considering {@link #targetUriObj}. It's used to make the new request.
     */
    protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        StringBuilder uri = new StringBuilder(500);
        uri.append(proxyTo);
        // Handle the path given to the servlet
        if (servletRequest.getPathInfo() != null) {// ex: /my/path.html
            uri.append(encodeUriQuery(servletRequest.getPathInfo()));
        }
        // Handle the query string
        String queryString = servletRequest.getQueryString();// ex:(following
                                                             // '?'):
                                                             // name=value&foo=bar#fragment
        if (queryString != null && queryString.length() > 0) {
            uri.append('?');
            int fragIdx = queryString.indexOf('#');
            String queryNoFrag = (fragIdx < 0 ? queryString : queryString
                    .substring(0, fragIdx));
            uri.append(encodeUriQuery(queryNoFrag));
            if (fragIdx >= 0) {
                uri.append('#');
                uri.append(encodeUriQuery(queryString.substring(fragIdx + 1)));
            }
        }
        return uri.toString();
    }

    /**
     * For a redirect response from the target server, this translates
     * {@code theUrl} to redirect to and translates it to one the original
     * client can use.
     */
    protected String rewriteUrlFromResponse(HttpServletRequest servletRequest,
            String theUrl) {
        // TODO document example paths
        if (theUrl.startsWith(proxyTo)) {
            String curUrl = servletRequest.getRequestURL().toString();// no
                                                                      // query
            String pathInfo = servletRequest.getPathInfo();
            if (pathInfo != null) {
                assert curUrl.endsWith(pathInfo);
                curUrl = curUrl.substring(0,
                        curUrl.length() - pathInfo.length());// take pathInfo
                                                             // off
            }
            theUrl = curUrl + theUrl.substring(proxyTo.length());
        }
        return theUrl;
    }

    public URI getTargetUriObj() {
        URI targetUriObj = null;
        try {
            targetUriObj = new URI(proxyTo);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Trying to process targetUri init parameter: " + e, e);
        }
        return targetUriObj;
    }

    /**
     * Encodes characters in the query or fragment part of the URI.
     *
     * <p>
     * Unfortunately, an incoming URI sometimes has characters disallowed by the
     * spec. HttpClient insists that the outgoing proxied request has a valid
     * URI because it uses Java's {@link URI}. To be more forgiving, we must
     * escape the problematic characters. See the URI class for the spec.
     *
     * @param in
     *            example: name=value&foo=bar#fragment
     */
    @SuppressWarnings("resource")
    protected static CharSequence encodeUriQuery(CharSequence in) {
        // Note that I can't simply use URI.java to encode because it will
        // escape pre-existing escaped things.
        StringBuilder outBuf = null;
        Formatter formatter = null;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            boolean escape = true;
            if (c < 128) {
                if (asciiQueryChars.get(c)) {
                    escape = false;
                }
            } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {// not-ascii
                escape = false;
            }
            if (!escape) {
                if (outBuf != null)
                    outBuf.append(c);
            } else {
                // escape
                if (outBuf == null) {
                    outBuf = new StringBuilder(in.length() + 5 * 3);
                    outBuf.append(in, 0, i);
                    formatter = new Formatter(outBuf);
                }
                // leading %, 0 padded, width 2, capital hex
                formatter.format("%%%02X", (int) c);// TODO
            }
        }
        return outBuf != null ? outBuf : in;
    }

    protected static final BitSet asciiQueryChars;
    static {
        char[] c_unreserved = "_-!.~'()*".toCharArray();// plus alphanum
        char[] c_punct = ",;:$&+=".toCharArray();
        char[] c_reserved = "?/[]@".toCharArray();// plus punct

        asciiQueryChars = new BitSet(128);
        for (char c = 'a'; c <= 'z'; c++)
            asciiQueryChars.set(c);
        for (char c = 'A'; c <= 'Z'; c++)
            asciiQueryChars.set(c);
        for (char c = '0'; c <= '9'; c++)
            asciiQueryChars.set(c);
        for (char c : c_unreserved)
            asciiQueryChars.set(c);
        for (char c : c_punct)
            asciiQueryChars.set(c);
        for (char c : c_reserved)
            asciiQueryChars.set(c);

        asciiQueryChars.set('%');// leave existing percent escapes in place
    }

    @Override
    public void destroy() {
        // As of HttpComponents v4.3, clients implement closeable
        if (proxyClient instanceof Closeable) {// TODO AutoCloseable in Java 1.6
            try {
                ((Closeable) proxyClient).close();
            } catch (IOException e) {
                System.out.println("While destroying servlet, shutting down httpclient: " + e);
            }
        } else {
            // Older releases require we do this:
            if (proxyClient != null)
                proxyClient.getConnectionManager().shutdown();
        }
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public String getReverseCookie() {
        return reverseCookie;
    }

    public void setReverseCookie(String reverseCookie) {
        this.reverseCookie = reverseCookie;
    }

}
