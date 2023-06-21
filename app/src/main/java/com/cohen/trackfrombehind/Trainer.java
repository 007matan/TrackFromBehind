package com.cohen.trackfrombehind;

public class Trainer {

    protected enum Gender{
        MALE,
        FEMALE
    }

    private int birthYear = 0;
    private int height = 0;
    private int weight = 0;
    private Gender gender = Gender.MALE;
    private int trainAWeek = 0;

    public Trainer(){}

    public int getBirthYear() {
        return birthYear;
    }

    public Trainer setBirthYear(int birthYear) {
        this.birthYear = birthYear;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Trainer setHeight(int height) {
        this.height = height;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public Trainer setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Trainer setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public int getTrainAWeek() {
        return trainAWeek;
    }

    public Trainer setTrainAWeek(int trainAWeek) {
        this.trainAWeek = trainAWeek;
        return this;
    }
}
