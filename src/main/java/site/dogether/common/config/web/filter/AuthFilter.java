package site.dogether.common.config.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import site.dogether.auth.infrastructure.JwtHandler;

@RequiredArgsConstructor
public class AuthFilter implements Filter {

    private final JwtHandler jwtHandler;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;

        final String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null) {
            jwtHandler.validateToken(bearerToken);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
