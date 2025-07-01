package com.one.what2do.service;

import com.one.what2do.entities.Place;
import com.one.what2do.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    /**
     * 이름 또는 주소로 장소 검색
     */
    public List<Place> searchByNameOrAddress(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색 키워드는 비어있을 수 없습니다.");
        }
        return placeRepository.findByNameOrAddressContainingIgnoreCase(keyword.trim());
    }

    /**
     * 위도와 경도로 정확히 일치하는 장소 검색
     */
    public List<Place> searchByCoordinates(String latitude, String longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("위도와 경도는 null일 수 없습니다.");
        }
        return placeRepository.findByLatitudeAndLongitude(latitude, longitude);
    }

    /**
     * 위도와 경도 범위로 장소 검색 (사각형 영역)
     */
    public List<Place> searchByCoordinateRange(double minLat, double maxLat, double minLng, double maxLng) {
        if (minLat > maxLat || minLng > maxLng) {
            throw new IllegalArgumentException("최소값은 최대값보다 작아야 합니다.");
        }
        return placeRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);
    }

    /**
     * 위도 범위로 장소 검색
     */
    public List<Place> searchByLatitudeRange(double minLat, double maxLat) {
        if (minLat > maxLat) {
            throw new IllegalArgumentException("최소 위도는 최대 위도보다 작아야 합니다.");
        }
        return placeRepository.findByLatitudeBetween(minLat, maxLat);
    }

    /**
     * 경도 범위로 장소 검색
     */
    public List<Place> searchByLongitudeRange(double minLng, double maxLng) {
        if (minLng > maxLng) {
            throw new IllegalArgumentException("최소 경도는 최대 경도보다 작아야 합니다.");
        }
        return placeRepository.findByLongitudeBetween(minLng, maxLng);
    }
} 