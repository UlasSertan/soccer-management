package com.turkcell.soccer.docs;

import com.turkcell.soccer.dto.request.TeamRequest;
import com.turkcell.soccer.dto.request.TeamUpdateRequest;
import com.turkcell.soccer.dto.response.TeamInfoResponse;
import com.turkcell.soccer.dto.response.TeamResponse;
import com.turkcell.soccer.dto.response.TeamUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Team", description = "Team management APIs")
public interface TeamControllerDocs {


    @Operation(
            summary = "Create a new team",
            description = "Creates a new team with a team name and a country"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Team created successfully",
                    content = @Content(schema = @Schema(implementation = TeamResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Account already has a team"
            )
    })
    ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest teamRequest);

    @Operation(
            summary = "Create a new team",
            description = "Creates a new team with a team name and a country"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Team created successfully",
                    content = @Content(schema = @Schema(implementation = TeamResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Account already has a team"
            )
    })
    ResponseEntity<TeamInfoResponse> getTeamInfo();



    @Operation(
            summary = "Updates the team",
            description = "Updates the users own teams name and country"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Team updated successfully",
                    content = @Content(schema = @Schema(implementation = TeamUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Account does not have a team"
            )
    })
    ResponseEntity<TeamUpdateResponse>  updateTeam(@Valid @RequestBody TeamUpdateRequest teamRequest);


}
