package com.sopt.rescat.dto;


import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.Place;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;


@Getter
@Builder
public class MarkerListDto {
    private List<CatDto> cats;
    private List<PlaceDto> hospitals;
    private List<PlaceDto> feeders;
}
