package com.one.what2do.controller;

import com.one.what2do.entities.ItineraryUser;
import com.one.what2do.entities.User;
import com.one.what2do.repository.ItineraryUserRepository;
import com.one.what2do.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final ItineraryUserRepository itineraryUserRepository;

    /**
     * itineraryId로 해당 여행의 모든 사용자 정보를 기반으로 장소 추천을 받아옵니다.
     * GET /api/predictions/recommendations/itinerary/{itineraryId}
     */
    @GetMapping("/recommendations/itinerary/{itineraryId}")
    public ResponseEntity<List<String>> getRecommendedPlacesByItinerary(@PathVariable Long itineraryId) {
        List<ItineraryUser> itineraryUsers = itineraryUserRepository.findById_ItineraryId(itineraryId);
        List<User> users = itineraryUsers.stream()
                .map(ItineraryUser::getUser)
                .collect(Collectors.toList());
        List<String> recommendedPlaces = predictionService.getRecommendedPlaces(users);
        return ResponseEntity.ok(recommendedPlaces);
    }
} 