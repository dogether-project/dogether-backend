package site.dogether.common.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.dogether.auth.resolver.AuthenticatedArgumentResolver;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedArgumentResolver authenticatedArgumentResolver;

    public WebConfig(final AuthenticatedArgumentResolver authenticatedArgumentResolver) {
        this.authenticatedArgumentResolver = authenticatedArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedArgumentResolver);
    }
}
