package com.bas.auction.core.config;

import com.bas.auction.core.utils.Utils;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@ComponentScan(basePackages = "com.bas.auction", useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.CUSTOM, classes = {ControllerTypeFilterImpl.class}),
        excludeFilters = @Filter(type = FilterType.ANNOTATION, value = Configuration.class))
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private Utils utils;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter msgConverter = new GsonHttpMessageConverter();
        Gson gson = utils.getGsonForClient();
        msgConverter.setGson(gson);
        converters.add(msgConverter);
    }
}
