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
    private Integer value = 1_000_000;

    @Max(40)
    @Min(18)
    @Column (nullable = false)
    private Integer age;

    @NotBlank
    @Column (nullable = false)
    private String position;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name = "team_id", nullable = false)
    private Team team;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private TransferList transferList;

    public Player(String firstName, String lastName, String country, Integer age, String position) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.age = age;
        this.position = position;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank())
            return;
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank())
            return;
        this.lastName = lastName;
    }

    public void setCountry(String country) {
        if (country == null || country.isBlank())
            return;
        this.country = country;
    }

    public void setValue(Integer value) {
        if (value < 0)
            throw new IllegalArgumentException("Value cannot be negative");
        this.value = value;
    }

    public void setAge(Integer age) {
        if (age < 18 || age > 40)
            throw new IllegalArgumentException("Age must be between 18 and 40");
        this.age = age;
    }

    public void setPosition(String position) {
        if (position == null || position.isBlank())
            return;
        if (!(position.equals("Goalkeeper") || position.equals("Defender")
            || position.equals("Midfielder") || position.equals("Forward")))
            throw new IllegalArgumentException("Position field is not valid");
        this.position = position;
    }

    public void setTeam(Team team) {
        if (team == null)
            return;
        this.team = team;
    }
}
