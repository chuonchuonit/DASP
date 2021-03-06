package ddth.dasp.servlet.rp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import ddth.dasp.common.rp.AcfuUploadedFile;
import ddth.dasp.common.rp.IRequestParser;
import ddth.dasp.common.rp.MalformedRequestException;
import ddth.dasp.common.rp.RequestParsingInteruptedException;
import ddth.dasp.common.tempdir.TempDir;
import ddth.dasp.common.utils.DaspConstants;
import ddth.dasp.common.utils.ServletUtils;

/**
 * This implementation of {@link IRequestParser} is used to parse a HTTP
 * request.
 * 
 * @author NBThanh <btnguyen2k@gmail.com>
 */
public class HttpRequestParser extends AbstractRequestParser {

    private final static Pattern PATTERN_JSESSIONID = Pattern.compile(";jsessionid=[0-9a-f]+",
            Pattern.CASE_INSENSITIVE);
    private static Log LOGGER = LogFactory.getLog(HttpRequestParser.class);

    private HttpServletRequest httpRequest;
    private boolean isMultipart;
    private String contextPath;
    private String[] virtualPathParams;
    private Map<String, String> urlParams = new HashMap<String, String>();
    private Map<String, Object> formFields = new HashMap<String, Object>();
    private String requestUri;

    /**
     * Gets the associated HTTP request instance.
     * 
     * @return HttpServletRequest
     */
    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Associates the parser with a HTTP request instance.
     * 
     * @param httpRequest
     *            HttpServletRequeset
     */
    public void setHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
        this.isMultipart = ServletFileUpload.isMultipartContent(httpRequest);
    }

    /**
     * Checks if the request is "multipart".
     * 
     * @return
     */
    public boolean isMultipart() {
        return isMultipart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getFormFields() {
        return Collections.unmodifiableMap(formFields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFormField(String name) {
        return formFields.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestModule() {
        return getVirtualParameter(DaspConstants.PARAM_INDEX_MODULE);
    }

    /**
     * {@inheritDoc}
     */
    public String getRequestAction() {
        return getVirtualParameter(DaspConstants.PARAM_INDEX_ACTION);
    }

    /**
     * {@inheritDoc}
     */
    public String getRequestAuthKey() {
        return getVirtualParameter(DaspConstants.PARAM_INDEX_AUTHKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getUrlParameters() {
        return Collections.unmodifiableMap(urlParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlParameter(String name) {
        return urlParams.get(name);
    }

    /**
     * Gets an URL parameter as a Boolean.
     * 
     * @param name
     *            String
     * @return Boolean
     */
    public Boolean getUrlParameterAsBoolean(String name) {
        String value = getUrlParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();

        // special cases (English!)
        if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"))
            return true;
        if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false"))
            return false;

        try {
            return Double.parseDouble(value) != 0;
        } catch (Exception e1) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e2) {
                return false;
            }
        }
    }

    /**
     * Gets an URL parameter as a Number.
     * 
     * @param name
     *            String
     * @return Number an instance of Number, null if the input can not be parsed
     */
    public Double getUrlParameterAsNumber(String name) {
        String value = getUrlParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getVirtualParameters() {
        return virtualPathParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVirtualParameter(int index) {
        try {
            return this.virtualPathParams[index];
        } catch (Exception e) {
            // it must be either NullPointerException or
            // IndexOutOfBoundsException
            return null;
        }
    }

    /**
     * Gets a virtual parameter as a Boolean.
     * 
     * @param index
     *            int
     * @return Boolean
     */
    public Boolean getVirtualParameterAsBoolean(int index) {
        String value = getVirtualParameter(index);
        if (value == null) {
            return null;
        }
        value = value.trim();

        // special cases (English!)
        if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"))
            return true;
        if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false"))
            return false;

        try {
            return Double.parseDouble(value) != 0;
        } catch (Exception e1) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e2) {
                return false;
            }
        }
    }

    /**
     * Gets a virtual parameter as a Number.
     * 
     * @param index
     *            int
     * @return Number an instance of Number, null if the input can not be parsed
     */
    public Number getVirtualParameterAsNumber(int index) {
        String value = getVirtualParameter(index);
        if (value == null) {
            return null;
        }
        value = value.trim();

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalParseRequest() throws RequestParsingInteruptedException,
            MalformedRequestException {
        if (httpRequest == null) {
            throw new MalformedRequestException("The HTTP request is null!");
        }
        this.contextPath = httpRequest.getContextPath();
        if (this.contextPath == null) {
            this.contextPath = "";
        }
        String encoding = httpRequest.getCharacterEncoding();
        try {
            requestUri = ServletUtils.decodeURL(httpRequest.getRequestURI(), encoding);
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Client suplied an unknown encoding: " + encoding, e);
            requestUri = "";
        }
        // remove jsessionid if it exists
        requestUri = PATTERN_JSESSIONID.matcher(requestUri).replaceAll("");
        if (this.contextPath.length() > 0) {
            requestUri = requestUri.substring(this.contextPath.length());
        }
        this.virtualPathParams = requestUri.replaceAll("^/+", "").replaceAll("/+$", "").split("/");

        if (virtualPathParams.length > 0) {
            requestUri = requestUri.substring(virtualPathParams[0].length() + 1);
        }

        String queryString;
        try {
            queryString = ServletUtils.decodeURL(httpRequest.getQueryString(), encoding);
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Client suplied an unknown encoding: " + encoding, e);
            queryString = "";
        }
        urlParams.clear();
        String[] queries = queryString != null && queryString.trim().length() > 0 ? queryString
                .trim().split("&") : new String[0];
        for (String query : queries) {
            String[] st = query.split("=");
            if (st.length > 0) {
                if (st.length > 1) {
                    this.urlParams.put(st[0].trim(), st[1].trim());
                } else {
                    this.urlParams.put(st[0].trim(), "");
                }
            }
        }

        parseRequestContent();
        if (isInterrupted()) {
            throw new RequestParsingInteruptedException();
        }
    }

    /**
     * Parses the request content.
     * 
     * @throws MalformedRequestException
     */
    protected void parseRequestContent() throws MalformedRequestException {
        this.formFields.clear();
        if (this.isMultipart) {
            parseRequestContentMultipart();
        } else {
            ServletInputStream sis = null;
            try {
                byte[] buffer = new byte[1024];
                int counter = 0;
                int contentLength = httpRequest.getContentLength();
                sis = httpRequest.getInputStream();
                while (!isInterrupted() && counter < contentLength) {
                    /*
                     * Note: checking sis.available() is not working as expected
                     * as the method returns 0 usually!
                     */
                    int bytesRead = sis.read(buffer, 0, 1024);
                    write(buffer, 0, bytesRead);
                    counter += bytesRead;
                    if (System.currentTimeMillis() - getStartParsingTimestamp() > getTimeout()) {
                        interrupt();
                    }
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                IOUtils.closeQuietly(sis);
            }

            // populate form fields
            if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
                String encoding = httpRequest.getCharacterEncoding();
                if (StringUtils.isBlank(encoding)) {
                    encoding = DaspConstants.DEFAULT_CHARSET;
                }
                try {
                    String content = getRequestContent(encoding);
                    List<NameValuePair> formFields = URLEncodedUtils.parse(content,
                            Charset.forName(encoding));
                    for (NameValuePair formField : formFields) {
                        this.formFields.put(formField.getName(), formField.getValue());
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new MalformedRequestException(e.getMessage(), e);
                }
            }
        }
    }

    protected void parseRequestContentMultipart() throws MalformedRequestException {
        String encoding = httpRequest.getCharacterEncoding();
        if (StringUtils.isBlank(encoding)) {
            encoding = DaspConstants.DEFAULT_CHARSET;
        }

        TempDir tempDir = (TempDir) httpRequest
                .getAttribute(DaspConstants.REQ_ATTR_REQUEST_TEMP_DIR);
        FileItemFactory factory = new DiskFileItemFactory(8192, tempDir.get());
        // DASP will remove temp files.
        ((DiskFileItemFactory) factory).setFileCleaningTracker(null);
        ServletFileUpload upload = new ServletFileUpload(factory);
        // TODO: control max upload file size
        try {
            List<?> items = upload.parseRequest(httpRequest);
            for (Object item : items) {
                FileItem fileItem = (FileItem) item;
                String fieldName = fileItem.getFieldName();
                if (fileItem.isFormField()) {
                    try {
                        String fieldValue = fileItem.getString(encoding);
                        this.formFields.put(fieldName, fieldValue);
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.warn("Client suplied an unknown encoding: " + encoding, e);
                    }
                } else {
                    this.formFields.put(fieldName, new AcfuUploadedFile(fileItem));
                }
                if (System.currentTimeMillis() - getStartParsingTimestamp() > getTimeout()) {
                    interrupt();
                }
            }
        } catch (FileUploadException fue) {
            throw new MalformedRequestException(fue.getMessage(), fue);
        }
    }
}
