package com.sesac.solbid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config implements WebMvcConfigurer {

    // properties에서 직접 주입 (env 아님 -> 후에 .env 파일로 수정 예정)
    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String regionStr;

    private StaticCredentialsProvider staticCreds() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    /*
    * 주입 예시
    * @Value("${app.s3.bucket}")
       private String bucket;*/

    /*
     * S3 설정*/
    private Region region() {
        return Region.of(regionStr);
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(region())
                .credentialsProvider(staticCreds())
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(region())
                .credentialsProvider(staticCreds())
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}