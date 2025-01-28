package site.dogether.docs.util;

import java.util.Map;

public class EnumDocs {

    Map<String, String> productCategory;

    public EnumDocs() {
    }

    public EnumDocs(final Map<String, String> productCategory) {
        this.productCategory = productCategory;
    }

    public Map<String, String> getProductCategory() {
        return productCategory;
    }
}
