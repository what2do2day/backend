package com.one.what2do.dto.detailed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * SK 교통 API 상세 경로 응답 DTO
 * 모든 정거장 정보를 포함하는 버전
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkTransitDetailedResponseDto {
    
    private MetaData metaData;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {
        private Plan plan;
        private RequestParameters requestParameters;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Plan {
        private List<DetailedItinerary> itineraries;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailedItinerary {
        private Integer totalTime;
        private Integer totalDistance;
        private Integer totalWalkTime;
        private Integer transferCount;
        private Integer totalWalkDistance;
        private Integer pathType;
        private List<DetailedLeg> legs;
        private Fare fare;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailedLeg {
        private String mode;
        private Integer sectionTime;
        private Integer distance;
        private Location start;
        private Location end;
        private List<Step> steps;
        private String routeColor;
        private String route;
        private String routeId;
        private Integer type;
        private Integer service;
        private PassStopList passStopList;  // 모든 정거장 정보
        private PassShape passShape;        // 경로 모양 정보
        private List<Lane> Lane;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PassStopList {
        private List<Stop> stationList;  // 지하철 정거장 목록 (SK API 필드명과 일치)
        private List<Stop> busStops;  // 버스 정거장 목록
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stop {
        private String stationName;  // 정거장명 (SK API 필드명과 일치)
        private String lon;          // 경도
        private String lat;          // 위도
        private String stationID;    // 정거장 ID (SK API 필드명과 일치)
        private Integer index;       // 순서 (SK API 필드명과 일치)
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PassShape {
        private List<Coordinate> coordinates;  // 경로 좌표 목록
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinate {
        private String lon;  // 경도
        private String lat;  // 위도
    }
    
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
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fare {
        private Regular regular;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Regular {
        private Integer totalFare;
        private Currency currency;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Currency {
        private String symbol;
        private String currency;
        private String currencyCode;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequestParameters {
        private Integer busCount;
        private Integer expressbusCount;
        private Integer subwayCount;
        private Integer airplaneCount;
        private String locale;
        private String endY;
        private String endX;
        private Integer wideareaRouteCount;
        private Integer subwayBusCount;
        private String startY;
        private String startX;
        private Integer ferryCount;
        private Integer trainCount;
        private String reqDttm;
    }
} 