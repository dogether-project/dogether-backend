package site.dogether.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ApiRequestLoggingFilter implements Filter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(
        final ServletRequest request,
        final ServletResponse response,
        final FilterChain filterChain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            // 요청을 CachedBodyHttpServletRequest로 래핑
            final CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);

            // URL, 메서드 및 요청 바디 로깅
            final String url = wrappedRequest.getRequestURI();
            final String method = wrappedRequest.getMethod();
            final String rawBody = wrappedRequest.getReader().lines().collect(Collectors.joining());

            final String prettyHeader = formatHeaders(wrappedRequest);
            final String prettyBody = formatJsonBody(rawBody);

            log.trace("Incoming API Request ({})\n======== [ Header ] ========\n{}\n\n======== [ Body ] ========\n{}", method + " " + url, prettyHeader, prettyBody + "\n");

            // 래핑된 요청 객체를 다음 필터 체인으로 전달
            filterChain.doFilter(wrappedRequest, response);
        } else {
            // HttpServletRequest가 아닌 경우 그대로 전달
            filterChain.doFilter(request, response);
        }
    }

    private String formatJsonBody(final String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            return "(empty)";
        }

        try {
            final Object json = objectMapper.readValue(rawBody, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (final Exception e) {
            // JSON이 아닐 경우 그대로 출력 (예: plain text)
            return rawBody;
        }
    }

    private String formatHeaders(final HttpServletRequest request) {
        try {
            final var headers = request.getHeaderNames();
            final var map = new java.util.LinkedHashMap<String, String>();

            while (headers.hasMoreElements()) {
                final String name = headers.nextElement();
                final String value = request.getHeader(name);
                map.put(name, value);
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        } catch (final Exception e) {
            return "(헤더 파싱에 문제가 발생했습니다.)";
        }
    }
}
