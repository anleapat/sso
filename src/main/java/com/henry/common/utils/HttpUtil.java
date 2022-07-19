package com.henry.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpUtil {
    private static final String REQUEST_HEADER_AJAX = "x-requested-with";
    private static final String XMLHttpRequest = "XMLHttpRequest";
    // if ajax request
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return StringUtils.isNotBlank(request.getHeader(REQUEST_HEADER_AJAX))
                && XMLHttpRequest.equals(request.getHeader(REQUEST_HEADER_AJAX));
    }

    // rewrite session timed out
    public static void writeUnLogin(HttpServletResponse response) {
        writeInfo(response, -1);
    }

    private static void writeInfo(HttpServletResponse response, int errorCode) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        try(PrintWriter writer = response.getWriter()) {
            String result ="{\"code\":"+errorCode+"}";
            writer.print(result);
            writer.flush();
        } catch (IOException ex) {
            log.error("writeInfo error", ex);
        }
    }

}
