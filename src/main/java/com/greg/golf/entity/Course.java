package com.greg.golf.entity;

import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.greg.golf.entity.helpers.OneOf;

@Entity
@Data
@Table(name = "course")
public class Course {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@NotNull
	private Boolean historical;
	
	@NotNull
	@Size(min = 3, max = 100, message = "Course name should be between 3 and 100 characters")
	@Column(name = "name")
	private String name;

	@NotNull
	@Min(value = 30, message = "Par should not be less than 30")
	@Max(value = 79, message = "Par should not be greater than 79")
	@Column(name = "par")
	private Integer par;

	@NotNull
	@OneOf(value = { 9, 18 }, message = "Number of holes can be either 9 or 18")
	@Column(name = "hole_nbr")
	private Integer holeNbr;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "course", orphanRemoval = true)
	private List<Hole> holes = new ArrayList<>();

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "course", orphanRemoval = true)
	private List<CourseTee> tees = new ArrayList<>();

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "course", orphanRemoval = true)
	private List<Round> rounds;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "course", orphanRemoval = true)
	private List<FavouriteCourse> favouriteCourse;
}
