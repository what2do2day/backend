package com.one.what2do.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiConstants {
    public static final String PREDICTION_URL = "http://localhost:5000/predict"; // 예측 서버 URL
    
    private static String skTransitApiUrl;
    private static String skAppKey;
    
    @Value("${sk.api.transit-url}")
    public void setSkTransitApiUrl(String url) {
        skTransitApiUrl = url;
    }
    
    @Value("${sk.api.app-key}")
    public void setSkAppKey(String key) {
        skAppKey = key;
    }
    
    public static String getSkTransitApiUrl() {
        return skTransitApiUrl;
    }
    
    public static String getSkAppKey() {
        return skAppKey;
    }
    
    private ApiConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
} 