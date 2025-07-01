package com.one.what2do.repository;

import com.one.what2do.entities.ItineraryPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryPlaceRepository extends JpaRepository<ItineraryPlace, ItineraryPlace.ItineraryPlaceId> {
    
    // itineraryId로 해당 여행의 모든 장소 조회 (순서대로)
    List<ItineraryPlace> findById_ItineraryIdOrderBySequence(Long itineraryId);
    
    // itineraryId로 해당 여행의 장소 개수 조회
    long countById_ItineraryId(Long itineraryId);
    
    // 특정 여행의 최대 sequence 값 조회
    @Query("SELECT MAX(ip.sequence) FROM ItineraryPlace ip WHERE ip.id.itineraryId = :itineraryId")
    Long findMaxSequenceByItineraryId(@Param("itineraryId") Long itineraryId);
    
    // 특정 여행의 특정 sequence 이후의 장소들 조회
    List<ItineraryPlace> findById_ItineraryIdAndSequenceGreaterThanOrderBySequence(Long itineraryId, Long sequence);
} 