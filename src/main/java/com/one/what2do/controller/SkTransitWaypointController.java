package com.one.what2do.controller;

import com.one.what2do.dto.waypoint.SkTransitWaypointRequestDto;
import com.one.what2do.dto.waypoint.SkTransitWaypointResponseDto;
import com.one.what2do.service.SkTransitWaypointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * SK 교통 API 경유지 경로 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/sk-transit-waypoint")
@RequiredArgsConstructor
public class SkTransitWaypointController {

    private final SkTransitWaypointService skTransitWaypointService;

    /**
     * 경유지 경로 조회 엔드포인트
     * 
     * @param request 경유지 요청 정보
     * @return 경유지 경로 정보
     */
    @PostMapping("/route")
    public ResponseEntity<SkTransitWaypointResponseDto> getWaypointRoute(@RequestBody SkTransitWaypointRequestDto request) {
        log.info("경유지 경로 조회 요청: {}개 경유지", 
                request.getWaypoints() != null ? request.getWaypoints().size() : 0);
        
        SkTransitWaypointResponseDto response = skTransitWaypointService.getWaypointRoute(request);
        
        if (response != null) {
            log.info("경유지 경로 조회 성공: 총 {}분, {}m, {}원", 
                    response.getSummary().getTotalTime() / 60,
                    response.getSummary().getTotalDistance(),
                    response.getSummary().getTotalFare());
            return ResponseEntity.ok(response);
        } else {
            log.warn("경유지 경로 조회 실패");
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * 예시 경유지 경로 테스트 (다솜아이어린이집 -> 충무아트센터 -> 신당 계류관)
     * 
     * @return 경유지 경로 정보
     */
    @GetMapping("/test/example")
    public ResponseEntity<SkTransitWaypointResponseDto> testExampleWaypointRoute() {
        log.info("예시 경유지 경로 테스트: 다솜아이어린이집 -> 충무아트센터 -> 신당 계류관");
        
        // 예시 경유지 설정
        List<SkTransitWaypointRequestDto.Waypoint> waypoints = Arrays.asList(
                SkTransitWaypointRequestDto.Waypoint.builder()
                        .name("다솜아이어린이집")
                        .lon("127.05760765007315")
                        .lat("37.65036532946899")
                        .sequence(1)
                        .build(),
                SkTransitWaypointRequestDto.Waypoint.builder()
                        .name("충무아트센터")
                        .lon("127.01424474974938")
                        .lat("37.56533637746254")
                        .sequence(2)
                        .build(),
                SkTransitWaypointRequestDto.Waypoint.builder()
                        .name("신당 계류관")
                        .lon("127.020197186069")
                        .lat("37.5660857686301")
                        .sequence(3)
                        .build()
        );
        
        SkTransitWaypointRequestDto request = SkTransitWaypointRequestDto.builder()
                .waypoints(waypoints)
                .lang(0)
                .format("json")
                .count(10)
                .includeDetailedStops(false)
                .routeType("fastest")  // 최단 시간 경로
                .build();
        
        SkTransitWaypointResponseDto response = skTransitWaypointService.getWaypointRoute(request);
        
        if (response != null) {
            log.info("예시 경유지 경로 테스트 성공: 총 {}분, {}m, {}원", 
                    response.getSummary().getTotalTime() / 60,
                    response.getSummary().getTotalDistance(),
                    response.getSummary().getTotalFare());
            return ResponseEntity.ok(response);
        } else {
            log.warn("예시 경유지 경로 테스트 실패");
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * 최소 환승 경유지 경로 테스트
     * 
     * @return 경유지 경로 정보 (최소 환승)
     */
    @GetMapping("/test/example/min-legs")
    public ResponseEntity<SkTransitWaypointResponseDto> testExampleMinLegsWaypointRoute() {
        log.info("예시 경유지 경로 테스트 (최소 환승): 다솜아이어린이집 -> 충무아트센터 -> 신당 계류관");
        
        // 예시 경유지 설정
        List<SkTransitWaypointRequestDto.Waypoint> waypoints = Arrays.asList(
                SkTransitWaypointRequestDto.Waypoint.builder()
                        .name("다솜아이어린이집")
                        .lon("127.05760765007315")
                        .lat("37.65036532946899")
                        .sequence(1)
                        .build(),
                SkTransitWaypointRequestDto.Waypoint.builder()
                        .name("충무아트센터")
                        .lon("127.01424474974938")
                        .lat("37.56533637746254")
                        .sequence(2)
                        .build(),
                SkTransitWaypointRequestDto.Waypoint.builder()
                        .name("신당 계류관")
                        .lon("127.020197186069")
                        .lat("37.5660857686301")
                        .sequence(3)
                        .build()
        );
        
        SkTransitWaypointRequestDto request = SkTransitWaypointRequestDto.builder()
                .waypoints(waypoints)
                .lang(0)
                .format("json")
                .count(10)
                .includeDetailedStops(false)
                .routeType("minLegs")  // 최소 환승 경로
                .build();
        
        SkTransitWaypointResponseDto response = skTransitWaypointService.getWaypointRoute(request);
        
        if (response != null) {
            log.info("예시 경유지 경로 테스트 (최소 환승) 성공: 총 {}분, {}m, {}원", 
                    response.getSummary().getTotalTime() / 60,
                    response.getSummary().getTotalDistance(),
                    response.getSummary().getTotalFare());
            return ResponseEntity.ok(response);
        } else {
            log.warn("예시 경유지 경로 테스트 (최소 환승) 실패");
            return ResponseEntity.noContent().build();
        }
    }
} 