package com.one.what2do.dto.waypoint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.one.what2do.dto.detailed.SkTransitDetailedResponseDto;
import lombok.*;

import java.util.List;

/**
 * SK 교통 API 경유지 경로 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkTransitWaypointResponseDto {
    
    private List<RouteSegment> segments;  // 각 구간별 경로 정보
    private RouteSummary summary;         // 전체 경로 요약 정보
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteSegment {
        private Integer sequence;           // 구간 순서
        private String fromName;            // 출발지명
        private String toName;              // 도착지명
        private String fromLon;             // 출발지 경도
        private String fromLat;             // 출발지 위도
        private String toLon;               // 도착지 경도
        private String toLat;               // 도착지 위도
        private Integer totalTime;          // 구간별 총 소요 시간 (초)
        private Integer totalDistance;      // 구간별 총 거리 (미터)
        private Integer totalFare;          // 구간별 총 요금 (원)
        private Integer totalWalkTime;      // 구간별 총 보행 시간 (초)
        private Integer transferCount;      // 구간별 환승 횟수
        private String routeType;           // 경로 타입 (fastest, minLegs)
        private List<Object> legs;          // 구간별 상세 경로 (모드별 다른 DTO)
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteSummary {
        private Integer totalTime;          // 전체 총 소요 시간 (초)
        private Integer totalDistance;      // 전체 총 거리 (미터)
        private Integer totalFare;          // 전체 총 요금 (원)
        private Integer totalWalkTime;      // 전체 총 보행 시간 (초)
        private Integer totalTransferCount; // 전체 총 환승 횟수
        private Integer segmentCount;       // 총 구간 수
        private List<String> waypointNames; // 경유지명 목록
    }
    
    // 공통 Location 클래스
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private String name;
        private String lon;
        private String lat;
    }
    
    // 공통 PassShape 클래스
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PassShape {
        private String lineString;
    }
    
    // 공통 Station 클래스
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Station {
        private Integer index;
        private String stationName;
        private String lon;
        private String lat;
        private String stationId;
    }
    
    // 공통 Lane 클래스 (버스용)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lane {
        private String routeColor;
        private String route;
        private String routeId;
        private Integer service;
        private Integer type;
    }
    
    // WALK 모드용 Leg
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WalkLeg {
        private String mode = "WALK";
        private Integer sectionTime;
        private Integer distance;
        private Location start;
        private Location end;
        private List<WalkStep> steps;
        private PassShape passShape;
    }
    
    // WALK 모드용 Step
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WalkStep {
        private String streetName;
        private Integer distance;
        private String description;
        private String linestring;
    }
    
    // SUBWAY 모드용 Leg
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubwayLeg {
        private String mode = "SUBWAY";
        private String routeColor;
        private Integer sectionTime;
        private String route;
        private String routeId;
        private Integer distance;
        private Integer service;
        private Location start;
        private SubwayPassStopList passStopList;
        private Location end;
        private Integer type;
        private PassShape passShape;
    }
    
    // SUBWAY 모드용 PassStopList
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubwayPassStopList {
        private List<Station> stationList;
    }
    
    // BUS 모드용 Leg
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusLeg {
        private String routeColor;
        private Integer distance;
        private Location start;
        private List<Lane> lane;
        private Integer type;
        private String mode = "BUS";
        private Integer sectionTime;
        private String route;
        private String routeId;
        private Integer service;
        private BusPassStopList passStopList;
        private Location end;
        private PassShape passShape;
    }
    
    // BUS 모드용 PassStopList
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusPassStopList {
        private List<Station> stationList;
    }
    
    // 기존 Leg 클래스 (호환성을 위해 유지)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {
        private String mode;
        private Integer sectionTime;
        private Integer distance;
        private Location start;
        private Location end;
        private String routeColor;
        private String route;
        private String routeId;
        private Integer type;
        private Integer service;
        private List<Step> steps;           // 상세 단계 정보
        private PassStopList passStopList;  // 정거장 정보
        private PassShape passShape;        // 경로 모양 정보
    }
    
    // 기존 Step 클래스 (호환성을 위해 유지)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private String streetName;
        private Integer distance;
        private String description;
        private String linestring;
        private Integer sectionTime;
        private String lon;
        private String lat;
        private String name; // 정류장명(정류장 step일 때)
        private String stationId; // 정류장ID(정류장 step일 때)
        private Integer sequence; // 정류장 순서(정류장 step일 때)
    }
    
    // 기존 PassStopList 클래스 (호환성을 위해 유지)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PassStopList {
        private List<Stop> stations;  // 지하철 정거장 목록
        private List<Stop> busStops;  // 버스 정거장 목록
    }
    
    // 기존 Stop 클래스 (호환성을 위해 유지)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stop {
        private String name;        // 정거장명
        private String lon;         // 경도
        private String lat;         // 위도
        private String stationId;   // 정거장 ID
        private Integer sequence;   // 순서
    }
    
    // 기존 Coordinate 클래스 (호환성을 위해 유지)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinate {
        private String lon;  // 경도
        private String lat;  // 위도
    }
} 