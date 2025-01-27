package site.dogether.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected MockMvc mockMvc;
    protected RestDocumentationResultHandler restDocs;

    protected static Attributes.Attribute constraints(final String value) {
        return new Attributes.Attribute("constraints", value);
    }

    @BeforeEach
    void setUp(final RestDocumentationContextProvider provider) {
        this.restDocs = MockMvcRestDocumentation.document("{class-name}/{method-name}");
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .apply(documentationConfiguration(provider)
                    .operationPreprocessors()
                    .withRequestDefaults(
                        modifyHeaders()
                            .remove("Content-Length")
                            .remove("Host"),
                        prettyPrint())
                    .withResponseDefaults(
                        modifyHeaders()
                            .remove("Content-Length")
                            .remove("X-Content-Type-Options")
                            .remove("X-XSS-Protection")
                            .remove("Cache-Control")
                            .remove("Pragma")
                            .remove("Expires")
                            .remove("X-Frame-Options"),
                        prettyPrint()))
                .alwaysDo(restDocs)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }

    protected abstract Object initController();
}
