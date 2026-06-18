package com.example.CMCmp3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class LyricLine {

    @Column(nullable = false)
    private Double time;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;
}