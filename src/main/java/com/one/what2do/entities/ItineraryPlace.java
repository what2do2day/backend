package com.one.what2do.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "itinerary_places")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItineraryPlace extends BaseTimeEntity {

    @EmbeddedId
    private ItineraryPlaceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itineraryId")
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("placeId")
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "sequence", nullable = false)
    private Long sequence;

    @Builder
    public ItineraryPlace(Long itineraryId, Long placeId, Long sequence) {
        this.id = new ItineraryPlaceId(itineraryId, placeId);
        this.sequence = sequence;
    }

    // 내부 클래스로 복합키 정의
    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ItineraryPlaceId implements Serializable {
        @Column(name = "itinerary_id")
        private Long itineraryId;
        
        @Column(name = "place_id")
        private Long placeId;
    }
} 