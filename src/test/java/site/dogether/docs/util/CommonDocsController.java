package site.dogether.docs.util;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.common.constant.EnumType;
import site.dogether.product.controller.ProductCategory;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.*;

@RequestMapping("/test")
@RestController
public class CommonDocsController {

    @GetMapping("/enums")
    public EnumDocs findEnums() {
        final Map<String, String> productCategory = convertToMap(ProductCategory.values());
        return new EnumDocs(productCategory);
    }

    private Map<String, String> convertToMap(final EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                     .collect(toMap(EnumType::getName, EnumType::getDescription));
    }
}
