package site.dogether.product.controller.response;

public record SearchProductsResponse(
    String name,
    int price,
    String description
) {
}
