package com.greg.golf.entity;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(name = "course_tee")
public class CourseTee {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@Size(min = 3, max = 100, message = "Tee name must be in range 3 to 100 characters")
	@Column(name = "tee")
	private String tee;
	
	@NotNull
	@Min(value = 25, message = "Course rate cannot be lower than 25")
    @Max(value = 89, message = "Course rate cannot be greater than 89")
	@Column(name = "cr")
	private Float cr;
	
	@NotNull
	@Min(value = 55, message = "Slope rate cannot be lower than 55")
    @Max(value = 155, message = "Slope rate cannot be greater than 155")
	@Column(name = "sr")
	private Integer sr;
	
	@NotNull
	@Min(value = 0, message = "0 - 18 holes, 1 - first 9, 2 - second 9")
    @Max(value = 2, message = "0 - 18 holes, 1 - first 9, 2 - second 9")
	@Column(name = "tee_type")
	private Integer teeType;
	
	@EqualsAndHashCode.Exclude
	@NotNull
	private Boolean sex;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	//@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	private Course course;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	//@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "courseTee", orphanRemoval = true)
	private List<OnlineRound> onlineRound;

}
