package com.one.what2do.dto.waypoint;

import lombok.*;

import java.util.List;

/**
 * SK 교통 API 경유지 경로 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkTransitWaypointRequestDto {
    
    private List<Waypoint> waypoints;  // 경유지 목록 (순서대로)
    private Integer lang;              // 언어 (0: 한국어)
    private String format;             // 응답 형식 (json)
    private Integer count;             // 각 구간별 경로 개수
    private Boolean includeDetailedStops; // 상세 정거장 정보 포함 여부
    private String routeType;          // 경로 타입 (fastest, minLegs)
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Waypoint {
        private String name;    // 장소명
        private String lon;     // 경도
        private String lat;     // 위도
        private Integer sequence; // 순서
    }
} 