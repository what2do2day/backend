package com.one.what2do.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "itinerary_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItineraryUser extends BaseTimeEntity {

    @EmbeddedId
    private ItineraryUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itineraryId")
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public ItineraryUser(Long itineraryId, Long userId) {
        this.id = new ItineraryUserId(itineraryId, userId);
    }

    // 내부 클래스로 복합키 정의
    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ItineraryUserId implements Serializable {
        @Column(name = "itinerary_id")
        private Long itineraryId;
        
        @Column(name = "user_id")
        private Long userId;
    }
}
