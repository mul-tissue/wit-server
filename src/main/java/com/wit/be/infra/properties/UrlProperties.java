package com.wit.be.infra.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * URL 관련 설정 Properties.
 *
 * @param clients CORS 허용 클라이언트 URL 목록
 * @param server Swagger 서버 URL
 */
@ConfigurationProperties("urls")
public record UrlProperties(List<String> clients, String server) {}
