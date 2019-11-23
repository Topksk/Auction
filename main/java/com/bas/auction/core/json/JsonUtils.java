package com.bas.auction.core.json;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

@Component
public class JsonUtils {

	private Object nextValue(JsonReader jr) throws IOException {
		JsonToken token = jr.peek();
		switch (token) {
		case STRING:
			return jr.nextString();
		case NUMBER:
			double v = jr.nextDouble();
			BigDecimal bd1 = BigDecimal.valueOf(v);
			BigDecimal bd2 = BigDecimal.valueOf((long) v);
			if (bd1.compareTo(bd2) == 0)
				return (long) v;
			else
				return v;
		case BOOLEAN:
			return jr.nextBoolean();
		case BEGIN_OBJECT:
			return parseObject(jr);
		case BEGIN_ARRAY:
			return parseArray(jr);
		case NULL:
			jr.nextNull();
			return null;
		default:
			jr.skipValue();
		}
		return null;
	}

	public Map<String, Object> parseObject(Reader reader) throws IOException {
		try (JsonReader jr = new JsonReader(reader)) {
			Map<String, Object> data = parseObject(jr);
			return data;
		}
	}

	private Map<String, Object> parseObject(JsonReader jr) throws IOException {
		jr.beginObject();
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		String key = "";
		while (jr.hasNext()) {
			key = jr.nextName();
			JsonToken token = jr.peek();
			switch (token) {
			case STRING:
			case NUMBER:
			case BOOLEAN:
			case BEGIN_OBJECT:
			case BEGIN_ARRAY:
			case NULL:
				params.put(key, nextValue(jr));
				break;
			default:
				jr.skipValue();
			}
		}
		jr.endObject();
		return params;
	}

	private List<?> parseArray(JsonReader jr) throws IOException {
		jr.beginArray();
		List<Object> res = new ArrayList<>();
		while (jr.hasNext()) {
			JsonToken token = jr.peek();
			switch (token) {
			case STRING:
			case NUMBER:
			case BOOLEAN:
			case BEGIN_OBJECT:
			case NULL:
				res.add(nextValue(jr));
				break;
			default:
				jr.skipValue();
			}
		}
		jr.endArray();
		return res;
	}

	private void moveToNext(JsonReader jr) throws IOException {
		while (true) {
			JsonToken token = jr.peek();
			if (token == JsonToken.BEGIN_OBJECT)
				jr.beginObject();
			else if (token == JsonToken.END_OBJECT)
				jr.endObject();
			else
				break;
		}
	}

	private void endObjects(JsonReader jr) throws IOException {
		while (jr.peek() == JsonToken.END_OBJECT) {
			jr.endObject();
		}
		if (jr.peek() == JsonToken.END_DOCUMENT)
			jr.close();
	}

	public <T> T nextValue(Gson gson, JsonReader jr, Class<T> clazz, String key) throws IOException {
		moveToNext(jr);
		String k = "";
		T result = null;
		while (jr.hasNext()) {
			k = jr.nextName();
			if (key.equals(k)) {
				result = gson.fromJson(jr, clazz);
				break;
			} else {
				jr.skipValue();
			}
		}
		endObjects(jr);
		return result;
	}

	public <T> T nextValue(Gson gson, JsonReader jr, Type type, String key)
			throws JsonIOException, JsonSyntaxException, IOException {
		moveToNext(jr);
		String k = "";
		T result = null;
		while (jr.hasNext()) {
			k = jr.nextName();
			if (key.equals(k)) {
				result = gson.fromJson(jr, type);
				break;
			} else {
				jr.skipValue();
			}
		}
		endObjects(jr);
		return result;
	}

	public List<Map<String, Object>> nextMapList(Gson gson, JsonReader jr, String key)
			throws JsonIOException, JsonSyntaxException, IOException {
		Type type = new TypeToken<List<Map<String, Object>>>() {
		}.getType();
		return nextValue(gson, jr, type, key);
	}

	public Map<String, Object> nextMap(Gson gson, JsonReader jr, String key)
			throws JsonIOException, JsonSyntaxException, IOException {
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		return nextValue(gson, jr, type, key);
	}
}
