package com.bas.auction.core.dao;

public interface GenericDAO<T> extends SqlAware {
	Class<T> getEntityType();
}
