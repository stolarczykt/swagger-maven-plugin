package com.github.kongchen.swagger.docgen.ninjamodel;

import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import scala.None;
import scala.Option;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;

public class NinjaToSwaggerModelMapper {

	public ApiListing changeJaxrsApiListingBasingOnNinjaValues(ApiListing apiListing, Resource resource,
			SwaggerConfig swaggerConfig, String basePath) {

		scala.collection.immutable.List<ApiDescription> apis = getApiDescriptionsWithNinjaValues(apiListing, resource);

		apiListing = new ApiListing(swaggerConfig.getApiVersion(), apiListing.swaggerVersion(),
				basePath, resource.getResourceUri(), apiListing.produces(), apiListing.consumes(),
				apiListing.protocols(), apiListing.authorizations(), apis, apiListing.models(), apiListing.description(),
				apiListing.position());

		if (None.canEqual(apiListing)) return null;

		return apiListing;
	}

	private scala.collection.immutable.List<ApiDescription> getApiDescriptionsWithNinjaValues(ApiListing apiListing,
			Resource resource) {
		List<ApiDescription> resultApiDescriptions = new ArrayList<>();

		scala.collection.immutable.List<ApiDescription> apis = apiListing.apis();
		List<ApiDescription> apiDescriptions = JavaConversions.asJavaList(apis);

		for(ApiDescription apiDescription : apiDescriptions) {
			fillUpApiDescriptionWithNinjaValues(apiDescription, resource, resultApiDescriptions);
		}

		return scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(resultApiDescriptions.iterator()));
	}

	private void fillUpApiDescriptionWithNinjaValues(ApiDescription apiDescription, Resource resource, List<ApiDescription> resultApiDescriptions) {
		List<Operation> operations = JavaConversions.asJavaList(apiDescription.operations());
		for(String operationUri : resource.getOperationsUris()) {
			List<Operation> resultOperations = fillUpOperationsWithNinjaValues(resource, operations, operationUri);
			ApiDescription newApiDescription = new ApiDescription(operationUri, Option.empty(),
					scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(resultOperations.iterator())));
			resultApiDescriptions.add(newApiDescription);
		}
	}

	private List<Operation> fillUpOperationsWithNinjaValues(Resource resource, List<Operation> operations, String operationUri) {
		List<Operation> resultOperations = new ArrayList<>();
		for(RouteMethod routeMethod : resource.getRouteMethodsFor(operationUri)) {
			for(Operation operation : operations) {
				addOperationWhenEquivalentsInModelsWereFound(operation, routeMethod, resultOperations);
			}
		}
		return resultOperations;
	}

	private void addOperationWhenEquivalentsInModelsWereFound(Operation operation, RouteMethod routeMethod, List<Operation> resultOperations) {
		boolean equivalentsWereFound = routeMethod.getControllerMethod().getName().equals(operation.nickname());
		if(equivalentsWereFound){

			List<Parameter> resultParameters = changeOperationParametersToNinjaSpecific(operation, routeMethod);

			resultOperations.add(new Operation(routeMethod.getHttpMethod(), operation.summary(), operation.notes(),
					operation.responseClass(), operation.nickname(), operation.position(), operation.produces(),
					operation.consumes(), operation.protocols(), operation.authorizations(),
					scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(resultParameters.iterator())),
					operation.responseMessages(), operation.deprecated()));
		}
	}

	private List<Parameter> changeOperationParametersToNinjaSpecific(Operation operation, RouteMethod routeMethod) {
		List<Parameter> resultParameters = new ArrayList<>();
		List<Parameter> parameters = JavaConversions.asJavaList(operation.parameters());
		java.lang.reflect.Parameter[] methodParameters = routeMethod.getControllerMethod().getParameters();

		for (int i = 0; i < methodParameters.length; i++) {
			java.lang.reflect.Parameter methodParameter = methodParameters[i];
			Parameter parameter = parameters.get(i);
			ParameterSpecification parameterSpecification = new ParameterSpecification(methodParameter, parameter);

			resultParameters.add(new Parameter(parameterSpecification.getParameterName(), parameter.description(), parameter.defaultValue(),
					parameter.required(), parameter.allowMultiple(), parameter.dataType(),
					parameter.allowableValues(), parameterSpecification.getParameterType(), parameter.paramAccess()));
		}

		return resultParameters;
	}
}
