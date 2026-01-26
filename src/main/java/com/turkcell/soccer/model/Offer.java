package com.turkcell.soccer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name= "offers")
public class Offer {

    public enum OfferStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CANCELLED
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;
    @Version
    private Long version;
    @NotNull
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Team buyerTeam;
    @NotNull
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Team sellerTeam;
    @NotNull
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Player player;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private Integer offeredPrice;
    @NotNull
    @Enumerated(EnumType.STRING)
    private OfferStatus status;


}
