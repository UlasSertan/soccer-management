package com.turkcell.soccer.model;


import org.springframework.data.jpa.domain.Specification;

public class TransferSpecification {

    public static Specification<TransferList> hasPlayerName(String playerName) {
        return (root, query, criteriaBuilder)
                -> {
            if (playerName == null || playerName.isBlank()) {
                return null;
            }

            query.distinct(true);

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("player").get("name")),
                    "%" + playerName.toLowerCase() + "%"
            );
        };
    };

    // (Check if returned root value contains name)
    public static Specification<TransferList> hasTeamName(String teamName) {
        return (root, query, criteriaBuilder)
                -> {
            if (teamName == null || teamName.isBlank()) {
                return null;
            }

            query.distinct(true);

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("player").join("team").get("name")),
                    "%" + teamName.toLowerCase() + "%"
            );

        };
    }

    // (Check if returned root value is same with country)
    public static Specification<TransferList> hasCountry(String country) {
        return (root, query, criteriaBuilder)
                -> {
            if (country == null) {
                return null;
            }

            query.distinct(true);

            return criteriaBuilder.equal(root.join("player").get("country"), country);
        };
    }

    public static Specification<TransferList> minValue(Integer minValue) {
        return (root, query, criteriaBuilder)
                -> minValue == null ? null : criteriaBuilder.greaterThan(root.get("value"), minValue);
    }

    public static Specification<TransferList> maxValue(Integer maxValue) {
        return (root, query, criteriaBuilder)
                -> maxValue == null ? null : criteriaBuilder.lessThan(root.get("value"), maxValue);
    }
}
