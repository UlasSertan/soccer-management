package com.turkcell.soccer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name= "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column (nullable = false)
    private String firstName;

    @NotBlank
    @Column (nullable = false)
    private String lastName;

    @NotBlank
    @Column (nullable = false)
    private String country;

    @Min(0)
    @Column (nullable = false)
    private Integer value;

    @Max(40)
    @Min(18)
    @Column (nullable = false)
    private Integer age;
}
