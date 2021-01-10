package com.greg.golf.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "hole")
public class Hole {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;
	
	@NotNull
	@Min(value = 3, message = "Hole par cannot be lower than 3")
    @Max(value = 6, message = "Hole par cannot be higher than 6")
	@Column(name = "par")
	private Integer par;

	@NotNull
	@Min(value = 1, message = "Hole number cannot be lower than 1")
    @Max(value = 18, message = "Hole number cannot be higher than 18")
	@Column(name = "number")
	private Integer number;
	
	@NotNull
	@Min(value = 1, message = "Hole SI cannot be lower than 1")
    @Max(value = 18, message = "Hole SI cannot be higher than 18")
	@Column(name = "si")
	private Integer si;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonProperty(value = "course_id", access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	private Course course;
	
}
