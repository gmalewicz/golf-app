package com.greg.golf.service.events;

import org.springframework.context.ApplicationEvent;

import com.greg.golf.entity.Round;
import lombok.Getter;

@Getter
public class RoundEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private transient Round round;
	
	public RoundEvent(Object source, Round round) {
        super(source);
        this.round = round;
    }
}
