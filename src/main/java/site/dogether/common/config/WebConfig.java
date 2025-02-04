package site.dogether.common.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.dogether.auth.resolver.AuthTokenArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthTokenArgumentResolver authTokenArgumentResolver;

    public WebConfig(final AuthTokenArgumentResolver authTokenArgumentResolver) {
        this.authTokenArgumentResolver = authTokenArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authTokenArgumentResolver);
    }

}
