package com.greg.golf.entity;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
