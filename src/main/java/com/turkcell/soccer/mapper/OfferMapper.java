package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.response.OfferCreationResponse;
import com.turkcell.soccer.dto.response.OfferUpdateResponse;
import com.turkcell.soccer.model.Offer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OfferMapper {

    OfferCreationResponse toOfferCreationResponse(Offer offer);
    OfferUpdateResponse toOfferUpdateResponse(Offer offer);
    List<OfferCreationResponse> toOfferCreationResponseList(List<Offer> offers);
}
