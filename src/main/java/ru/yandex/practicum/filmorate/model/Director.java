package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director implements Comparable<Director> {
    private int id;
    @NotBlank
    private String name;

    @Override
    public int compareTo(Director d) {
        return id - d.getId();
    }
}