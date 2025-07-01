package com.one.what2do.repository;

import com.one.what2do.entities.ItineraryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryUserRepository extends JpaRepository<ItineraryUser, ItineraryUser.ItineraryUserId> {
    List<ItineraryUser> findById_ItineraryId(Long itineraryId);
} 