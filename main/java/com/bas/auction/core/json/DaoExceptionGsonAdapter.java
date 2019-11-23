package com.bas.auction.core.json;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bas.auction.core.dao.DaoException;
import com.bas.auction.core.dao.MessageDAO;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@Component
public class DaoExceptionGsonAdapter extends TypeAdapter<DaoException> {
	private final MessageDAO messages;

	@Autowired
	public DaoExceptionGsonAdapter(MessageDAO messages) {
		this.messages = messages;
	}

	@Override
	public void write(JsonWriter out, DaoException value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.beginObject();
		out.name("status").value("error");
		out.name("messages").beginArray();
		out.value(messages.get(value.getCode()));
		out.endArray().endObject();
	}

	@Override
	public DaoException read(JsonReader in) throws IOException {
		return null;
	}

}
