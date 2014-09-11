package com.github.kongchen.swagger.docgen.util;

import com.github.kongchen.swagger.docgen.ninjamodel.MethodUriInfo;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 1/21/14
 */
public class Utils {

	public static MethodUriInfo getMethodUriInfo(String uri, String apiUri) {

		String resourceUri = "";
		String methodUri = "";
		if(uri.startsWith(apiUri)) {
			String[] split = uri.replaceFirst("/", "").split("/");
			resourceUri = (split.length > 1) ? ("/" + split[1]) : "";
			if(uri.length() > apiUri.length()) {
				methodUri = resourceUri + uri.substring(apiUri.length() + resourceUri.length());
			}
		}
		return new MethodUriInfo(resourceUri, methodUri);
	}
}
