package com.github.kongchen.swagger.docgen.reflection;

import java.lang.reflect.Field;

public class ClassMember<T> {

	private final T value;

	public ClassMember(Object instance, String memberName) {
		try {
			Field field = instance.getClass().getDeclaredField(memberName);
			field.setAccessible(true);
			this.value = (T) field.get(instance);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public T getValue() {
		return value;
	}
}
