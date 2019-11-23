package com.bas.auction.core.dao;

import com.bas.auction.auth.dao.impl.SpravDAOImpl;
import com.bas.auction.profile.request.dto.ReqSearch;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.web.bind.annotation.RequestBody;

//import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bas.auction.profile.request.dto.AddressSearch;
import com.bas.auction.profile.request.dto.KskEmpsSearch;

public interface DaoJdbcUtil {

    <V> List<V> queryScalarList(SqlAware dao, String sqlCode, Object... params);

    <V> List<V> queryScalarList(SqlAware dao, String sqlCode);

    <V> V queryScalar(SqlAware dao, String sqlCode, Object... params);

    <V> V queryScalar(SqlAware dao, String sqlCode);

    <T> List<T> query(SqlAware dao, RowMapper<T> rowMapper, String sqlCode, Object... params);

    <T> List<T> query(GenericDAO<T> dao, String sqlCode, Object... params);

    <T> T queryForObject(SqlAware dao, RowMapper<T> rowMapper, String sqlCode, Object... params);

    <T> T queryForObject(GenericDAO<T> dao, String sqlCode, Object... params);

    Map<String, Object> queryForMap(SqlAware dao, String sqlCode, Object... params);

    <V> Map<String, V> queryForTypedMap(SqlAware dao, String sqlCode, Object... params);

    List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, Object... params);

    List<Map<String, Object>> queryForMapListParams(SqlAware dao, String sqlCode, Map<String, Object> params);

    List<Map<String,Object>> queryForMapListArray(SqlAware dao, String sqlCode,  int langid, String[] sid);

    List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, ReqSearch params);

    List<Map<String, Object>> queryForMapListServ(SqlAware dao, String sqlCode, ReqSearch params);

    List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode);

    List<Map<String, Object>> queryForMapList(Map<String, Object> params);

    List<Map<String, Object>> queryForMapList(String sqlCode, Object... params);

    List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, AddressSearch params);

    List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, KskEmpsSearch params);

    Date StrtoDate (String StrDate);

    String textByID(String sqlpath, Double n_id);

    String textByID(String sqlpath, Long n_id);

    String textByID(String sqlpath, Object... params);

    String textByID(String sqlpath, Double n_id, String s_field);

    int IdById(String sqlpath, Double n_id);

    int IdById(String sqlpath, Object... params);

    Long IdByParams(String sqlpath, Object... params);

    <V> List<Map<String, V>> queryForTypedMapList(SqlAware dao, String sqlCode, Object... params);

    <V> V execStoredFunc(String name, int resSqlType, Object... paramVals);

    <V> V execStoredFunc2(String name, int resSqlType,Object[] values);

    <V> V execStoredFunc3(String name, int resSqlType,Object[] values);

    void execStoredProc(String name, Object... paramVals);

    String getSql(SqlAware dao, String sqlName);

    boolean exists(SqlAware dao, String sqlCode, Object... params);

    KeyHolder dml(SqlAware dao, String sqlCode, Object[] values);

    KeyHolder dml_for_reg(SqlAware dao, String sqlCode, Object[] values);

    KeyHolder insert(SqlAware dao, Object[] values);

    KeyHolder insertNotifFile(SqlAware dao, Object[] values);

    KeyHolder insert_for_reg_sn(SqlAware dao, Object[] values);

    KeyHolder insert_for_reg(SqlAware dao, Object[] values);

    KeyHolder update(SqlAware dao, Object[] values);

    KeyHolder delete(SqlAware dao, Object[] values);

    KeyHolder inserts(SqlAware dao, String SqlCode, Object[] values) throws Exception;

    KeyHolder inserts(String SqlPath, Object[] values);

    KeyHolder updates(SqlAware dao, String SqlCode, Object[] values);

    KeyHolder updates(String SqlPath, Object[] values);

    KeyHolder deletes(String SqlPath, Object[] values);

    int[] batchDML(SqlAware dao, String sqlCode, List<Object[]> values);

    int[] batchInsert(SqlAware dao, List<Object[]> values);

    int[] batchUpdate(SqlAware dao, List<Object[]> values);

    int[] batchDelete(SqlAware dao, List<Object[]> values);

    Long seqNextval(String sequence);

    void commit();

    void rollback();
}
