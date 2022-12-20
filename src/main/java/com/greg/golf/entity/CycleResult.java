package com.greg.golf.entity;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Data
@Entity
@Table(name = "cycle_result")
public class CycleResult {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private Cycle cycle;

    @NotNull
    @Size(min = 3, max = 100, message = "Player last and first name")
    @Column(name = "player_name")
    private String playerName;

    @Type(IntArrayType.class)
    @NotNull
    @Column(name = "results", columnDefinition = "int[]")
    private int[] results;

    @NotNull
    @Min(value = 0, message = "Minimum total score")
    @Column(name = "total")
    private Integer total;

    @NotNull
    @Min(value = 0, message = "Minimum player cycle result")
    @Column(name = "cycle_result")
    private Integer cycleScore;

    @Transient
    private Float whs;

}
