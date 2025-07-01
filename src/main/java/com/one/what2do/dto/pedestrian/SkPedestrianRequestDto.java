package com.one.what2do.dto.pedestrian;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkPedestrianRequestDto {
    
    @JsonProperty("startX")
    private String startX;
    
    @JsonProperty("startY")
    private String startY;
    
    @JsonProperty("endX")
    private String endX;
    
    @JsonProperty("endY")
    private String endY;
    
    @JsonProperty("startName")
    private String startName;
    
    @JsonProperty("endName")
    private String endName;
    
    @JsonProperty("reqCoordType")
    private String reqCoordType = "WGS84GEO";
    
    @JsonProperty("resCoordType")
    private String resCoordType = "WGS84GEO";
    
    @JsonProperty("searchOption")
    private String searchOption = "0";
    
    @JsonProperty("sort")
    private String sort = "index";
    
    // 선택적 파라미터들
    @JsonProperty("angle")
    private Integer angle;
    
    @JsonProperty("speed")
    private Integer speed;
    
    @JsonProperty("endPoiId")
    private String endPoiId;
    
    @JsonProperty("passList")
    private String passList;
} 