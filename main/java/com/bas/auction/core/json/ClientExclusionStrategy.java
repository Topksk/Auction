package com.bas.auction.core.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ClientExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getAnnotation(ExcludeFromClient.class) != null;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return clazz.getAnnotation(ExcludeFromClient.class) != null;
	}
}
