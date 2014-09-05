package com.github.kongchen.swagger.docgen.mavenplugin;

import ninja.params.Param;
import ninja.params.PathParam;

import java.lang.reflect.Parameter;

public class ParameterSpecification {

	private String parameterName;
	private String parameterType;

	public ParameterSpecification(Parameter methodParameter, com.wordnik.swagger.model.Parameter parameter) {
		parameterName = parameter.name();
		parameterType = parameter.paramType();

		if (methodParameter.isAnnotationPresent(PathParam.class)) {
			PathParam paramAnnotation = methodParameter.getAnnotation(PathParam.class);
			parameterName = paramAnnotation.value();
			parameterType = "path";
		}

		if (methodParameter.isAnnotationPresent(Param.class)) {
			Param paramAnnotation = methodParameter.getAnnotation(Param.class);
			parameterName = paramAnnotation.value();
			parameterType = "query";
		}
	}

	public String getParameterName() {
		return parameterName;
	}

	public String getParameterType() {
		return parameterType;
	}
}
