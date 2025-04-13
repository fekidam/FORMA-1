package com.example.forma_1;

import java.io.Serializable;

public class Driver implements Serializable {
    private String name;
    private String team;
    private String nationality;
    private String imageName;
    private int podiums;
    private int points;
    private int grandsPrixEntered;
    private int worldChampionships;
    private String highestRaceFinish;
    private String highestGridPosition;
    private String dateOfBirth;
    private String placeOfBirth;

    public Driver(String name, String team, String nationality, String imageName, int podiums, int points,
                  int grandsPrixEntered, int worldChampionships, String highestRaceFinish,
                  String highestGridPosition, String dateOfBirth, String placeOfBirth) {
        this.name = name;
        this.team = team;
        this.nationality = nationality;
        this.imageName = imageName;
        this.podiums = podiums;
        this.points = points;
        this.grandsPrixEntered = grandsPrixEntered;
        this.worldChampionships = worldChampionships;
        this.highestRaceFinish = highestRaceFinish;
        this.highestGridPosition = highestGridPosition;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
    }

    public String getName() {
        return name;
    }

    public String getTeam() {
        return team;
    }

    public String getNationality() {
        return nationality;
    }

    public String getImageName() {
        return imageName;
    }

    public int getPodiums() {
        return podiums;
    }

    public int getPoints() {
        return points;
    }

    public int getGrandsPrixEntered() {
        return grandsPrixEntered;
    }

    public int getWorldChampionships() {
        return worldChampionships;
    }

    public String getHighestRaceFinish() {
        return highestRaceFinish;
    }

    public String getHighestGridPosition() {
        return highestGridPosition;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }
}