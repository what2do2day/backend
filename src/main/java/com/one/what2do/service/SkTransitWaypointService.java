package com.one.what2do.service;

import com.one.what2do.dto.waypoint.SkTransitWaypointRequestDto;
import com.one.what2do.dto.waypoint.SkTransitWaypointResponseDto;
import com.one.what2do.dto.detailed.SkTransitDetailedResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SK 교통 API 경유지 경로 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkTransitWaypointService {

    private final SkTransitDetailedService skTransitDetailedService;

    /**
     * 경유지를 포함한 상세 경로를 조회합니다.
     * 각 구간별로 상세 경로(모든 정거장 포함)를 계산합니다.
     * 
     * @param request 경유지 요청 정보
     * @return 경유지 상세 경로 정보
     */
    public SkTransitWaypointResponseDto getWaypointRoute(SkTransitWaypointRequestDto request) {
        try {
            List<SkTransitWaypointRequestDto.Waypoint> waypoints = request.getWaypoints();
            
            if (waypoints == null || waypoints.size() < 2) {
                log.warn("경유지가 2개 미만입니다. 최소 2개의 경유지가 필요합니다.");
                return null;
            }

            log.info("경유지 상세 경로 조회 시작: {}개 경유지", waypoints.size());
            
            List<SkTransitWaypointResponseDto.RouteSegment> segments = new ArrayList<>();
            int totalTime = 0;
            int totalDistance = 0;
            int totalFare = 0;
            int totalWalkTime = 0;
            int totalTransferCount = 0;
            List<String> waypointNames = new ArrayList<>();

            // 각 구간별로 상세 경로 조회
            for (int i = 0; i < waypoints.size() - 1; i++) {
                SkTransitWaypointRequestDto.Waypoint from = waypoints.get(i);
                SkTransitWaypointRequestDto.Waypoint to = waypoints.get(i + 1);
                
                log.info("구간 {} 상세 경로 조회: {} -> {}", i + 1, from.getName(), to.getName());
                
                // 상세 경로 조회 (보행 경로 fallback 포함)
                SkTransitDetailedResponseDto detailedRouteResponse = skTransitDetailedService.getDetailedTransitRoute(
                    from.getLon(), from.getLat(), to.getLon(), to.getLat()
                );
                
                if (detailedRouteResponse != null && detailedRouteResponse.getMetaData() != null && 
                    detailedRouteResponse.getMetaData().getPlan() != null && 
                    detailedRouteResponse.getMetaData().getPlan().getItineraries() != null &&
                    !detailedRouteResponse.getMetaData().getPlan().getItineraries().isEmpty()) {
                    
                    SkTransitDetailedResponseDto.DetailedItinerary itinerary = 
                        detailedRouteResponse.getMetaData().getPlan().getItineraries().get(0);
                    
                    // 구간 정보 생성 (상세 경로 포함)
                    SkTransitWaypointResponseDto.RouteSegment segment = SkTransitWaypointResponseDto.RouteSegment.builder()
                            .sequence(i + 1)
                            .fromName(from.getName())
                            .toName(to.getName())
                            .fromLon(from.getLon())
                            .fromLat(from.getLat())
                            .toLon(to.getLon())
                            .toLat(to.getLat())
                            .totalTime(itinerary.getTotalTime())
                            .totalDistance(itinerary.getTotalDistance())
                            .totalFare(itinerary.getFare() != null && itinerary.getFare().getRegular() != null ? 
                                      itinerary.getFare().getRegular().getTotalFare() : 0)
                            .totalWalkTime(itinerary.getTotalWalkTime())
                            .transferCount(itinerary.getTransferCount())
                            .routeType(request.getRouteType() != null ? request.getRouteType() : "fastest")
                            .build();
                    
                    // legs 필드를 별도로 설정 (모드별 DTO 사용)
                    segment.setLegs(convertDetailedLegs(itinerary.getLegs()));
                    
                    segments.add(segment);
                    
                    // 전체 통계 누적
                    totalTime += itinerary.getTotalTime() != null ? itinerary.getTotalTime() : 0;
                    totalDistance += itinerary.getTotalDistance() != null ? itinerary.getTotalDistance() : 0;
                    totalFare += itinerary.getFare() != null && itinerary.getFare().getRegular() != null ? 
                                itinerary.getFare().getRegular().getTotalFare() : 0;
                    totalWalkTime += itinerary.getTotalWalkTime() != null ? itinerary.getTotalWalkTime() : 0;
                    totalTransferCount += itinerary.getTransferCount() != null ? itinerary.getTransferCount() : 0;
                    
                    if (i == 0) {
                        waypointNames.add(from.getName());
                    }
                    waypointNames.add(to.getName());
                    
                    log.info("구간 {} 상세 경로 완료: {}분, {}m, {}원", i + 1, 
                            itinerary.getTotalTime() != null ? itinerary.getTotalTime() / 60 : 0,
                            itinerary.getTotalDistance() != null ? itinerary.getTotalDistance() : 0,
                            itinerary.getFare() != null && itinerary.getFare().getRegular() != null ? 
                            itinerary.getFare().getRegular().getTotalFare() : 0);
                    
                } else {
                    log.warn("구간 {} 상세 경로 조회 실패: {} -> {}", i + 1, from.getName(), to.getName());
                    return null;
                }
            }
            
            // 전체 요약 정보 생성
            SkTransitWaypointResponseDto.RouteSummary summary = SkTransitWaypointResponseDto.RouteSummary.builder()
                    .totalTime(totalTime)
                    .totalDistance(totalDistance)
                    .totalFare(totalFare)
                    .totalWalkTime(totalWalkTime)
                    .totalTransferCount(totalTransferCount)
                    .segmentCount(segments.size())
                    .waypointNames(waypointNames)
                    .build();
            
            log.info("경유지 상세 경로 조회 완료: 총 {}분, {}m, {}원, {}회 환승", 
                    totalTime / 60, totalDistance, totalFare, totalTransferCount);
            
            return SkTransitWaypointResponseDto.builder()
                    .segments(segments)
                    .summary(summary)
                    .build();
                    
        } catch (Exception e) {
            log.error("경유지 상세 경로 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 상세 경로 API 응답의 DetailedLeg를 경유지 응답 DTO의 Leg로 변환합니다.
     * 모드별로 다른 DTO 구조를 사용합니다.
     */
    private List<Object> convertDetailedLegs(List<SkTransitDetailedResponseDto.DetailedLeg> originalLegs) {
        if (originalLegs == null) {
            return new ArrayList<>();
        }
        
        return originalLegs.stream()
                .map(leg -> {
                    if ("WALK".equals(leg.getMode())) {
                        return convertToWalkLeg(leg);
                    } else if ("SUBWAY".equals(leg.getMode())) {
                        return convertToSubwayLeg(leg);
                    } else if ("BUS".equals(leg.getMode())) {
                        return convertToBusLeg(leg);
                    } else {
                        // 기존 방식으로 변환 (호환성)
                        return convertToLeg(leg);
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * WALK 모드용 Leg로 변환
     */
    private SkTransitWaypointResponseDto.WalkLeg convertToWalkLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        List<SkTransitWaypointResponseDto.WalkStep> steps = convertToWalkSteps(leg.getSteps());
        
        return SkTransitWaypointResponseDto.WalkLeg.builder()
                .mode("WALK")
                .sectionTime(leg.getSectionTime())
                .distance(leg.getDistance())
                .start(convertDetailedLocation(leg.getStart()))
                .end(convertDetailedLocation(leg.getEnd()))
                .steps(steps)
                .passShape(convertPassShape(leg.getPassShape()))
                .build();
    }
    
    /**
     * SUBWAY 모드용 Leg로 변환
     */
    private SkTransitWaypointResponseDto.SubwayLeg convertToSubwayLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        List<SkTransitWaypointResponseDto.Station> stationList = new ArrayList<>();
        
        if (leg.getPassStopList() != null && leg.getPassStopList().getStationList() != null) {
            stationList = leg.getPassStopList().getStationList().stream()
                    .map(stop -> SkTransitWaypointResponseDto.Station.builder()
                            .index(stop.getIndex())
                            .stationName(stop.getStationName())
                            .lon(stop.getLon())
                            .lat(stop.getLat())
                            .stationId(stop.getStationID())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return SkTransitWaypointResponseDto.SubwayLeg.builder()
                .mode("SUBWAY")
                .routeColor(leg.getRouteColor())
                .sectionTime(leg.getSectionTime())
                .route(leg.getRoute())
                .routeId(leg.getRouteId())
                .distance(leg.getDistance())
                .service(leg.getService())
                .start(convertDetailedLocation(leg.getStart()))
                .passStopList(SkTransitWaypointResponseDto.SubwayPassStopList.builder()
                        .stationList(stationList)
                        .build())
                .end(convertDetailedLocation(leg.getEnd()))
                .type(leg.getType())
                .passShape(convertPassShape(leg.getPassShape()))
                .build();
    }
    
    /**
     * BUS 모드용 Leg로 변환
     */
    private SkTransitWaypointResponseDto.BusLeg convertToBusLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        List<SkTransitWaypointResponseDto.Station> stationList = new ArrayList<>();
        
        if (leg.getPassStopList() != null && leg.getPassStopList().getStationList() != null) {
            stationList = leg.getPassStopList().getStationList().stream()
                    .map(stop -> SkTransitWaypointResponseDto.Station.builder()
                            .index(stop.getIndex())
                            .stationName(stop.getStationName())
                            .lon(stop.getLon())
                            .lat(stop.getLat())
                            .stationId(stop.getStationID())
                            .build())
                    .collect(Collectors.toList());
        }
        
        List<SkTransitWaypointResponseDto.Lane> lanes = new ArrayList<>();
        if (leg.getRouteColor() != null || leg.getRoute() != null || leg.getRouteId() != null) {
            lanes.add(SkTransitWaypointResponseDto.Lane.builder()
                    .routeColor(leg.getRouteColor())
                    .route(leg.getRoute())
                    .routeId(leg.getRouteId())
                    .service(leg.getService())
                    .type(leg.getType())
                    .build());
        }
        
        return SkTransitWaypointResponseDto.BusLeg.builder()
                .routeColor(leg.getRouteColor())
                .distance(leg.getDistance())
                .start(convertDetailedLocation(leg.getStart()))
                .lane(lanes)
                .type(leg.getType())
                .mode("BUS")
                .sectionTime(leg.getSectionTime())
                .route(leg.getRoute())
                .routeId(leg.getRouteId())
                .service(leg.getService())
                .passStopList(SkTransitWaypointResponseDto.BusPassStopList.builder()
                        .stationList(stationList)
                        .build())
                .end(convertDetailedLocation(leg.getEnd()))
                .passShape(convertPassShape(leg.getPassShape()))
                .build();
    }
    
    /**
     * 기존 방식의 Leg로 변환 (호환성)
     */
    private SkTransitWaypointResponseDto.Leg convertToLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        List<SkTransitWaypointResponseDto.Step> steps;
        if ("SUBWAY".equals(leg.getMode()) && leg.getPassStopList() != null && leg.getPassStopList().getStationList() != null && !leg.getPassStopList().getStationList().isEmpty()) {
            // SUBWAY: 모든 정류장 정보를 steps로 변환
            steps = leg.getPassStopList().getStationList().stream()
                    .map(stop -> SkTransitWaypointResponseDto.Step.builder()
                            .name(stop.getStationName())
                            .lon(stop.getLon())
                            .lat(stop.getLat())
                            .stationId(stop.getStationID())
                            .sequence(stop.getIndex())
                            .build())
                    .collect(Collectors.toList());
        } else if ("BUS".equals(leg.getMode()) && leg.getPassStopList() != null && leg.getPassStopList().getStationList() != null && !leg.getPassStopList().getStationList().isEmpty()) {
            // BUS: 모든 정류장 정보를 steps로 변환
            steps = leg.getPassStopList().getStationList().stream()
                    .map(stop -> SkTransitWaypointResponseDto.Step.builder()
                            .name(stop.getStationName())
                            .lon(stop.getLon())
                            .lat(stop.getLat())
                            .stationId(stop.getStationID())
                            .sequence(stop.getIndex())
                            .build())
                    .collect(Collectors.toList());
        } else {
            // WALK 등 기존 방식
            steps = convertSteps(leg.getSteps());
        }
        return SkTransitWaypointResponseDto.Leg.builder()
                .mode(leg.getMode())
                .sectionTime(leg.getSectionTime())
                .distance(leg.getDistance())
                .start(convertDetailedLocation(leg.getStart()))
                .end(convertDetailedLocation(leg.getEnd()))
                .routeColor(leg.getRouteColor())
                .route(leg.getRoute())
                .routeId(leg.getRouteId())
                .type(leg.getType())
                .service(leg.getService())
                .steps(steps)
                .passStopList(convertPassStopList(leg.getPassStopList()))
                .passShape(convertPassShape(leg.getPassShape()))
                .build();
    }
    
    /**
     * WALK 모드용 Step으로 변환
     */
    private List<SkTransitWaypointResponseDto.WalkStep> convertToWalkSteps(List<SkTransitDetailedResponseDto.Step> originalSteps) {
        if (originalSteps == null) {
            return new ArrayList<>();
        }
        
        return originalSteps.stream()
                .map(step -> SkTransitWaypointResponseDto.WalkStep.builder()
                        .streetName(step.getStreetName())
                        .distance(step.getDistance())
                        .description(step.getDescription())
                        .linestring(step.getLinestring())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 상세 경로 API 응답의 Location을 경유지 응답 DTO의 Location으로 변환합니다.
     */
    private SkTransitWaypointResponseDto.Location convertDetailedLocation(SkTransitDetailedResponseDto.Location originalLocation) {
        if (originalLocation == null) {
            return null;
        }
        
        return SkTransitWaypointResponseDto.Location.builder()
                .name(originalLocation.getName())
                .lon(originalLocation.getLon())
                .lat(originalLocation.getLat())
                .build();
    }

    /**
     * 상세 경로 API 응답의 Step을 경유지 응답 DTO의 Step으로 변환합니다.
     */
    private List<SkTransitWaypointResponseDto.Step> convertSteps(List<SkTransitDetailedResponseDto.Step> originalSteps) {
        if (originalSteps == null) {
            return new ArrayList<>();
        }
        
        return originalSteps.stream()
                .map(step -> SkTransitWaypointResponseDto.Step.builder()
                        .streetName(step.getStreetName())
                        .distance(step.getDistance())
                        .description(step.getDescription())
                        .linestring(step.getLinestring())
                        .sectionTime(step.getSectionTime())
                        .lon(step.getLon())
                        .lat(step.getLat())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 상세 경로 API 응답의 PassStopList를 경유지 응답 DTO의 PassStopList로 변환합니다.
     */
    private SkTransitWaypointResponseDto.PassStopList convertPassStopList(SkTransitDetailedResponseDto.PassStopList originalPassStopList) {
        if (originalPassStopList == null) {
            return null;
        }
        
        return SkTransitWaypointResponseDto.PassStopList.builder()
                .stations(convertStops(originalPassStopList.getStationList()))
                .busStops(convertStops(originalPassStopList.getBusStops()))
                .build();
    }

    /**
     * 상세 경로 API 응답의 Stop을 경유지 응답 DTO의 Stop으로 변환합니다.
     */
    private List<SkTransitWaypointResponseDto.Stop> convertStops(List<SkTransitDetailedResponseDto.Stop> originalStops) {
        if (originalStops == null) {
            return new ArrayList<>();
        }
        
        return originalStops.stream()
                .map(stop -> SkTransitWaypointResponseDto.Stop.builder()
                        .name(stop.getStationName())
                        .lon(stop.getLon())
                        .lat(stop.getLat())
                        .stationId(stop.getStationID())
                        .sequence(stop.getIndex())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 상세 경로 API 응답의 PassShape를 경유지 응답 DTO의 PassShape로 변환합니다.
     */
    private SkTransitWaypointResponseDto.PassShape convertPassShape(SkTransitDetailedResponseDto.PassShape originalPassShape) {
        if (originalPassShape == null) {
            return null;
        }
        
        // lineString을 생성 (좌표들을 연결)
        List<SkTransitDetailedResponseDto.Coordinate> coordinates = originalPassShape.getCoordinates();
        if (coordinates != null && !coordinates.isEmpty()) {
            String lineString = coordinates.stream()
                    .map(coord -> coord.getLon() + " " + coord.getLat())
                    .collect(Collectors.joining(","));
            
            return SkTransitWaypointResponseDto.PassShape.builder()
                    .lineString(lineString)
                    .build();
        }
        
        return SkTransitWaypointResponseDto.PassShape.builder()
                .lineString("")
                .build();
    }

    /**
     * 상세 경로 API 응답의 Coordinate를 경유지 응답 DTO의 Coordinate로 변환합니다.
     */
    private List<SkTransitWaypointResponseDto.Coordinate> convertCoordinates(List<SkTransitDetailedResponseDto.Coordinate> originalCoordinates) {
        if (originalCoordinates == null) {
            return new ArrayList<>();
        }
        
        return originalCoordinates.stream()
                .map(coord -> SkTransitWaypointResponseDto.Coordinate.builder()
                        .lon(coord.getLon())
                        .lat(coord.getLat())
                        .build())
                .collect(Collectors.toList());
    }
} 