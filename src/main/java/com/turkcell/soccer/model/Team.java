package com.turkcell.soccer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column (nullable = false)
    private String name;

    @NotBlank
    @Column (nullable = false)
    private String country;

    @Min(0)
    private Integer playerCount;

    @Min(0)
    private Integer teamValue;

    @Min(0)
    private Integer budget;


}
