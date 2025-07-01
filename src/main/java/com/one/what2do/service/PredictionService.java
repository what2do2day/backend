package com.one.what2do.service;

import com.one.what2do.constants.ApiConstants;
import com.one.what2do.dto.PredictionRequestDto;
import com.one.what2do.dto.PredictionResponseDto;
import com.one.what2do.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final WebClient webClient;

    /**
     * 사용자 리스트를 기반으로 장소 추천을 받아옵니다.
     * 
     * @param users 추천을 받을 사용자 리스트
     * @return 추천된 장소 이름 리스트
     */
    public List<String> getRecommendedPlaces(List<User> users) {
        try {
            // 사용자 정보를 UserInfo 리스트로 변환
            List<PredictionRequestDto.UserInfo> userInfos = users.stream()
                    .map(user -> PredictionRequestDto.UserInfo.builder()
                            .gender(user.getGender())
                            .age(user.getAge())
                            .address(user.getAddress())
                            .build())
                    .collect(Collectors.toList());

            // 요청 DTO 생성
            PredictionRequestDto requestDto = PredictionRequestDto.builder()
                    .users(userInfos)
                    .build();

            log.info("예측 서버에 요청 전송: {}", ApiConstants.PREDICTION_URL);
            log.info("요청 데이터: {}명의 사용자 정보", userInfos.size());

            // WebClient를 사용하여 외부 서버에 요청 전송
            PredictionResponseDto responseDto = webClient.post()
                    .uri(ApiConstants.PREDICTION_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(PredictionResponseDto.class)
                    .block(); // 동기적으로 결과를 기다림

            if (responseDto != null && responseDto.isSuccess()) {
                log.info("예측 서버 응답 성공: {}개의 장소 추천", 
                        responseDto.getPlaceNames() != null ? responseDto.getPlaceNames().size() : 0);
                return responseDto.getPlaceNames() != null ? responseDto.getPlaceNames() : Collections.emptyList();
            } else {
                log.warn("예측 서버 응답 실패: {}", responseDto != null ? responseDto.getMessage() : "응답이 null입니다");
                return Collections.emptyList();
            }

        } catch (WebClientResponseException e) {
            log.error("예측 서버 HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("예측 서비스 오류: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 단일 사용자 정보를 기반으로 장소 추천을 받아옵니다.
     * 
     * @param user 추천을 받을 사용자
     * @return 추천된 장소 이름 리스트
     */
    public List<String> getRecommendedPlaces(User user) {
        return getRecommendedPlaces(Collections.singletonList(user));
    }

    /**
     * 사용자 ID 리스트를 기반으로 장소 추천을 받아옵니다.
     * 
     * @param userIds 사용자 ID 리스트
     * @param userService 사용자 서비스
     * @return 추천된 장소 이름 리스트
     */
    public List<String> getRecommendedPlacesByUserIds(List<Long> userIds, UserService userService) {
        try {
            List<User> users = userIds.stream()
                    .map(userService::findById)
                    .collect(Collectors.toList());
            return getRecommendedPlaces(users);
        } catch (Exception e) {
            log.error("사용자 ID 리스트 {}로 추천 장소 조회 실패: {}", userIds, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 사용자 ID를 기반으로 장소 추천을 받아옵니다.
     * 
     * @param userId 사용자 ID
     * @param userService 사용자 서비스
     * @return 추천된 장소 이름 리스트
     */
    public List<String> getRecommendedPlacesByUserId(Long userId, UserService userService) {
        return getRecommendedPlacesByUserIds(Collections.singletonList(userId), userService);
    }
} 