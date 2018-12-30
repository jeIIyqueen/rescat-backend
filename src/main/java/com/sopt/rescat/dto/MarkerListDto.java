package com.sopt.rescat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MarkerListDto {
    private List<CatDto> cats;
    private List<PlaceDto> hospitals;
    private List<PlaceDto> feeders;
}