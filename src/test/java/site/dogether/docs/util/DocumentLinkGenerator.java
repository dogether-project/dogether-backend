package site.dogether.docs.util;

public final class DocumentLinkGenerator {

    public static String generateLink(final DocUrl docUrl) {
        return String.format("link:enum/%s.html[%s,role=\"popup\"]", docUrl.fileName, docUrl.title);
    }

    public enum DocUrl {
        CHALLENGE_GROUP_START_AT_OPTION("challenge-group-start-at-option", "그룹 시작일 옵션"),
        CHALLENGE_GROUP_DURATION_OPTION("challenge-group-duration-option", "그룹 진행 기간 옵션"),
        CHALLENGE_GROUP_STATUS("challenge-group-status", "그룹 상태 타입"),
        DAILY_TODO_CERTIFICATION_REVIEW_RESULT("daily-todo-certification-review-result", "데일리 투두 수행 인증 검사 결과 옵션"),
        S3_UPLOAD_FILE_TYPE("s3-upload-file-type", "S3 업로드 파일 타입 옵션"),
        DAILY_TODO_STATUS("daily-todo-status", "데일리 투두 상태 옵션"),
        ;

        private final String fileName;
        private final String title;

        DocUrl(final String fileName, final String title) {
            this.fileName = fileName;
            this.title = title;
        }
    }
}
