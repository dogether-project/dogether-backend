package site.dogether.product.controller;

import lombok.RequiredArgsConstructor;
import site.dogether.common.constant.EnumType;

@RequiredArgsConstructor
public enum ProductCategory implements EnumType {

    CHICKEN("치킨"),
    SELL("판매"),
    GOOD("좋아용");

    private final String description;

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
