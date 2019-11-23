package com.bas.auction.plans.dto;

import java.nio.file.Path;

import com.bas.auction.auth.dto.User;

public class PlanImport {

	public final long id;
	public final Path path;
	public final User user;
	public final long fileId;
	private Path log;

	public PlanImport(long id, Path path, User user, long fileId) {
		this.id = id;
		this.path = path;
		this.user = user;
		this.fileId = fileId;
	}

	public Path getLog() {
		return log;
	}

	public void setLog(Path log) {
		this.log = log;
	}

}
