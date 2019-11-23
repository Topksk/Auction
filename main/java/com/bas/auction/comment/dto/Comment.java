package com.bas.auction.comment.dto;

import java.util.List;

import com.bas.auction.core.dto.AuditableRow;

public class Comment extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4706253260288303788L;
	private long commentId;
	private long parentId;
	private long negId;
	private String text;
	private boolean isCustomer;
	private List<Comment> children;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getCommentId() {
		return commentId;
	}

	public void setCommentId(long commentId) {
		this.commentId = commentId;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public long getNegId() {
		return negId;
	}

	public void setNegId(long negId) {
		this.negId = negId;
	}

	public boolean isCustomer() {
		return isCustomer;
	}

	public void setIsCustomer(boolean isCustomer) {
		this.isCustomer = isCustomer;
	}

	public List<Comment> getChildren() {
		return children;
	}

	public void setChildren(List<Comment> children) {
		this.children = children;
	}
}
