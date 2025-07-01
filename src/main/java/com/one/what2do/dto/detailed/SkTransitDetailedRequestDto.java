package com.one.what2do.dto.detailed;

import lombok.*;

/**
 * SK 교통 API 상세 경로 요청 DTO
 * 모든 정거장 정보를 포함하는 버전
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkTransitDetailedRequestDto {
    
    private String startX;        // 출발지 경도
    private String startY;        // 출발지 위도
    private String endX;          // 도착지 경도
    private String endY;          // 도착지 위도
    private Integer lang;         // 언어 (0: 한국어)
    private String format;        // 응답 형식 (json)
    private Integer count;        // 경로 개수
    private Boolean includeDetailedStops; // 상세 정거장 정보 포함 여부
} 