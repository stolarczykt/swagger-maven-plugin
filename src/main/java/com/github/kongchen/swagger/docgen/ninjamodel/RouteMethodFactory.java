package com.github.kongchen.swagger.docgen.ninjamodel;

import com.github.kongchen.swagger.docgen.reflection.ClassMember;
import ninja.RouteBuilder;

import java.lang.reflect.Method;

public class RouteMethodFactory {

	private static final String HTTP_METHOD_FIELD_NAME = "httpMethod";
	private static final String CONTROLLER_METHOD_FIELD_NAME = "controllerMethod";

	public RouteMethod createFrom(RouteBuilder routeBuilder, String methodUri) {

		String httpMethodName = getHttpMethodName(routeBuilder);
		Method method = getOperationMethod(routeBuilder);

		return new RouteMethod(methodUri, httpMethodName, method);
	}

	private Method getOperationMethod(RouteBuilder routeBuilder) {
		ClassMember<Method> controllerMethodFiled = new ClassMember<>(routeBuilder, CONTROLLER_METHOD_FIELD_NAME);
		return controllerMethodFiled.getValue();
	}

	private String getHttpMethodName(RouteBuilder routeBuilder) {
		ClassMember<String> httpMethodField = new ClassMember<>(routeBuilder, HTTP_METHOD_FIELD_NAME);
		return httpMethodField.getValue();
	}
}
