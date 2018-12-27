package com.sopt.rescat.dto;


import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.Place;
import lombok.Builder;

import java.util.List;

@Builder
public class MarkerListDto {
    private List<Cat> cats;
    private List<Place> hospitals;
    private List<Place> soupKitchens;
}
