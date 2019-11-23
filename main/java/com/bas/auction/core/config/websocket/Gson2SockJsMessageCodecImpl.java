package com.bas.auction.core.config.websocket;

import com.bas.auction.core.utils.Utils;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.sockjs.frame.AbstractSockJsMessageCodec;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class Gson2SockJsMessageCodecImpl extends AbstractSockJsMessageCodec {
    private final Gson gson;

    @Autowired
    public Gson2SockJsMessageCodecImpl(Utils utils) {
        this.gson = utils.getGsonForClient();
    }

    @Override
    protected char[] applyJsonQuoting(String content) {
        return gson.toJsonTree(content).getAsString().toCharArray();
    }

    @Override
    public String[] decode(String content) throws IOException {
        return gson.fromJson(content, String[].class);
    }

    @Override
    public String[] decodeInputStream(InputStream content) throws IOException {
        InputStreamReader contentReader = new InputStreamReader(content);
        return gson.fromJson(contentReader, String[].class);
    }
}
