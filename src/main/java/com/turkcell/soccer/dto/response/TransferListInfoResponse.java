package com.turkcell.soccer.dto.response;
import com.turkcell.soccer.dto.TransferListDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferListInfoResponse {


    List<TransferListDto> players;

}
