package site.dogether.s3.url_generator;

public interface PresignedUrlGenerator {

    String generate(Long dailyTodoId, String uploadFileType);
}
