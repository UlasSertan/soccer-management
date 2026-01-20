package com.turkcell.soccer.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamUpdateRequest {

    private String name;

    private String country;
}
