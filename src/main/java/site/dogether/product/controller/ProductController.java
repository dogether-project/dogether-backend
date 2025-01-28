package site.dogether.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.product.controller.request.CreateProductRequest;
import site.dogether.product.controller.response.SearchProductsResponse;

import java.net.URI;

@RequestMapping("/api/products")
@RestController
public class ProductController {

    @GetMapping
    public ResponseEntity<ApiResponse<SearchProductsResponse>> searchProducts(
        @RequestParam(required = false) final String category
    ) {
        return ResponseEntity.ok(ApiResponse.successWithData(
            "상품 검색이 완료되었습니다.",
            new SearchProductsResponse(
                "켈리의 맥북",
                100000000,
                "아주아주 귀한 물건"
            )
        ));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<SearchProductsResponse>> getProductById(@PathVariable final Long productId) {
        return ResponseEntity.ok(ApiResponse.successWithData(
            "상품 상세 조회가 왼료되었습니다.",
            new SearchProductsResponse(
                "뿌링클",
                30000,
                "냠냠 쫩쫩"
            )
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createProduct(@RequestBody final CreateProductRequest request) {
        return ResponseEntity.created(URI.create("/api/products/1")).body(ApiResponse.success("상품 정보가 저장되었습니다."));
    }
}
