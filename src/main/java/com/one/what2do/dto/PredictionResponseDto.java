package com.one.what2do.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponseDto {
    private List<String> placeNames;
    private String message;
    private boolean success;
} 