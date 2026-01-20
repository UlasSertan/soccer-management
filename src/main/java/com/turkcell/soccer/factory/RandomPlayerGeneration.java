package com.turkcell.soccer.factory;

import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Team;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPlayerGeneration {

    private static final Random random = new Random();

    public static String getRandomFirstName() {
        return FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
    }

    public static String getRandomLastName() {
        return LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
    }

    public static String getRandomCountry() {
        return COUNTRIES.get(random.nextInt(COUNTRIES.size()));
    }

    public static int getRandomAge() {
        return random.nextInt(18, 41);
    }

    public static String initialPositionGenerate(int i) {
        if (i >= 20)
            throw new IllegalArgumentException("Can only generate 20 players");
        if (i < 3)
            return "Goalkeeper";
        if (i < 9)
            return "Defender";
        if (i < 15)
            return "Midfielder";
        return "Forward";
    }

    public static List<Player> initializeSquad(Team team) {
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Player player = new Player(
                    getRandomFirstName(),
                    getRandomLastName(),
                    getRandomCountry(),
                    getRandomAge(),
                    initialPositionGenerate(i));
            player.setTeam(team); // Player - Team relation
            players.add(player);
        }

        return players;
    }


    public static final List<String> FIRST_NAMES = List.of(
            "Ali",
            "Mehmet",
            "Emre",
            "Can",
            "Kerem",
            "Lucas",
            "Pedro",
            "Diego",
            "John",
            "Michael"
    );

    public static final List<String> LAST_NAMES = List.of(
            "Yılmaz",
            "Kaya",
            "Demir",
            "Şahin",
            "Rossi",
            "Garcia",
            "Smith",
            "Brown",
            "Silva",
            "Gomez"
    );

    public static final List<String> COUNTRIES = List.of(
            "Turkey",
            "Brazil",
            "Argentina",
            "Spain",
            "Italy",
            "France",
            "Germany",
            "England",
            "Portugal",
            "Netherlands"
    );
}
