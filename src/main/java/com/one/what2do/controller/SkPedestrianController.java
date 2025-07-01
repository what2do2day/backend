package com.one.what2do.controller;

import com.one.what2do.dto.pedestrian.SkPedestrianResponseDto;
import com.one.what2do.service.SkPedestrianService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sk-pedestrian")
@RequiredArgsConstructor
public class SkPedestrianController {

    private final SkPedestrianService skPedestrianService;

    /**
     * SK 보행자 경로 API 테스트 엔드포인트
     * 
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @param startName 출발지 이름 (선택)
     * @param endName 도착지 이름 (선택)
     * @return 보행자 경로 정보
     */
    @GetMapping("/test")
    public ResponseEntity<SkPedestrianResponseDto> testSkPedestrianApi(
            @RequestParam String startX,
            @RequestParam String startY,
            @RequestParam String endX,
            @RequestParam String endY,
            @RequestParam(required = false) String startName,
            @RequestParam(required = false) String endName) {
        
        log.info("SK 보행자 경로 API 테스트 요청: ({}, {}) -> ({}, {})", startX, startY, endX, endY);
        
        SkPedestrianResponseDto response = skPedestrianService.getPedestrianRoute(
            startX, startY, endX, endY, startName, endName
        );
        
        if (response != null) {
            log.info("SK 보행자 경로 API 테스트 성공");
            return ResponseEntity.ok(response);
        } else {
            log.warn("SK 보행자 경로 API 테스트 실패");
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * 거리 계산 테스트 엔드포인트
     * 
     * @param lat1 첫 번째 지점의 위도
     * @param lon1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lon2 두 번째 지점의 경도
     * @return 거리 정보
     */
    @GetMapping("/distance")
    public ResponseEntity<Object> calculateDistance(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2) {
        
        log.info("거리 계산 요청: ({}, {}) -> ({}, {})", lat1, lon1, lat2, lon2);
        
        double distance = skPedestrianService.calculateDistance(lat1, lon1, lat2, lon2);
        boolean isShort = skPedestrianService.isShortDistance(distance);
        
        return ResponseEntity.ok(new Object() {
            public final double distanceValue = distance;
            public final boolean isShortDistanceValue = isShort;
            public final String unit = "meters";
        });
    }

    /**
     * 짧은 거리 테스트용 엔드포인트 (노원역 -> 광화문역 근처)
     */
    @GetMapping("/test-short")
    public ResponseEntity<SkPedestrianResponseDto> testShortDistance() {
        // 노원역 근처 -> 광화문역 근처 (약 800m 거리)
        String startX = "126.92365493654832";
        String startY = "37.556770374096615";
        String endX = "126.92432158129688";
        String endY = "37.55279861528311";
        
        log.info("짧은 거리 보행자 경로 테스트: 노원역 근처 -> 광화문역 근처");
        
        SkPedestrianResponseDto response = skPedestrianService.getPedestrianRoute(
            startX, startY, endX, endY, "노원역", "광화문역"
        );
        
        if (response != null) {
            log.info("짧은 거리 보행자 경로 테스트 성공");
            return ResponseEntity.ok(response);
        } else {
            log.warn("짧은 거리 보행자 경로 테스트 실패");
            return ResponseEntity.noContent().build();
        }
    }
} 