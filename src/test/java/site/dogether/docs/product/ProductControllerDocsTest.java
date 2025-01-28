package site.dogether.docs.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.product.controller.ProductController;
import site.dogether.product.controller.request.CreateProductRequest;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.dogether.docs.util.DocumentLinkGenerator.DocUrl.PRODUCT_CATEGORY;
import static site.dogether.docs.util.DocumentLinkGenerator.generateLink;

public class ProductControllerDocsTest extends RestDocsSupport {

    @Override
    protected Object initController() {
        return new ProductController();
    }

    @DisplayName("상품 검색 API")
    @Test
    void searchProduct() throws Exception {
        final String category = "CHICKEN";

        mockMvc.perform(
                   get("/api/products")
                       .queryParam("category", category))
               .andExpect(status().isOk())
               .andDo(createDocument(
                   queryParameters(
                       parameterWithName("category")
//                           .description("link:enum/product-category.html[상품 카테고리,role=\"popup\"]")
                           .description(generateLink(PRODUCT_CATEGORY))
                           .optional()
                           .attributes(constraints("시스템 제공 값만 입력 가능"))),
                   responseFields(
                       fieldWithPath("message")
                           .description("응답 메시지")
                           .type(JsonFieldType.STRING),
                       fieldWithPath("data.name")
                           .description("상품명")
                           .type(JsonFieldType.STRING),
                       fieldWithPath("data.price")
                           .description("상품 가격")
                           .type(JsonFieldType.NUMBER),
                       fieldWithPath("data.description")
                           .description("상품 설명")
                           .type(JsonFieldType.STRING)
                   )));
    }

    @DisplayName("상품 상세 조회 API")
    @Test
    void getProductById() throws Exception {
        final long productId = 343L;

        mockMvc.perform(get("/api/products/{productId}", productId))
               .andExpect(status().isOk())
               .andDo(createDocument(
                   pathParameters(
                       parameterWithName("productId")
                           .description("상품 id")
                           .attributes(constraints("등록된 상품 id만 입력 가능"))),
                   responseFields(
                       fieldWithPath("message")
                           .description("응답 메시지")
                           .type(JsonFieldType.STRING),
                       fieldWithPath("data.name")
                           .description("상품명")
                           .type(JsonFieldType.STRING),
                       fieldWithPath("data.price")
                           .description("상품 가격")
                           .type(JsonFieldType.NUMBER),
                       fieldWithPath("data.description")
                           .description("상품 설명")
                           .type(JsonFieldType.STRING)
                   )));
    }

    @DisplayName("신규 상품 등록 API")
    @Test
    void createProduct() throws Exception {
        final CreateProductRequest request = new CreateProductRequest(
            "맛좋은 커피",
            100000,
            "개꿀맛 커피!!! 이거 한 잔이면 와따여 와따!");

        mockMvc.perform(
                   post("/api/products")
                       .content(convertToJson(request))
                       .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isCreated())
               .andDo(createDocument(
                   requestFields(
                       fieldWithPath("name")
                           .description("상품명")
                           .type(JsonFieldType.STRING)
                           .optional()
                           .attributes(constraints("2 ~ 10자 문자열")),
                       fieldWithPath("price")
                           .description("상품 가격")
                           .type(JsonFieldType.NUMBER)
                           .attributes(constraints("5자 문자열")),
                       fieldWithPath("description")
                           .description("상품 설명")
                           .type(JsonFieldType.STRING)
                           .attributes(constraints("까리하게"))
                   ),
                   responseFields(
                       fieldWithPath("message")
                           .description("응답 메시지")
                           .type(JsonFieldType.STRING)
                   )));
    }
}
