package com.one.what2do.repository;

import com.one.what2do.entities.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 이름 또는 주소로 검색 (대소문자 구분 없이)
    @Query("SELECT p FROM Place p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Place> findByNameOrAddressContainingIgnoreCase(@Param("keyword") String keyword);

    // 위도와 경도로 정확히 검색
    List<Place> findByLatitudeAndLongitude(String latitude, String longitude);

    // 위도 범위로 검색
    @Query("SELECT p FROM Place p WHERE CAST(p.latitude AS double) BETWEEN :minLat AND :maxLat")
    List<Place> findByLatitudeBetween(@Param("minLat") double minLat, @Param("maxLat") double maxLat);

    // 경도 범위로 검색
    @Query("SELECT p FROM Place p WHERE CAST(p.longitude AS double) BETWEEN :minLng AND :maxLng")
    List<Place> findByLongitudeBetween(@Param("minLng") double minLng, @Param("maxLng") double maxLng);

    // 위도와 경도 범위로 검색 (사각형 영역)
    @Query("SELECT p FROM Place p WHERE CAST(p.latitude AS double) BETWEEN :minLat AND :maxLat AND CAST(p.longitude AS double) BETWEEN :minLng AND :maxLng")
    List<Place> findByLatitudeBetweenAndLongitudeBetween(
            @Param("minLat") double minLat, 
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng, 
            @Param("maxLng") double maxLng
    );
} 