package com.greg.golf.service.events;

import org.springframework.context.ApplicationEvent;

import com.greg.golf.entity.Round;
import lombok.Getter;

import java.io.Serial;

@Getter
public class RoundEvent extends ApplicationEvent {

	@Serial
    private static final long serialVersionUID = 1L;
	private final transient Round round;
	
	public RoundEvent(Object source, Round round) {
        super(source);
        this.round = round;
    }
}
