package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.TransferListDto;
import com.turkcell.soccer.dto.response.PurchaseResponse;
import com.turkcell.soccer.dto.response.TransferListAdditionResponse;
import com.turkcell.soccer.dto.response.TransferListInfoResponse;
import com.turkcell.soccer.model.Purchase;
import com.turkcell.soccer.model.TransferList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferListMapper {

    TransferListDto transferListToDto(TransferList transferList);
    List<TransferListDto> transferListToDtoList(List<TransferList> transferList);

    @Mapping(source = "id", target = "purchaseId")
    PurchaseResponse toPurchaseResponse(Purchase purchase);

    default TransferListInfoResponse toTransferListInfoResponse(List<TransferListDto> dtoList) {
        if (dtoList == null) {
            return null;
        }

        TransferListInfoResponse response = new TransferListInfoResponse();
        response.setPlayers(dtoList);
        return response;

    }

    default TransferListAdditionResponse toAdditionResponse(TransferList transferList) {
        return new TransferListAdditionResponse(
                transferList.getPrice(),
                transferList.getPlayer().getId(),
                LocalDateTime.now()
        );
    }
}
