package com.cdk8s.code.gen.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.Array;
import java.util.*;

public final class CollectionUtil {


	public static boolean isNotEmpty(final Collection coll) {
		return CollectionUtils.isNotEmpty(coll);
	}

	public static boolean isEmpty(final Collection coll) {
		return CollectionUtils.isEmpty(coll);
	}


	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return MapUtils.isEmpty(map);
	}


	public static <T> List<T> toList(T[] arrays) {
		return new ArrayList<>(Arrays.asList(arrays));
	}


	public static <T> T[] toArray(Collection<T> collection, Class<T> componentType) {
		final T[] array = newArray(componentType, collection.size());
		return collection.toArray(array);
	}


	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(Class<?> componentType, int newSize) {
		return (T[]) Array.newInstance(componentType, newSize);
	}


}



