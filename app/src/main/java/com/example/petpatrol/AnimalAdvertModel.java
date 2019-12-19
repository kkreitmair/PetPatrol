package com.example.petpatrol;

public class AnimalAdvertModel {
    public String title;
    public String animal;

    public AnimalAdvertModel(){}

    public AnimalAdvertModel(String title, String animal) {
        this.title = title;
        this.animal = animal;
    }

    public String getAnimal() {
        return animal;
    }

    public String getTitle() {
        return title;
    }
}
