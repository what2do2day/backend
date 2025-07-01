package com.one.what2do.controller;

import com.one.what2do.entities.Place;
import com.one.what2do.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    /**
     * 이름 또는 주소로 장소 검색
     * GET /api/places/search?keyword=검색어
     */
    @GetMapping("/search")
    public ResponseEntity<List<Place>> searchByNameOrAddress(@RequestParam String keyword) {
        try {
            List<Place> places = placeService.searchByNameOrAddress(keyword);
            return ResponseEntity.ok(places);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 위도와 경도로 정확히 일치하는 장소 검색
     * GET /api/places/search/coordinates?latitude=37.5665&longitude=126.9780
     */
    @GetMapping("/search/coordinates")
    public ResponseEntity<List<Place>> searchByCoordinates(
            @RequestParam String latitude,
            @RequestParam String longitude) {
        try {
            List<Place> places = placeService.searchByCoordinates(latitude, longitude);
            return ResponseEntity.ok(places);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 위도와 경도 범위로 장소 검색 (사각형 영역)
     * GET /api/places/search/range?minLat=37.5&maxLat=37.6&minLng=126.9&maxLng=127.0
     */
    @GetMapping("/search/range")
    public ResponseEntity<List<Place>> searchByCoordinateRange(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLng,
            @RequestParam double maxLng) {
        try {
            List<Place> places = placeService.searchByCoordinateRange(minLat, maxLat, minLng, maxLng);
            return ResponseEntity.ok(places);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 위도 범위로 장소 검색
     * GET /api/places/search/latitude?minLat=37.5&maxLat=37.6
     */
    @GetMapping("/search/latitude")
    public ResponseEntity<List<Place>> searchByLatitudeRange(
            @RequestParam double minLat,
            @RequestParam double maxLat) {
        try {
            List<Place> places = placeService.searchByLatitudeRange(minLat, maxLat);
            return ResponseEntity.ok(places);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 경도 범위로 장소 검색
     * GET /api/places/search/longitude?minLng=126.9&maxLng=127.0
     */
    @GetMapping("/search/longitude")
    public ResponseEntity<List<Place>> searchByLongitudeRange(
            @RequestParam double minLng,
            @RequestParam double maxLng) {
        try {
            List<Place> places = placeService.searchByLongitudeRange(minLng, maxLng);
            return ResponseEntity.ok(places);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 