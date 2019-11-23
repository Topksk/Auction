package com.bas.auction.comment.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.comment.dao.CommentDAO;
import com.bas.auction.comment.dto.Comment;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class CommentDAOImpl implements CommentDAO, GenericDAO<Comment> {
    private final static Logger logger = LoggerFactory.getLogger(CommentDAOImpl.class);
    private final DaoJdbcUtil daoutil;

    @Autowired
    public CommentDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "comments";
    }

    @Override
    public Class<Comment> getEntityType() {
        return Comment.class;
    }

    @Override
    @SpringTransactional
    public Comment create(User user, Comment comment) {
        Object[] values = {comment.getParentId(), comment.getNegId(), comment.getText(), comment.isCustomer(),
                user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        comment.setCommentId((Long) kh.getKeys().get("comment_id"));
        comment.setCreationDate((Date) kh.getKeys().get("creation_date"));
        return comment;
    }

    @Override
    public List<Comment> findComments(long negId) {
        logger.debug("find comments: negId = {}", negId);
        List<Comment> comments = daoutil.query(this, "find_comments", negId);
        for (Comment comment : comments) {
            comment.setChildren(findChildren(comment.getCommentId()));
        }
        return comments;
    }

    @Override
    public List<Comment> findChildren(long commentId) {
        logger.debug("find children of: commentId = {}", commentId);
        return daoutil.query(this, "find_children", commentId);
    }

    @Override
    @SpringTransactional
    public void update(User user, Comment comment) {
        logger.trace("data: {}", comment);
        Object[] values = {comment.getParentId(), comment.getNegId(), comment.getText(), comment.isCustomer(),
                comment.getCommentId()};
        daoutil.update(this, values);
    }

    @Override
    public Comment findById(long commentId) {
        return daoutil.queryForObject(this, "get", commentId);
    }
}
