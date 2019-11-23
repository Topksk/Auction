package com.bas.auction.core.config;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

public class ControllerTypeFilterImpl implements TypeFilter {
    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        return metadataReader
                .getAnnotationMetadata()
                .getAnnotationTypes()
                .contains(RestController.class.getName());
    }
}
