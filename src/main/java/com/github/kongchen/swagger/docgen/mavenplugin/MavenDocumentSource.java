package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.filter.SpecFilter;
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
		java.util.Map<String, List<Resource>> resources = apiSource.getValidClasses();
		for (String resource : resources.keySet()) {
			ApiListing doc;
			try {
				doc = getDocFromClass(resource, swaggerConfig, getBasePath(), resources.get(resource));
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

	private ApiListing getDocFromClass(String resourcePath, SwaggerConfig swaggerConfig, String basePath, List<Resource> resources) throws Exception {
//        Api resource = (Api) c.getAnnotation(Api.class);

//        if (resource == null) return null;
//        JaxrsApiReader reader = new DefaultJaxrsApiReader();
//        reader.read(basePath, c, swaggerConfig);

		String apiVersion = swaggerConfig.getApiVersion();
		String swaggerVersion = swaggerConfig.getSwaggerVersion();
		scala.collection.immutable.List<String> produces = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<String> consumes = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<String> protocols = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<Authorization> authorizations = scala.collection.immutable.List.empty();

		List<ApiDescription> apiDescriptions = new ArrayList<>();
		for (Resource resource : resources) {
			List<Operation> operations = new ArrayList<>();
			operations.add(getOperation(resource));
			ApiDescription apiDescription = new ApiDescription(resourcePath, Option.empty(),
					scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(operations.iterator())));
			apiDescriptions.add(apiDescription);
		}

		scala.collection.immutable.List<ApiDescription> apis = scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiDescriptions.iterator()));
		Option<Map<String, Model>> models = Option.empty();
		Option<String> description = Option.apply("description");
		int position = 0;
		ApiListing apiListing = new ApiListing(apiVersion, swaggerVersion, basePath, resourcePath, produces, consumes,
				protocols, authorizations, apis, models, description, position);

		if (None.canEqual(apiListing)) return null;

		return apiListing;
	}

	private Operation getOperation(Resource resource) {
		String summary = "summary";
		String notes = "notes";
		String responseClass = "Task";
		String nickname = "Nickname";
		int position = 0;
		scala.collection.immutable.List<String> produces = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<String> consumes = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<String> protocols = scala.collection.immutable.List.empty();
		scala.collection.immutable.List<Authorization> authorisations = scala.collection.immutable.List.empty();

		java.lang.reflect.Parameter[] methodParameters = resource.getMethod().getParameters();
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
		return new Operation(resource.getHttpMethod(), summary, notes, responseClass, nickname, position, produces, consumes,
				protocols, authorisations, scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(parameters.iterator())),
				responseMessages, Option.<String>empty());
	}
}
