package com.bas.auction.lookup.dto;

import java.util.ArrayList;
import java.util.List;

public class LookupResults {
	private String status;
	private final List<Lookup> items = new ArrayList<>();

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Lookup> getItems() {
		return items;
	}
}
