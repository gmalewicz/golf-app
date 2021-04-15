package com.greg.golf.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseController {

	protected final ModelMapper modelMapper;

	protected <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {

		return source.stream().map(element -> modelMapper.map(element, targetClass)).collect(Collectors.toList());
	}
}
