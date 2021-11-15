package com.greg.golf.entity;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@TypeDefs({
		@TypeDef(
				name = "int-array",
				typeClass = IntArrayType.class
		)
})
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

	@Type(type = "int-array")
	@NotNull
	@Column(name = "results")
	private int[] results;

	@NotNull
	@Min(value = 0, message = "Minimum total score")
	@Column(name = "total")
	private Integer total;

	@NotNull
	@Min(value = 0, message = "Minimum player cycle result")
	@Column(name = "cycle_result")
	private Integer cycleResult;

	@Transient
	private Float whs;

}
