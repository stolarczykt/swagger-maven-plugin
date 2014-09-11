package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.*;

public class Resource {

	private Class<?> controllerClass;
	private String resourceUri;
	private Map<String, List<RouteMethod>> routeMethodMap;

	public Resource(Class<?> controllerClass, String resourceUri) {
		this.controllerClass = controllerClass;
		this.resourceUri = resourceUri;
		this.routeMethodMap = new HashMap<>();
	}

	public Class<?> getControllerClass() {
		return controllerClass;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public Set<String> getOperationsUris(){
		return routeMethodMap.keySet();
	}

	public List<RouteMethod> getRouteMethodsFor(String uri) {
		return routeMethodMap.get(uri);
	}

	public void addRouteMethod(RouteMethod routeMethod) {
		String methodUri = routeMethod.getUri();
		if(routeMethodMap.containsKey(methodUri)) {
			routeMethodMap.get(methodUri).add(routeMethod);
		} else {
			ArrayList<RouteMethod> value = new ArrayList<>();
			value.add(routeMethod);
			routeMethodMap.put(methodUri, value);
		}
	}
}
