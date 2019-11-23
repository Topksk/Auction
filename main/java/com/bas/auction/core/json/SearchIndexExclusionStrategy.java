package com.bas.auction.core.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class SearchIndexExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getAnnotation(ExcludeFromSearchIndex.class) != null;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return clazz.getAnnotation(ExcludeFromSearchIndex.class) != null;
	}
}
