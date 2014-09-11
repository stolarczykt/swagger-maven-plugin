package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.*;
import org.apache.maven.plugin.logging.Log;
import scala.None;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.mutable.Buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {
	private final ApiSource apiSource;

	public MavenDocumentSource(ApiSource apiSource, Log log) {
		super(new LogAdapter(log),
				apiSource.getOutputPath(), apiSource.getOutputTemplate(), apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure(), apiSource.getOverridingModels());

		setApiVersion(apiSource.getApiVersion());
		setBasePath(apiSource.getBasePath() + apiSource.getApiUri());
		setApiInfo(apiSource.getApiInfo());
		this.apiSource = apiSource;
	}

	@Override
	public void loadDocuments() throws GenerateException {
		SwaggerConfig swaggerConfig = new SwaggerConfig();
		swaggerConfig.setApiVersion(apiSource.getApiVersion());
		swaggerConfig.setSwaggerVersion(SwaggerSpec.version());
		List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();

		List<AuthorizationType> authorizationTypes = new ArrayList<AuthorizationType>();
		java.util.Map<Class<?>, Resource> resources = apiSource.getValidResources();
		for (Class<?> key : resources.keySet()) {
			ApiListing doc;
			try {
				doc = getDocFromClass(resources.get(key), swaggerConfig, getBasePath());
			} catch (Exception e) {
				throw new GenerateException(e);
			}
			if (doc == null) continue;

			Buffer<AuthorizationType> buffer = doc.authorizations().toBuffer();
			authorizationTypes.addAll(JavaConversions.asJavaList(buffer));
			ApiListingReference apiListingReference = new ApiListingReference(doc.resourcePath(), doc.description(), doc.position());
			apiListingReferences.add(apiListingReference);
			acceptDocument(doc);
		}
		// sort apiListingRefernce by position
		Collections.sort(apiListingReferences, new Comparator<ApiListingReference>() {
			@Override
			public int compare(ApiListingReference o1, ApiListingReference o2) {
				if (o1 == null && o2 == null) return 0;
				if (o1 == null && o2 != null) return -1;
				if (o1 != null && o2 == null) return 1;
				return o1.position() - o2.position();
			}
		});
		serviceDocument = new ResourceListing(swaggerConfig.apiVersion(), swaggerConfig.swaggerVersion(),
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiListingReferences.iterator())),
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizationTypes.iterator())),
				toSwaggerApiInfo(apiSource.getApiInfo()));
	}

	private Option<ApiInfo> toSwaggerApiInfo(ApiSourceInfo info) {
		if (info == null) return Option.empty();
		return Option.apply(new ApiInfo(info.getTitle(), info.getDescription(),
				info.getTermsOfServiceUrl(), info.getContact(),
				info.getLicense(), info.getLicenseUrl()));
	}

	private ApiListing getDocFromClass(Resource resource, SwaggerConfig swaggerConfig, String basePath) throws Exception {

		basePath = basePath + apiSource.getApiUri();
        JaxrsApiReader reader = new DefaultJaxrsApiReader();
		ApiListing apiListing = reader.read(basePath, resource.getControllerClass(), swaggerConfig).get();

		List<ApiDescription> apiDescriptions = new ArrayList<>();
		scala.collection.immutable.List<ApiDescription> apis = apiListing.apis();
		List<ApiDescription> apiDescriptionsJava = JavaConversions.asJavaList(apis);

		for(ApiDescription apiDescription : apiDescriptionsJava) {
			List<Operation> operationsJava = JavaConversions.asJavaList(apiDescription.operations());
			for(String key : resource.getOperationsUris()) {
				List<Operation> operations = new ArrayList<>();
				for(RouteMethod routeMethod : resource.getRouteMethodsFor(key)) {
					for(Operation operation : operationsJava) {
						if(routeMethod.getControllerMethod().getName().equals(operation.nickname())){

							scala.collection.immutable.List<Parameter> parameters = operation.parameters();
							List<Parameter> resultParameters = new ArrayList<>();
							List<Parameter> parametersJava = JavaConversions.asJavaList(parameters);
							java.lang.reflect.Parameter[] methodParameters = routeMethod.getControllerMethod().getParameters();

							for (int i = 0; i < methodParameters.length; i++) {
								java.lang.reflect.Parameter methodParameter = methodParameters[i];
								Parameter parameter = parametersJava.get(i);
								ParameterSpecification parameterSpecification = new ParameterSpecification(methodParameter, parameter);

								resultParameters.add(new Parameter(parameterSpecification.getParameterName(), parameter.description(), parameter.defaultValue(),
										parameter.required(), parameter.allowMultiple(), parameter.dataType(),
										parameter.allowableValues(), parameterSpecification.getParameterType(), parameter.paramAccess()));
							}

							String httpMethod = routeMethod.getHttpMethod();
							operations.add(new Operation(httpMethod, operation.summary(), operation.notes(),
									operation.responseClass(), operation.nickname(), operation.position(), operation.produces(),
									operation.consumes(), operation.protocols(), operation.authorizations(),
									scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(resultParameters.iterator())),
									operation.responseMessages(), operation.deprecated()));
						}
					}
				}
				ApiDescription newApiDescription = new ApiDescription(key, Option.empty(),
						scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(operations.iterator())));
				apiDescriptions.add(newApiDescription);
			}
		}

		apis = scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiDescriptions.iterator()));

		apiListing = new ApiListing(swaggerConfig.getApiVersion(), apiListing.swaggerVersion(),
				basePath + apiSource.getApiUri(), resource.getResourceUri(), apiListing.produces(), apiListing.consumes(),
				apiListing.protocols(), apiListing.authorizations(), apis, apiListing.models(), apiListing.description(),
				apiListing.position());

		if (None.canEqual(apiListing)) return null;

		return apiListing;
	}

}
