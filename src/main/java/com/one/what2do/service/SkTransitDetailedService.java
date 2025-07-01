package com.one.what2do.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.what2do.constants.ApiConstants;
import com.one.what2do.dto.detailed.SkTransitDetailedRequestDto;
import com.one.what2do.dto.detailed.SkTransitDetailedResponseDto;
import com.one.what2do.dto.pedestrian.SkPedestrianResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * SK 교통 API 상세 경로 서비스
 * 모든 정거장 정보를 포함하는 버전
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkTransitDetailedService {

    private final WebClient webClient;
    private final SkPedestrianService skPedestrianService;

    /**
     * SK 교통 API를 통해 상세 경로를 조회합니다.
     * 모든 정거장 정보를 포함합니다.
     * 대중교통 API 응답이 없을 경우 보행 경로 API를 호출합니다.
     * 
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @return 상세 경로 정보 (모든 정거장 포함)
     */
    public SkTransitDetailedResponseDto getDetailedTransitRoute(String startX, String startY, String endX, String endY) {
        try {
            // 요청 DTO 생성
            SkTransitDetailedRequestDto requestDto = SkTransitDetailedRequestDto.builder()
                    .startX(startX)
                    .startY(startY)
                    .endX(endX)
                    .endY(endY)
                    .lang(0)
                    .format("json")
                    .count(10)
                    .includeDetailedStops(true)
                    .build();

            log.info("SK 교통 API 상세 경로 요청 전송: {}", ApiConstants.getSkTransitApiUrl());
            log.info("요청 데이터: {} -> {}", 
                    String.format("(%s, %s)", startX, startY), 
                    String.format("(%s, %s)", endX, endY));

            // WebClient를 사용하여 SK 교통 API에 요청 전송 (원본 JSON 로그 출력)
            String rawJson = webClient.post()
                    .uri(ApiConstants.getSkTransitApiUrl())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("appKey", ApiConstants.getSkAppKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // log.info("SK API 원본 응답: {}", rawJson);

            // 이후 필요하다면 Jackson ObjectMapper로 DTO로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            SkTransitDetailedResponseDto response = objectMapper.readValue(rawJson, SkTransitDetailedResponseDto.class);

            if (response != null && response.getMetaData() != null && 
                response.getMetaData().getPlan() != null && 
                response.getMetaData().getPlan().getItineraries() != null &&
                !response.getMetaData().getPlan().getItineraries().isEmpty()) {
                
                // log.info("SK 교통 API 상세 경로 응답 성공");
                // log.info("전체 경로 개수: {}", response.getMetaData().getPlan().getItineraries().size());
                
                // SK API 응답 그대로 사용 (정거장 정보 포함)
                
                // 최단 시간 경로만 추출
                SkTransitDetailedResponseDto fastestRoute = getFastestDetailedRoute(response);
                if (fastestRoute != null) {
                    // log.info("최단 시간 상세 경로 추출 완료: {}분", 
                    //         fastestRoute.getMetaData().getPlan().getItineraries().get(0).getTotalTime() / 60);
                    return fastestRoute;
                }
                return response;
            } else {
                log.warn("SK 교통 API 상세 경로 응답이 없습니다. 보행 경로 API를 시도합니다.");
                return getPedestrianRouteAsDetailedTransit(startX, startY, endX, endY);
            }

        } catch (WebClientResponseException e) {
            log.error("SK 교통 API HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            log.warn("보행 경로 API를 시도합니다.");
            return getPedestrianRouteAsDetailedTransit(startX, startY, endX, endY);
        } catch (Exception e) {
            log.error("SK 교통 API 상세 경로 서비스 오류: {}", e.getMessage(), e);
            log.warn("보행 경로 API를 시도합니다.");
            return getPedestrianRouteAsDetailedTransit(startX, startY, endX, endY);
        }
    }

    /**
     * SUBWAY와 BUS 모드에 대해 기본 정류장 정보를 추가합니다.
     * 
     * @param response SK 교통 API 응답
     */
    private void addDefaultStopInfo(SkTransitDetailedResponseDto response) {
        if (response.getMetaData() != null && response.getMetaData().getPlan() != null &&
            response.getMetaData().getPlan().getItineraries() != null) {
            
            for (SkTransitDetailedResponseDto.DetailedItinerary itinerary : response.getMetaData().getPlan().getItineraries()) {
                if (itinerary.getLegs() != null) {
                    for (SkTransitDetailedResponseDto.DetailedLeg leg : itinerary.getLegs()) {
                        // SK API에서 정거장 정보가 없는 경우에만 기본 정거장 정보 추가
                        if (("SUBWAY".equals(leg.getMode()) || "BUS".equals(leg.getMode())) && 
                            (leg.getPassStopList() == null || 
                             (leg.getPassStopList().getStationList() == null && leg.getPassStopList().getBusStops() == null) ||
                             (leg.getPassStopList().getStationList() != null && leg.getPassStopList().getStationList().isEmpty() && 
                              leg.getPassStopList().getBusStops() != null && leg.getPassStopList().getBusStops().isEmpty()))) {
                            log.debug("기본 정류장 정보 추가: {} 모드 (SK API에서 정거장 정보가 없는 경우)", leg.getMode());
                            // 기본 정류장 정보 생성
                            List<SkTransitDetailedResponseDto.Stop> stops = new ArrayList<>();
                            
                            // 출발 정류장
                            if (leg.getStart() != null) {
                                stops.add(SkTransitDetailedResponseDto.Stop.builder()
                                        .stationName(leg.getStart().getName())
                                        .lon(leg.getStart().getLon())
                                        .lat(leg.getStart().getLat())
                                        .stationID(leg.getRouteId())
                                        .index(0)
                                        .build());
                            }
                            
                            // 도착 정류장
                            if (leg.getEnd() != null) {
                                stops.add(SkTransitDetailedResponseDto.Stop.builder()
                                        .stationName(leg.getEnd().getName())
                                        .lon(leg.getEnd().getLon())
                                        .lat(leg.getEnd().getLat())
                                        .stationID(leg.getRouteId())
                                        .index(1)
                                        .build());
                            }
                            
                            // PassStopList 생성
                            SkTransitDetailedResponseDto.PassStopList passStopList = SkTransitDetailedResponseDto.PassStopList.builder()
                                    .stationList("SUBWAY".equals(leg.getMode()) ? stops : new ArrayList<>())
                                    .busStops("BUS".equals(leg.getMode()) ? stops : new ArrayList<>())
                                    .build();
                            
                            leg.setPassStopList(passStopList);
                        }
                    }
                }
            }
        }
    }

    /**
     * 보행 경로를 상세 교통 경로 형식으로 변환합니다.
     * 
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @return 상세 교통 경로 형식의 보행 경로
     */
    private SkTransitDetailedResponseDto getPedestrianRouteAsDetailedTransit(String startX, String startY, String endX, String endY) {
        try {
            log.info("보행 경로 API 호출: {} -> {}", 
                    String.format("(%s, %s)", startX, startY), 
                    String.format("(%s, %s)", endX, endY));
            
            // 보행 경로 조회
            SkPedestrianResponseDto pedestrianResponse = skPedestrianService.getPedestrianRoute(
                startX, startY, endX, endY, "출발", "도착"
            );
            
            if (pedestrianResponse == null || pedestrianResponse.getFeatures() == null) {
                log.warn("보행 경로 조회 실패");
                return null;
            }
            
            // 보행 경로를 상세 교통 경로 형식으로 변환
            Integer totalDistance = skPedestrianService.getTotalDistance(pedestrianResponse);
            Integer totalTime = skPedestrianService.getTotalTime(pedestrianResponse);
            
            log.info("보행 경로 정보 - 총 거리: {}m, 총 시간: {}초", totalDistance, totalTime);
            
            // Location 객체 생성
            SkTransitDetailedResponseDto.Location startLocation = SkTransitDetailedResponseDto.Location.builder()
                    .name("출발지")
                    .lon(startX)
                    .lat(startY)
                    .build();
            
            SkTransitDetailedResponseDto.Location endLocation = SkTransitDetailedResponseDto.Location.builder()
                    .name("도착지")
                    .lon(endX)
                    .lat(endY)
                    .build();
            
            // Steps 생성 - 실제 보행 경로의 좌표들을 추출
            List<SkTransitDetailedResponseDto.Step> steps = new ArrayList<>();
            StringBuilder linestringBuilder = new StringBuilder();
            
            for (SkPedestrianResponseDto.Feature feature : pedestrianResponse.getFeatures()) {
                if (feature.getGeometry() != null && feature.getGeometry().getCoordinates() != null) {
                    List<List<Double>> coordinates = feature.getGeometry().getCoordinates();
                    
                    for (int i = 0; i < coordinates.size(); i++) {
                        List<Double> coord = coordinates.get(i);
                        if (coord.size() >= 2) {
                            double lon = coord.get(0);
                            double lat = coord.get(1);
                            
                            // linestring에 좌표 추가
                            if (linestringBuilder.length() > 0) {
                                linestringBuilder.append(" ");
                            }
                            linestringBuilder.append(String.format("%.6f,%.6f", lon, lat));
                            
                            // Step 생성 (중요한 지점들만)
                            if (i == 0 || i == coordinates.size() - 1 || i % 10 == 0) { // 시작, 끝, 10개마다
                                SkTransitDetailedResponseDto.Step step = SkTransitDetailedResponseDto.Step.builder()
                                        .streetName("보행자도로")
                                        .distance(feature.getProperties() != null ? feature.getProperties().getDistance() : 0)
                                        .description(feature.getProperties() != null ? 
                                            feature.getProperties().getDescription() : "보행자도로를 따라 이동")
                                        .linestring(linestringBuilder.toString())
                                        .build();
                                steps.add(step);
                            }
                        }
                    }
                }
            }
            
            // Step이 비어있으면 기본 Step 생성
            if (steps.isEmpty()) {
                steps.add(SkTransitDetailedResponseDto.Step.builder()
                        .streetName("보행자도로")
                        .distance(totalDistance)
                        .description("보행자도로를 따라 " + totalDistance + "m 이동")
                        .linestring(String.format("%s,%s %s,%s", startX, startY, endX, endY))
                        .build());
            }
            
            // DetailedLeg 객체 생성
            SkTransitDetailedResponseDto.DetailedLeg leg = SkTransitDetailedResponseDto.DetailedLeg.builder()
                    .mode("WALK")
                    .sectionTime(totalTime)
                    .distance(totalDistance)
                    .start(startLocation)
                    .end(endLocation)
                    .steps(steps)
                    .build();
            
            // DetailedItinerary 객체 생성
            SkTransitDetailedResponseDto.DetailedItinerary itinerary = SkTransitDetailedResponseDto.DetailedItinerary.builder()
                    .totalTime(totalTime)
                    .totalDistance(totalDistance)
                    .totalWalkTime(totalTime)
                    .transferCount(0)
                    .totalWalkDistance(totalDistance)
                    .pathType(1) // 보행 경로
                    .legs(List.of(leg))
                    .build();
            
            // Plan 객체 생성
            SkTransitDetailedResponseDto.Plan plan = SkTransitDetailedResponseDto.Plan.builder()
                    .itineraries(List.of(itinerary))
                    .build();
            
            // RequestParameters 객체 생성
            SkTransitDetailedResponseDto.RequestParameters requestParameters = SkTransitDetailedResponseDto.RequestParameters.builder()
                    .startX(startX)
                    .startY(startY)
                    .endX(endX)
                    .endY(endY)
                    .locale("ko")
                    .build();
            
            // MetaData 객체 생성
            SkTransitDetailedResponseDto.MetaData metaData = SkTransitDetailedResponseDto.MetaData.builder()
                    .plan(plan)
                    .requestParameters(requestParameters)
                    .build();
            
            // 최종 응답 생성
            SkTransitDetailedResponseDto response = SkTransitDetailedResponseDto.builder()
                    .metaData(metaData)
                    .build();
            
            log.info("보행 경로를 상세 교통 경로 형식으로 변환 완료: {}m, {}초", totalDistance, totalTime);
            return response;
            
        } catch (Exception e) {
            log.error("보행 경로 변환 중 오류: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 시간이 가장 짧은 상세 경로를 반환합니다.
     * 
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @return 최단 시간 상세 경로 정보
     */
    public SkTransitDetailedResponseDto getFastestDetailedTimeRoute(String startX, String startY, String endX, String endY) {
        try {
            // 요청 DTO 생성
            SkTransitDetailedRequestDto requestDto = SkTransitDetailedRequestDto.builder()
                    .startX(startX)
                    .startY(startY)
                    .endX(endX)
                    .endY(endY)
                    .lang(0)
                    .format("json")
                    .count(10)
                    .includeDetailedStops(true)
                    .build();

            log.info("SK 교통 API 상세 경로 요청 전송 (최단 시간): {}", ApiConstants.getSkTransitApiUrl());
            log.info("요청 데이터: {} -> {}", 
                    String.format("(%s, %s)", startX, startY), 
                    String.format("(%s, %s)", endX, endY));

            // WebClient를 사용하여 SK 교통 API에 요청 전송
            SkTransitDetailedResponseDto response = webClient.post()
                    .uri(ApiConstants.getSkTransitApiUrl())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("appKey", ApiConstants.getSkAppKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(SkTransitDetailedResponseDto.class)
                    .block();

            if (response != null) {
                //                log.info("SK 교통 API 상세 경로 응답 성공");
                if (response.getMetaData() != null && response.getMetaData().getPlan() != null) {
                    //                    log.info("전체 경로 개수: {}", response.getMetaData().getPlan().getItineraries().size());
                    // 최단 시간 경로만 추출
                    SkTransitDetailedResponseDto fastestRoute = getFastestDetailedRoute(response);
                    if (fastestRoute != null) {
                        //                        log.info("최단 시간 상세 경로 추출 완료: {}분", 
                        //                                fastestRoute.getMetaData().getPlan().getItineraries().get(0).getTotalTime() / 60);
                        return fastestRoute;
                    }
                }
                return response;
            } else {
                log.warn("SK 교통 API 상세 경로 응답이 null입니다");
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("SK 교통 API HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("SK 교통 API 상세 경로 서비스 오류: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * legs 개수가 가장 적은 상세 경로를 반환합니다.
     * 
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @return 최소 legs 상세 경로 정보
     */
    public SkTransitDetailedResponseDto getMinLegsDetailedRoute(String startX, String startY, String endX, String endY) {
        try {
            // 요청 DTO 생성
            SkTransitDetailedRequestDto requestDto = SkTransitDetailedRequestDto.builder()
                    .startX(startX)
                    .startY(startY)
                    .endX(endX)
                    .endY(endY)
                    .lang(0)
                    .format("json")
                    .count(10)
                    .includeDetailedStops(true)
                    .build();

            log.info("SK 교통 API 상세 경로 요청 전송 (최소 legs): {}", ApiConstants.getSkTransitApiUrl());
            log.info("요청 데이터: {} -> {}", 
                    String.format("(%s, %s)", startX, startY), 
                    String.format("(%s, %s)", endX, endY));

            // WebClient를 사용하여 SK 교통 API에 요청 전송
            SkTransitDetailedResponseDto response = webClient.post()
                    .uri(ApiConstants.getSkTransitApiUrl())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("appKey", ApiConstants.getSkAppKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(SkTransitDetailedResponseDto.class)
                    .block();

            if (response != null) {
                //                log.info("SK 교통 API 상세 경로 응답 성공");
                if (response.getMetaData() != null && response.getMetaData().getPlan() != null) {
                    //                    log.info("전체 경로 개수: {}", response.getMetaData().getPlan().getItineraries().size());
                    // 최소 legs 경로만 추출
                    SkTransitDetailedResponseDto minLegsRoute = getMinLegsDetailedRouteFromResponse(response);
                    if (minLegsRoute != null) {
                        //                        int legsCount = minLegsRoute.getMetaData().getPlan().getItineraries().get(0).getLegs().size();
                        //                        log.info("최소 legs 상세 경로 추출 완료: {}개 legs", legsCount);
                        return minLegsRoute;
                    }
                }
                return response;
            } else {
                log.warn("SK 교통 API 상세 경로 응답이 null입니다");
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("SK 교통 API HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("SK 교통 API 상세 경로 서비스 오류: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 응답에서 최단 시간 상세 경로만 추출하여 새로운 응답을 생성합니다.
     * 
     * @param response 원본 SK 교통 API 상세 응답
     * @return 최단 시간 상세 경로만 포함된 응답
     */
    private SkTransitDetailedResponseDto getFastestDetailedRoute(SkTransitDetailedResponseDto response) {
        if (response == null || response.getMetaData() == null || 
            response.getMetaData().getPlan() == null || 
            response.getMetaData().getPlan().getItineraries() == null ||
            response.getMetaData().getPlan().getItineraries().isEmpty()) {
            return null;
        }

        List<SkTransitDetailedResponseDto.DetailedItinerary> itineraries = response.getMetaData().getPlan().getItineraries();
        
        // 최단 시간 경로 찾기
        SkTransitDetailedResponseDto.DetailedItinerary fastestItinerary = itineraries.stream()
                .filter(itinerary -> itinerary.getTotalTime() != null)
                .min((a, b) -> Integer.compare(a.getTotalTime(), b.getTotalTime()))
                .orElse(null);

        if (fastestItinerary == null) {
            return null;
        }

        // 최단 시간 상세 경로만 포함된 새로운 응답 생성
        SkTransitDetailedResponseDto.Plan plan = SkTransitDetailedResponseDto.Plan.builder()
                .itineraries(List.of(fastestItinerary))
                .build();

        SkTransitDetailedResponseDto.MetaData metaData = SkTransitDetailedResponseDto.MetaData.builder()
                .plan(plan)
                .requestParameters(response.getMetaData().getRequestParameters())
                .build();

        return SkTransitDetailedResponseDto.builder()
                .metaData(metaData)
                .build();
    }

    /**
     * 응답에서 legs 개수가 가장 적은 상세 경로만 추출하여 새로운 응답을 생성합니다.
     * 
     * @param response 원본 SK 교통 API 상세 응답
     * @return 최소 legs 상세 경로만 포함된 응답
     */
    private SkTransitDetailedResponseDto getMinLegsDetailedRouteFromResponse(SkTransitDetailedResponseDto response) {
        if (response == null || response.getMetaData() == null || 
            response.getMetaData().getPlan() == null || 
            response.getMetaData().getPlan().getItineraries() == null ||
            response.getMetaData().getPlan().getItineraries().isEmpty()) {
            return null;
        }

        List<SkTransitDetailedResponseDto.DetailedItinerary> itineraries = response.getMetaData().getPlan().getItineraries();
        
        // legs 개수가 가장 적은 경로 찾기
        SkTransitDetailedResponseDto.DetailedItinerary minLegsItinerary = itineraries.stream()
                .filter(itinerary -> itinerary.getLegs() != null)
                .min((a, b) -> Integer.compare(a.getLegs().size(), b.getLegs().size()))
                .orElse(null);

        if (minLegsItinerary == null) {
            return null;
        }

        // 최소 legs 상세 경로만 포함된 새로운 응답 생성
        SkTransitDetailedResponseDto.Plan plan = SkTransitDetailedResponseDto.Plan.builder()
                .itineraries(List.of(minLegsItinerary))
                .build();

        SkTransitDetailedResponseDto.MetaData metaData = SkTransitDetailedResponseDto.MetaData.builder()
                .plan(plan)
                .requestParameters(response.getMetaData().getRequestParameters())
                .build();

        return SkTransitDetailedResponseDto.builder()
                .metaData(metaData)
                .build();
    }
} 