package site.dogether.docs.util;

public final class DocumentLinkGenerator {

    public static String generateLink(final DocUrl docUrl) {
        return String.format("link:enum/%s.html[%s,role=\"popup\"]", docUrl.fileName, docUrl.title);
    }

    public enum DocUrl {
        PRODUCT_CATEGORY("product-category", "상품 카테고리");

        private final String fileName;
        private final String title;

        DocUrl(final String fileName, final String title) {
            this.fileName = fileName;
            this.title = title;
        }
    }
}
