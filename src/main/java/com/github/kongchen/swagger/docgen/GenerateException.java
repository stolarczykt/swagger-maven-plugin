package com.github.kongchen.swagger.docgen;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/29/2013
 */
public class GenerateException extends Throwable {

	private static final long serialVersionUID = -1641016437077276797L;

	public GenerateException(String errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}

	public GenerateException(String errorMessage) {
		super(errorMessage);
	}

	public GenerateException(Exception e) {
		super(e);
	}
}
