package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.filter.SpecFilter;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.*;
import ninja.params.PathParam;
import org.apache.maven.plugin.logging.Log;
import scala.None;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.immutable.Map;

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

	private final SpecFilter specFilter = new SpecFilter();

	public MavenDocumentSource(ApiSource apiSource, Log log) {
		super(new LogAdapter(log),
				apiSource.getOutputPath(), apiSource.getOutputTemplate(), apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure(), apiSource.getOverridingModels());

		setApiVersion(apiSource.getApiVersion());
		setBasePath(apiSource.getBasePath());
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
		java.util.Map<Class<?>, Resource> resources = apiSource.getValidClasses();
		for (Class<?> key : resources.keySet()) {
			ApiListing doc;
			try {
				doc = getDocFromClass(resources.get(key), swaggerConfig, getBasePath());
			} catch (Exception e) {
				throw new GenerateException(e);
			}
			if (doc == null) continue;
//            LOG.info("Detect Resource:" + c.getName());

//            Buffer<AuthorizationType> buffer = (Buffer<AuthorizationType>) doc.authorizations().toBuffer();
//            authorizationTypes.addAll(JavaConversions.asJavaList(buffer));
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
//        Api resource = (Api) c.getAnnotation(Api.class);

//        if (resource == null) return null;
        JaxrsApiReader reader = new DefaultJaxrsApiReader();
		ApiListing apiListing = reader.read(basePath, resource.getControllerClass(), swaggerConfig).get();
		String apiVersion = swaggerConfig.getApiVersion();
		String swaggerVersion = apiListing.swaggerVersion();
		scala.collection.immutable.List<String> produces = apiListing.produces();
		scala.collection.immutable.List<String> consumes = apiListing.consumes();
		scala.collection.immutable.List<String> protocols = apiListing.protocols();
		scala.collection.immutable.List<Authorization> authorizations = apiListing.authorizations();

		List<ApiDescription> apiDescriptions = new ArrayList<>();
		for (RouteMethod routeMethod : resource.getRouteMethods()) {
//			scala.collection.immutable.List<ApiDescription> apis = apiListing.apis();
//			apis.foreach();
			List<Operation> operations = new ArrayList<>();
			operations.add(getOperation(routeMethod));
			ApiDescription apiDescription = new ApiDescription(routeMethod.getUri(), Option.empty(),
					scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(operations.iterator())));
			apiDescriptions.add(apiDescription);
		}

		scala.collection.immutable.List<ApiDescription> apis = scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiDescriptions.iterator()));

		Option<Map<String, Model>> models = apiListing.models();


		Option<String> description = apiListing.description();
		int position = apiListing.position();
		ApiListing apiListing2 = new ApiListing(apiVersion, swaggerVersion, basePath, resource.getResourceUri(), produces, consumes,
				protocols, authorizations, apis, models, description, position);

		if (None.canEqual(apiListing)) return null;

		return apiListing2;
	}

	private Operation getOperation(RouteMethod routeMethod) {
		String summary = "summary";
		String notes = "notes";
		String responseClass = "Task";
		String nickname = "Nickname";
		int position = 0;
		scala.collection.immutable.List<String> produces = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<String> consumes = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<String> protocols = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<Authorization> authorisations = scala.collection.immutable.List.empty();

		java.lang.reflect.Parameter[] methodParameters = routeMethod.getControllerMethod().getParameters();
		List<Parameter> parameters = new ArrayList<>();
		for (java.lang.reflect.Parameter methodParameter : methodParameters) {
			String paramName = methodParameter.getName();

			if (methodParameter.isAnnotationPresent(PathParam.class)) {
				PathParam paramAnnotation = methodParameter.getAnnotation(PathParam.class);
				paramName = paramAnnotation.value();
			}
			Parameter parameter = new Parameter(paramName, Option.<String>empty(), Option.<String>empty(), false, false,
					methodParameter.getType().toString(), null, "path", Option.<String>empty());
			parameters.add(parameter);
		}

		scala.collection.immutable.List<ResponseMessage> responseMessages = scala.collection.immutable.List.empty();
		return new Operation(routeMethod.getHttpMethod(), summary, notes, responseClass, nickname, position, produces, consumes,
				protocols, authorisations, scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(parameters.iterator())),
				responseMessages, Option.<String>empty());
	}
}
