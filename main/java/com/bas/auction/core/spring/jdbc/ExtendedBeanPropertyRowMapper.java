package com.bas.auction.core.spring.jdbc;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExtendedBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {
    public ExtendedBeanPropertyRowMapper() {
        super();
    }

    public ExtendedBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public ExtendedBeanPropertyRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
        super(mappedClass, checkFullyPopulated);
    }

    @Override
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        if (String[].class == pd.getPropertyType()) {
            if (rs.getArray(index) != null)
                return rs.getArray(index).getArray();
            return null;
        }
        return super.getColumnValue(rs, index, pd);
    }

}
