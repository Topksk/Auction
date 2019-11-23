package com.bas.auction.core.json;

import com.bas.auction.core.ApplException;
import com.bas.auction.core.dao.MessageDAO;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ApplExceptionGsonAdapter extends TypeAdapter<ApplException> {
    private final MessageDAO messages;

    @Autowired
    public ApplExceptionGsonAdapter(MessageDAO messages) {
        this.messages = messages;
    }

    @Override
    public void write(JsonWriter out, ApplException value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("status").value("error");
        out.name("messages").beginArray();
        boolean hasParams = value.getParams() != null;
        for (String code : value.getCodes()) {
            if (hasParams) {
                for (Map<String, String> param : value.getParams()) {
                    out.value(messages.get(code, param));
                }
            } else
                out.value(messages.get(code));
        }
        out.endArray().endObject();
    }


    @Override
    public ApplException read(JsonReader in) throws IOException {
        return null;
    }

}
