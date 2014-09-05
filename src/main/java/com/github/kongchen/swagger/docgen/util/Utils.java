package com.github.kongchen.swagger.docgen.util;

import com.github.kongchen.swagger.docgen.mavenplugin.MethodUriInfo;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableRangeValues;
import com.wordnik.swagger.model.AllowableValues;
import com.wordnik.swagger.model.AnyAllowableValues$;
import scala.Option;

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
				methodUri = uri.substring(apiUri.length() + resourceUri.length());
			}
		}
		return new MethodUriInfo(resourceUri, methodUri);
	}

	public static String getStrInOption(Option<String> scalaStr) {
		if (scalaStr.isEmpty()) return null;
		return scalaStr.get();
	}

	public static String allowableValuesToString(AllowableValues allowableValues) {
		if (allowableValues == null) {
			return null;
		}
		String values = "";
		if (allowableValues instanceof AllowableListValues) {
//            Buffer<String> buffer = (Buffer<String>) ((AllowableListValues) allowableValues).values().toBuffer();
//            for (String aVlist : JavaConversions.asJavaList(buffer)) {
//                values += aVlist.trim() + ", ";
//            }
			values = values.trim();
			values = values.substring(0, values.length() - 1);
		} else if (allowableValues instanceof AllowableRangeValues) {
			String max = ((AllowableRangeValues) allowableValues).max();
			String min = ((AllowableRangeValues) allowableValues).min();
			values = min + " to " + max;

		} else if (allowableValues instanceof AnyAllowableValues$) {
			return values;
		}
		return values;
	}
}
