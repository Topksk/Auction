package com.bas.auction.comment.dao;

import java.util.List;

import com.bas.auction.auth.dto.User;
import com.bas.auction.comment.dto.Comment;

public interface CommentDAO {
	Comment create(User user, Comment comment);

	void update(User user, Comment comment);

	List<Comment> findComments(long negId);

	List<Comment> findChildren(long commentId);

	Comment findById(long commentId);
}
