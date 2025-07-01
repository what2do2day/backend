package com.one.what2do.service;

import com.one.what2do.entities.ItineraryPlace;
import com.one.what2do.repository.ItineraryPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItineraryPlaceService {

    private final ItineraryPlaceRepository itineraryPlaceRepository;

    /**
     * 여행에 장소 추가 (마지막 순서로)
     */
    @Transactional
    public ItineraryPlace addPlaceToItinerary(Long itineraryId, Long placeId) {
        // 현재 최대 sequence 값 조회
        Long maxSequence = itineraryPlaceRepository.findMaxSequenceByItineraryId(itineraryId);
        Long nextSequence = (maxSequence != null) ? maxSequence + 1 : 1L;
        
        ItineraryPlace itineraryPlace = ItineraryPlace.builder()
                .itineraryId(itineraryId)
                .placeId(placeId)
                .sequence(nextSequence)
                .build();
        
        return itineraryPlaceRepository.save(itineraryPlace);
    }

    /**
     * 여행에 장소 추가 (특정 순서로)
     */
    @Transactional
    public ItineraryPlace addPlaceToItineraryAtSequence(Long itineraryId, Long placeId, Long sequence) {
        // 해당 순서 이후의 장소들의 sequence를 1씩 증가
        List<ItineraryPlace> placesToUpdate = itineraryPlaceRepository
                .findById_ItineraryIdAndSequenceGreaterThanOrderBySequence(itineraryId, sequence - 1);
        
        for (ItineraryPlace place : placesToUpdate) {
            place.setSequence(place.getSequence() + 1);
            itineraryPlaceRepository.save(place);
        }
        
        ItineraryPlace itineraryPlace = ItineraryPlace.builder()
                .itineraryId(itineraryId)
                .placeId(placeId)
                .sequence(sequence)
                .build();
        
        return itineraryPlaceRepository.save(itineraryPlace);
    }

    /**
     * 여행의 모든 장소 조회 (순서대로)
     */
    public List<ItineraryPlace> getPlacesByItineraryId(Long itineraryId) {
        return itineraryPlaceRepository.findById_ItineraryIdOrderBySequence(itineraryId);
    }

    /**
     * 여행에서 장소 제거
     */
    @Transactional
    public void removePlaceFromItinerary(Long itineraryId, Long placeId) {
        ItineraryPlace.ItineraryPlaceId id = new ItineraryPlace.ItineraryPlaceId(itineraryId, placeId);
        itineraryPlaceRepository.deleteById(id);
        
        // 순서 재정렬
        reorderSequence(itineraryId);
    }

    /**
     * 여행의 장소 순서 변경
     */
    @Transactional
    public void reorderSequence(Long itineraryId) {
        List<ItineraryPlace> places = itineraryPlaceRepository.findById_ItineraryIdOrderBySequence(itineraryId);
        
        for (int i = 0; i < places.size(); i++) {
            ItineraryPlace place = places.get(i);
            place.setSequence((long) (i + 1));
            itineraryPlaceRepository.save(place);
        }
    }

    /**
     * 여행의 장소 개수 조회
     */
    public long getPlaceCountByItineraryId(Long itineraryId) {
        return itineraryPlaceRepository.countById_ItineraryId(itineraryId);
    }
} 