package com.bas.auction.core.dao;

import com.bas.auction.core.spring.jdbc.CustomSQLErrorCodesTranslator;
import com.bas.auction.core.spring.jdbc.ExtendedBeanPropertyRowMapper;
import com.bas.auction.profile.request.dto.ReqSearch;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.text.SimpleDateFormat;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.bas.auction.profile.request.dto.AddressSearch;
import com.bas.auction.profile.request.dto.KskEmpsSearch;

@Component
public class DaoJdbcUtilImpl implements DaoJdbcUtil {
    private final static Logger logger = LoggerFactory.getLogger(DaoJdbcUtilImpl.class);
    private static final Map<String, String> sqlList = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbctempl;

    private synchronized static String getSqlFromFile(String sqlName) {
        //logger.debug(" getSqlFromFile, sqlName = " + sqlName);

        try (InputStream is = DaoJdbcUtilImpl.class.getResourceAsStream("/com/bas/conf/sql.xml")) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression xPathExpression = xPath.compile(String.format("/sql/%s/text()", sqlName));
            InputSource inputSource = new InputSource(is);
            String sql = xPathExpression.evaluate(inputSource).trim();
            sqlList.put(sqlName, sql);
            //logger.debug(" getSqlFromFile, sql = " + sql);
            return sql;
        } catch (Exception e) {
            //logger.debug("getSqlFromFile, exception = " + e.getMessage());
            logger.error("getSqlFromFile, exception = " + e.getMessage(), e);
            throw null;
        }
        //return "";
    }

    @Autowired
    public DaoJdbcUtilImpl(DataSource dataSource) {
        this.jdbctempl = new JdbcTemplate(dataSource);
        CustomSQLErrorCodesTranslator tr = new CustomSQLErrorCodesTranslator();
        jdbctempl.setResultsMapCaseInsensitive(true);
        jdbctempl.setExceptionTranslator(tr);
    }

    @Override
    public String getSql(SqlAware dao, String sqlName) {
        //logger.debug(" dao.getSqlPath()= "+dao.getSqlPath());
        return getSqlAbs(dao.getSqlPath() + "/" + sqlName);
    }


    private String getSqlAbs(String sqlName) {
        if (sqlList.containsKey(sqlName))
            return sqlList.get(sqlName);
        else
            return getSqlFromFile(sqlName);
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(jdbctempl.getDataSource());
    }

    @Override
    public void commit() {
        try {
            getConnection().commit();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void rollback() {
        try {
            getConnection().rollback();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> List<V> queryScalarList(SqlAware dao, String sqlCode, Object... params) {
        String sql = getSql(dao, sqlCode);
        logger.debug("parameterrrrrrrrrrrrr={}"+sql);
        return (List<V>) jdbctempl.queryForList(sql, Object.class, params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> List<V> queryScalarList(SqlAware dao, String sqlCode) {
        String sql = getSql(dao, sqlCode);
        return (List<V>) jdbctempl.queryForList(sql, Object.class, (Object[]) null);
    }

    @Override
    public <V> V queryScalar(SqlAware dao, String sqlCode, Object... params) {
        List<V> object = queryScalarList(dao, sqlCode, params);
        if (!object.isEmpty())
            return object.get(0);
        return null;
    }

    @Override
    public <V> V queryScalar(SqlAware dao, String sqlCode) {
        List<V> object = queryScalarList(dao, sqlCode);
        if (!object.isEmpty())
            return object.get(0);
        return null;
    }

    @Override
    public <T> List<T> query(SqlAware dao, RowMapper<T> rowMapper, String sqlCode, Object... params) {
        String sql = getSql(dao, sqlCode);
        //logger.debug("paramete22222222={}"+sql);
        return jdbctempl.query(sql, params, rowMapper);
    }

    @Override
    public <T> List<T> query(GenericDAO<T> dao, String sqlCode, Object... params) {
        return query(dao, new ExtendedBeanPropertyRowMapper<>(dao.getEntityType()), sqlCode, params);
    }

    @Override
    public <T> T queryForObject(SqlAware dao, RowMapper<T> rowMapper, String sqlCode, Object... params) {
        List<T> res = query(dao, rowMapper, sqlCode, params);
        if (!res.isEmpty())
            return res.get(0);
        return null;
    }

    @Override
    public <T> T queryForObject(GenericDAO<T> dao, String sqlCode, Object... params) {
        List<T> res = query(dao, sqlCode, params);
        if (!res.isEmpty())
            return res.get(0);
        return null;
    }

    @Override
    public Map<String, Object> queryForMap(SqlAware dao, String sqlCode, Object... params) {
        List<Map<String, Object>> res = queryForMapList(dao, sqlCode, params);
        if (!res.isEmpty())
            return res.get(0);
        return null;
    }

    @Override
    public <V> Map<String, V> queryForTypedMap(SqlAware dao, String sqlCode, Object... params) {
        Map<String, Object> map = queryForMap(dao, sqlCode, params);
        return castMapValues(map);
    }

    @Override
    public List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, Object... params) {
        String sql = getSql(dao, sqlCode);
        try {
            return jdbctempl.queryForList(sql, params);
        }
        catch (Exception e) {
            logger.error("queryForMapList_1.exception = " + e.getMessage(), e);
            logger.error(" queryForMapList_1, sqlCode="+sqlCode+ ", sqltext =" + sql);
        }
        throw null;
    }

	
    @Override
    public List<Map<String, Object>> queryForMapListParams(SqlAware dao, String sqlCode, Map<String, Object> params){
        String sql = getSql(dao, sqlCode);
        ArrayList<Object> newObj = new ArrayList<Object>();
        String s_where="";
        try {

            if (sqlCode.equals("get_ksk_by_user_params") || sqlCode.equals("get_rel_id_by_user_params")) {
                if (!params.get("street").toString().isEmpty()){
                    s_where += " AND tb2.t_street_id=?";
                    newObj.add(Math.round((Double)params.get("street")));
                }
                if (!params.get("home").toString().isEmpty()){
                    s_where += " AND tb2.building_num=?";
                    if(params.get("home").getClass().getName()=="java.lang.String") {
                        newObj.add(Integer.parseInt((String) params.get("home")));
                    }else {
                        newObj.add(Math.round((Double) params.get("home")));
                    }
                }
                if (!params.get("fraction").toString().isEmpty()){
                    s_where += " AND tb2.build_subunit=lower(?)";
                    newObj.add((String) params.get("fraction"));
                }else {
                    s_where += " AND tb2.build_subunit IS NULL ";
                }

                if (!params.get("flat").toString().isEmpty()){
                    s_where += " AND tf2.flat_num=?";
                    if(params.get("flat").getClass().getName()=="java.lang.String") {
                        newObj.add(Integer.parseInt((String) params.get("flat")));
                    }else{
                        newObj.add(Math.round((Double) params.get("flat")));
                    }
                }
                if (!params.get("flatfraction").toString().isEmpty()){
                    s_where += " AND tf2.flat_subunit=lower(?)";
                    newObj.add((String) params.get("flatfraction"));
                }else{
                    s_where += " AND tf2.flat_subunit IS NULL";
                }
                if (!params.get("userId").toString().isEmpty()){
                    s_where += " AND tr2.t_user_id=?";
                    newObj.add(Math.round((Long) params.get("userId")));
                }
                if (sqlCode.equals("get_ksk_by_user_params")) {
                    s_where += ")";
                }
                logger.info("newObj count="+newObj.size());
            }

            Object[] new_params=new Object[newObj.size()];
            for (int i =0 ; i<newObj.size(); i++) {
                logger.info("newObj.get(i)="+newObj.get(i));
                new_params[i]=newObj.get(i);
            }
            logger.info("sql+s_where="+sql+s_where);
            return jdbctempl.queryForList(sql+s_where, new_params);
        }
        catch (Exception e) {
            logger.error("queryForMapList_1.exception = " + e.getMessage(), e);
            logger.error(" queryForMapList_1, sqlCode="+sqlCode+ ", sqltext =" + sql);
        }
        throw null;
    }

	
    public List<Map<String, Object>> queryForMapListArray(SqlAware dao, String sqlCode,  int langid, String[] params) {
        String sql = getSql(dao, sqlCode);
        String reverseSql;
        boolean allIsChoosen=false;
        logger.info(" queryForMapList_1 arraaay {}{}="+sqlCode);
        logger.info(" queryForMapList_1 arraaay params {}{}="+params[0]);
        sql+=" in (";
        for (int rec=0; rec<params.length; rec++){
            if ((((String) params[rec]).equals("-1") && params.length==1) || (((String) params[rec]).equals("-1.0") && params.length==1)){
                allIsChoosen=true;
            }
            if (rec ==(params.length-1)) {
                sql += (String) params[rec];
            }else{
                sql += (String) params[rec] + ", ";
            }
        }

        sql+=")";

        Object[] new_params;
        if (langid!=-1){
            new_params = new Object[1];
            new_params[0] = langid;
        }else{
            new_params = new Object[0];
        }


        if (allIsChoosen){
            logger.info(" queryForMapList_1 arraaay sddsdssd {}{}=" + sql);
            sql = new StringBuilder(sql).reverse().toString();

            logger.info(" queryForMapList_1 arraaay sql {}{}=" + sql);
            sql = new StringBuilder(sql.substring(sql.indexOf("dna") + 4, sql.length())).reverse().toString();
            logger.info(" queryForMapList_1 cut sql {}{}=" + sql);
        }

        //return jdbctempl.queryForList(sql, params);
        return jdbctempl.queryForList(sql, new_params);
        //  return null;

    }
	
    @Override
    public List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, ReqSearch params) {
        MDC.put("action", "queryForMapList_2");
        String sql = getSql(dao, sqlCode);
        int n_cp=188;
        try {
            String s_where="";
            String s_val="";
            int n_not_null = -1;
            Object[] newObj;
            n_cp=194;
            //if (params.getId()==null) {params.setId("");}
            s_val=params.getId();
            if (s_val!=null && !s_val.isEmpty()) {    // Если указан номер заявки
                s_val=params.getKsk_id();               //Если выбрана КСК
                if (s_val!=null && !s_val.isEmpty()){

                    s_where = " AND t8.id=?";
                    newObj = new Object[3];
                    n_not_null++;
                    newObj[n_not_null]= Integer.parseInt(params.getLang_id());
                    n_not_null++;
                    newObj[n_not_null]= Integer.parseInt(params.getKsk_id());
                }else{
                    newObj = new Object[4];
                    n_not_null++;
                    newObj[n_not_null]= Integer.parseInt(params.getLang_id());
                    s_val=params.getUser_id();
                    if (s_val!=null && !s_val.isEmpty()){
                        n_not_null++;
                        n_cp=215;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        n_cp=216;
                        s_where += " AND t3.id in (select tr.t_flats_id from ch_ksk.t_user tu, ch_ksk.t_relation tr where tu.id=tr.t_user_id and tr.status not in (3,4) and tu.id=?)";
                        n_not_null++;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        s_where += " AND case when t2.to_ksk = 0 then t1.t_user_id = ? else 1=1 end";
                    }
                }

                n_cp=202;
                s_where += " AND t1.id=?";

                n_not_null++;
                newObj[n_not_null]= Integer.parseInt(params.getId());
            }
            else {
                int n_len = 11;
                newObj = new Object[n_len];
                n_cp=204;
                // Язык
                n_not_null++;
                newObj[n_not_null]= Integer.parseInt(params.getLang_id());
                n_cp=207;
                //id Ksk
                s_val=params.getKsk_id();               //Если выбрана КСК
                if (s_val!=null && !s_val.isEmpty()){
                    s_where = " AND t8.id=?";
                    n_not_null++;
                    newObj[n_not_null]= Integer.parseInt(params.getKsk_id());
                }
                n_cp=207;
                //  Дата начала
                n_not_null++;
                Date date1 = null;
                newObj[n_not_null] = StrtoDate(params.getDat_reg_beg());

                n_cp=213;
                //  Дата окончания
                n_not_null++;
                newObj[n_not_null] = StrtoDate(params.getDat_reg_end());
                s_where += " AND t1.dat_reg between ? and ?";

                n_cp=219;
                s_val=params.getT_flats_id();
                if (s_val!=null && !s_val.isEmpty()) {    // Если выбрана квартира
                    n_not_null++;
                    n_cp=225;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                            //(Math.round(Float.parseFloat(s_val.toString())));
                            //Integer.parseInt(params.getT_flats_id());
                    n_cp=229;
                    s_where += " AND t3.id=?";
                }
                else {
                    // Если пользователь выбрал все квартиры
                    n_cp=214;
                    s_val=params.getUser_id();
                    if (s_val!=null && !s_val.isEmpty()){
                        n_not_null++;
                        n_cp=215;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        n_cp=216;
                        s_where += " AND t3.id in (select tr.t_flats_id from ch_ksk.t_user tu, ch_ksk.t_relation tr where tu.id=tr.t_user_id and tr.status not in (3,4) and tu.id=?)";
                        n_not_null++;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        s_where += " AND case when t2.to_ksk = 0 then t1.t_user_id = ? else 1=1 end";
                    }
                    s_val=params.getReq_building();
                    n_cp=234;
                    if (s_val!=null && !s_val.isEmpty()) {      // Если выбрано здание
                        n_not_null++;
                        n_cp=237;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        s_where += " AND t3.t_building_id=?";
                    }
                    else {
                        s_val=params.getReq_street();
                        n_cp=243;
                        if (s_val!=null && !s_val.isEmpty()) {      //  Если выбрана улица
                            n_not_null++;
                            n_cp=246;
                            newObj[n_not_null] = Integer.parseInt(s_val);
                            s_where += " AND t4.t_street_id=?";
                        }
                        else  {
                            s_val=params.getReq_city();
                            n_cp=252;
                            if (s_val!=null && !s_val.isEmpty()) {      //  Если выбран город
                                n_not_null++;
                                n_cp=255;
                                newObj[n_not_null] = Integer.parseInt(s_val);
                                s_where += " AND t5.t_city_id=?";
                            }
                        }
                    }
                }
                n_cp=262;
                /*s_val=params.getT_req_subtype_id();
                //logger.info("getT_req_subtype_id="+s_val);
                if (s_val!=null && !s_val.isEmpty()) {  // Если указан подтип заявки
                    n_not_null++;
                    n_cp=266;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    s_where += " AND t1.t_req_subtype_id=?";
                }
                else {*/
                    s_val=params.getReq_type();
                    //logger.info("getReq_type="+s_val);
                    if (s_val!=null && !s_val.isEmpty()) {  //  Если указан тип заявки
                        n_not_null++;
                        n_cp=274;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        if (sqlCode.equals("citreqs_list")||sqlCode.equals("reqs_list")) {
                            s_where += " AND t2.id in (WITH RECURSIVE q (id) AS (\n" +
                                    "    select ts.id\n" +
                                    "      FROM ch_ksk.t_services ts\n" +
                                    "     WHERE ts.id=?\n" +
                                    "    UNION ALL\n" +
                                    "    select ts2.id\n" +
                                    "      from ch_ksk.t_services ts2, q\n" +
                                    "     WHERE q.id=ts2.serv_id\n" +
                                    ")\n" +
                                    "SELECT q.id as id\n" +
                                    "  FROM q)";
                        }
                        else {
                            s_where += " AND t2.id=?";
                        }

                    }
                //}
                s_val=params.getT_req_priority_id();
                if (s_val!=null && !s_val.isEmpty()) { // Если указана срочность
                    n_not_null++;
                    n_cp=282;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    s_where += " AND t1.t_req_priority_id=?";
                }
                s_val=params.getReq_status();
                if (s_val!=null && !s_val.isEmpty()) {  // Если указан статус
                    n_cp=289;
                  if (!s_val.equals("-1")) {
                    if (s_val.equals("0")) {
                        s_where += " AND t1.t_req_status_id not in (5,6,8)";
                      } else if (s_val.equals("9")) {
                        s_where += " AND t1.t_req_status_id in (5,6,8)";
                      } else {
                        n_not_null++;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        s_where += " AND t1.t_req_status_id=?";
                    }
                }
                }
                s_val=params.getReq_disp_exec();
                if (s_val!=null && !s_val.isEmpty()) {
                    n_not_null++;
                    n_cp=296;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    s_where += " AND exists (select 1 from ch_ksk.t_oper_history t_hist where t_hist.t_request_id=t1.id and t_hist.t_position_id=?)";
                }
                s_val=params.getReq_executer();
                if (s_val!=null && !s_val.isEmpty()) {
                    n_not_null++;
                    n_cp=303;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    s_where += " AND exists (select 1 from ch_ksk.t_oper_history t_hist where t_hist.t_request_id=t1.id and t_hist.t_position_id=?)";
                }
            }
            n_cp=308;

            sql += s_where;
            sql += " order by 1 desc";

            Object[] new_params = new Object[n_not_null + 1];

            if (n_not_null==0) {
                new_params[n_not_null] = newObj[n_not_null];
                //logger.debug("1par " + n_not_null + " = " + new_params[n_not_null]);
            }
            else {
                for(int i1 = 0; i1 <= n_not_null; i1++){
                    new_params[i1] = newObj[i1];
                    //logger.debug("par " + i1 + " = " + new_params[i1]);
                }
            }
            logger.info("sql="+sql);
            return jdbctempl.queryForList(sql, new_params);
        }
        catch (Exception e) {
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            logger.info("sql="+sql);
        }
        throw null;
    }

    @Override
    public List<Map<String, Object>> queryForMapListServ(SqlAware dao, String sqlCode, ReqSearch params) {
        MDC.put("action", "queryForMapList_3.1");
        String sql = getSql(dao, sqlCode);
        int n_cp=188;
        try {
            String s_where="";
            String s_val="";
            int n_not_null = -1;
            Object[] newObj;
            n_cp=194;
            //if (params.getId()==null) {params.setId("");}
            s_val=params.getId();
            if (s_val!=null && !s_val.isEmpty()) {    // Если указан номер заявки
                s_val=params.getComp_id();               //Если выбрана Компания
                logger.info("s_valCompId=="+s_val);
                if (s_val!=null && !s_val.isEmpty()){

                    s_where = " AND t9.id=?";
                    newObj = new Object[4];
                    n_not_null++;
                    newObj[n_not_null]= Integer.parseInt(params.getLang_id());
                    n_not_null++;
                    newObj[n_not_null]= Integer.parseInt(params.getComp_id());
                    n_cp=195;
                    n_not_null++;
                    newObj[n_not_null] = Integer.parseInt(params.getReq_city());
                    s_where += " AND t5.t_city_id=? AND t8.city_id=t5.t_city_id ";
                    n_cp=196;
                }else {
                    newObj = new Object[2];
                    n_not_null++;
                    newObj[n_not_null] = Integer.parseInt(params.getLang_id());
                }




                n_cp=202;
                s_where += " AND t1.id=?";

                n_not_null++;
                newObj[n_not_null]= Integer.parseInt(params.getId());
            }
            else {
                int n_len = 11;
                newObj = new Object[n_len];
                n_cp=204;
                // Язык
                n_not_null++;
                newObj[n_not_null]= Integer.parseInt(params.getLang_id());
                n_cp=207;
                s_val=params.getComp_id();               //Если выбрана КСК
                if (s_val!=null && !s_val.isEmpty()){
                    s_where = " AND t9.id=?";
                    n_not_null++;
                    newObj[n_not_null]= Integer.parseInt(params.getComp_id());
                }
                n_cp=208;
                //  Дата начала
                n_not_null++;
                Date date1 = null;
                newObj[n_not_null] = StrtoDate(params.getDat_reg_beg());

                n_cp=213;
                //  Дата окончания
                n_not_null++;
                newObj[n_not_null] = StrtoDate(params.getDat_reg_end());
                s_where += " AND t1.dat_reg between ? and ?";

              /*  n_cp=219;
                s_val=params.getT_flats_id();
                if (s_val!=null && !s_val.isEmpty()) {    // Если выбрана квартира
                    n_not_null++;
                    n_cp=225;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    //(Math.round(Float.parseFloat(s_val.toString())));
                    //Integer.parseInt(params.getT_flats_id());
                    n_cp=229;
                    s_where += " AND t3.id=?";
                }
                else {
                    // Если пользователь выбрал все квартиры
                    n_cp=214;
                    s_val=params.getUser_id();
                    if (s_val!=null && !s_val.isEmpty()){
                        n_not_null++;
                        n_cp=215;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        n_cp=216;
                        s_where += " AND t3.id in (select tr.t_flats_id from ch_ksk.t_user tu, ch_ksk.t_relation tr where tu.id=tr.t_user_id and tu.id=?)";
                    }
                    s_val=params.getReq_building();
                    n_cp=234;
                    if (s_val!=null && !s_val.isEmpty()) {      // Если выбрано здание
                        n_not_null++;
                        n_cp=237;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        s_where += " AND t3.t_building_id=? ";
                    }
                    else {
                        s_val=params.getReq_street();
                        n_cp=243;
                        if (s_val!=null && !s_val.isEmpty()) {      //  Если выбрана улица
                            n_not_null++;
                            n_cp=246;
                            newObj[n_not_null] = Integer.parseInt(s_val);
                            s_where += " AND t4.t_street_id=? AND t8.city_id=t5.t_city_id";
                        }
                        else  {*/
                            s_val=params.getReq_city();
                            n_cp=252;
                            if (s_val!=null && !s_val.isEmpty()) {      //  Если выбран город
                                n_not_null++;
                                n_cp=255;
                                newObj[n_not_null] = Integer.parseInt(s_val);
                                s_where += " AND t5.t_city_id=? AND t8.city_id=t5.t_city_id";
                            }
                    /*    }
                    }
                }*/
                n_cp=262;
                /*s_val=params.getT_req_subtype_id();
                //logger.info("getT_req_subtype_id="+s_val);
                if (s_val!=null && !s_val.isEmpty()) {  // Если указан подтип заявки
                    n_not_null++;
                    n_cp=266;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    s_where += " AND t1.t_req_subtype_id=?";
                }
                else {*/
                s_val=params.getReq_type();
                //logger.info("getReq_type="+s_val);
                if (s_val!=null && !s_val.isEmpty()) {  //  Если указан тип заявки
                    n_not_null++;
                    n_cp=274;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    s_where += " AND ? in (select distinct (serv.id) from (\n" +
                            "            WITH RECURSIVE q (id, serv_id , level) AS (\n" +
                            "            select ts.id,ts.serv_id, 1 as level\n" +
                            "            FROM ch_ksk.t_services ts\n" +
                            "            WHERE \n" +
                            "             ts.status=1\n" +
                            "            AND ts.to_ksk<>1\n" +
                            "            and ts.id=t1.t_services_id\n" +
                            "            UNION ALL\n" +
                            "            select ts2.id,ts2.serv_id, q.level +1 as level\n" +
                            "            from ch_ksk.t_services ts2, q\n" +
                            "            WHERE\n" +
                            "             ts2.status=1\n" +
                            "            and q.serv_id=ts2.id\n" +
                            "            )\n" +
                            "            SELECT case when q.serv_id is null then -100 else q.id end as id, q.serv_id, q.level\n" +
                            "            FROM q\n" +
                            "            where q.level=(select max(k.level)-1 from q as k)\n" +
                            "            ORDER BY q.level desc) as serv)";
                }
                s_val=params.getReq_status();
                if (s_val!=null && !s_val.isEmpty()) {  // Если указан статус
                    n_cp=289;
                    if (!s_val.equals("-1")) {
                    if (s_val.equals("0")) {
                        s_where += " AND t1.t_req_status_id not in (5,6,8)";
                        } else if (s_val.equals("9")) {
                        s_where += " AND t1.t_req_status_id in (5,6,8)";
                        } else {
                        n_not_null++;
                        newObj[n_not_null] = Integer.parseInt(s_val);
                        s_where += " AND t1.t_req_status_id=?";
                    }
                }
                }

             /*   s_val=params.getReq_executer();
                if (s_val!=null && !s_val.isEmpty()) {
                    n_not_null++;
                    n_cp=303;
                    newObj[n_not_null] = Integer.parseInt(s_val);
                    s_where += " AND exists (select 1 from ch_ksk.t_oper_history t_hist where t_hist.t_request_id=t1.id and t_hist.t_position_id=?)";
                }*/
            }
            n_cp=308;

            sql += s_where;
            sql += " order by 1 desc";

            Object[] new_params = new Object[n_not_null + 1];

            if (n_not_null==0) {
                new_params[n_not_null] = newObj[n_not_null];
                //logger.debug("1par " + n_not_null + " = " + new_params[n_not_null]);
            }
            else {
                for(int i1 = 0; i1 <= n_not_null; i1++){
                    new_params[i1] = newObj[i1];
                    //logger.debug("par " + i1 + " = " + new_params[i1]);
                }
            }
            logger.info("sql="+sql);
            return jdbctempl.queryForList(sql, new_params);
        }
        catch (Exception e) {
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            logger.info("sql="+sql);
        }
        throw null;
    }


    @Override
    public List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode) {
        String sql = getSql(dao, sqlCode);
        //logger.debug(" queryForMapList_3, sqlCode="+sqlCode+ ", sqltext =" + sql);
        return jdbctempl.queryForList(sql, (Object[]) null);
    }

    @Override
    public List<Map<String, Object>> queryForMapList(Map<String, Object> params) {
        MDC.put("action", "queryForMapList_4");
        logger.info("params="+params.toString());
        int n_cp=323;
        String sql = "";
        String sqlCode = null;
        int par_cnt = params.size();
        if (par_cnt<1)
            return null;
        Object[] new_params = new Object[par_cnt - 1];
        try {
            int n_id = -1;
            int nn  = 0;
            String s_where = "0";
            n_cp=333;
            for (String key : params.keySet()) {
                nn++;
                //logger.info(" queryForMapList_4, " + nn +", key=" + key);
                n_cp=338;
                Object val = params.get(key);
                if (key.equals("sqlpath")){
                    sqlCode = val.toString();
                   // logger.info(" queryForMapList_4d, " + nn + ", sqlCode=" + sqlCode);
                }
                else {
                    if (key.substring(0,1).equals("d")) {
                        if (val!=null){
                            val = StrtoDate(val.toString());
                        }
                    }
                    n_id++;
                    n_cp=354;
                    new_params[n_id] = val;
                }
            }


            if (sqlCode.equals("sprav/all_position_building")){
                Object[] new_params2=new Object[3];

                new_params2[0]=params.get("lang_id");
                new_params2[1]=params.get("userId");
                new_params2[2]=params.get("kskid");
            sql = getSqlAbs(sqlCode);
                        logger.debug("jdbctempl.queryForList(={}{}="+jdbctempl.queryForList(sql, new_params2).size());
                if(jdbctempl.queryForList(sql, new_params2).size()>0){
                    return jdbctempl.queryForList(sql, new_params2);
                }else{
                    Object[] new_params3=new Object[2];
                    new_params3[0]=params.get("lang_id");
                    new_params3[1]=params.get("kskid");
                    sqlCode="sprav/all_building_pos";
                    sql = getSqlAbs(sqlCode);
                    logger.debug("sql(=="+sql);
                    logger.debug("jdbctempl.queryForList(2=="+jdbctempl.queryForList(sql, new_params2).size());
                    return jdbctempl.queryForList(sql, new_params2);
                }
            }else if (sqlCode.equals("sprav/serv_subtype")){
                Object[] new_params2=new Object[1];

                new_params2[0]=params.get("lang_id");
                sql = getSqlAbs(sqlCode);

                logger.debug("sql32323(=="+sql);

                StringTokenizer st = new StringTokenizer(params.get("serv_id").toString().substring(1,params.get("serv_id").toString().length()-1), ",");
               if (st.hasMoreTokens()) {
                   sql+=" AND ts.serv_id in (";
                   while (st.hasMoreTokens()) {
                       sql+= Math.round(Double.parseDouble(st.nextToken()));
                       if (st.hasMoreTokens()) {
                           sql += ", ";
                       }
                   }
                   sql+=")";
               }
               return jdbctempl.queryForList(sql, new_params2);
            }else if (sqlCode.equals("sprav/serv_questions")){
                Object[] new_params3=new Object[1];

                new_params3[0]=params.get("lang_id");
                sql = getSqlAbs(sqlCode);

                logger.debug("sql4444(=="+sql);

                StringTokenizer st = new StringTokenizer(params.get("serv_id").toString().substring(1,params.get("serv_id").toString().length()-1), ",");
                if (st.hasMoreTokens()) {
                    sql+=" and tsq.t_services_id in (";
                    while (st.hasMoreTokens()) {
                        sql+= Math.round(Double.parseDouble(st.nextToken()));
                        if (st.hasMoreTokens()) {
                            sql += ", ";
                        }
                    }
                    sql+=")";
                }
                return jdbctempl.queryForList(sql, new_params3);
            }else if (sqlCode.equals("sprav/serv_questions_mng")){
                Object[] new_params3=new Object[1];
                new_params3[0]=params.get("lang_id");
                sql = getSqlAbs(sqlCode);
                String tmpParam="";
                StringTokenizer st = new StringTokenizer(params.get("serv_id").toString().substring(1,params.get("serv_id").toString().length()-1), ",");
                if (st.hasMoreTokens()) {
                    while (st.hasMoreTokens()) {
                        tmpParam+= Math.round(Double.parseDouble(st.nextToken()));
                        if (st.hasMoreTokens()) {
                            tmpParam += ", ";
                        }
                        }
                    }

                sql+=" and tsq.t_services_id in ("+tmpParam+") " +
                        "union " +
                        "select  '' as text , ts.id as ts_id, tlt2.lang_text as serv_text " +
                        "from ch_ksk.t_services ts, " +
                        "ch_ksk.t_any_lang_text tlt2, " +
                        "param p " +
                        "where ts.status=1 " +
                        "and ts.to_ksk<>1 " +
                        "and tlt2.table_name='t_services' " +
                        "and tlt2.table_id=ts.id " +
                        "and tlt2.t_language_id=p.lang_id " +
                        "and ts.id in (" +tmpParam+") "+
                        "order by ts_id asc";

                logger.info("sql4444(=="+sql);
                return jdbctempl.queryForList(sql, new_params3);
            }else if (sqlCode.equals("sprav/get_comp_moderatormail")){
                    Object[] new_params4=new Object[2];
                    new_params4[0]=Integer.parseInt(params.get("lang_id").toString());
                    new_params4[1]=Integer.parseInt(params.get("comp_id").toString());
                    sql = getSqlAbs(sqlCode);
                   /*  if(params.get("code").equals("f_edit_serv_function")) {
                        sql = getSqlAbs(sqlCode);
                        logger.debug("sql5555(==" + sql);

                       StringTokenizer st = new StringTokenizer(params.get("city_id").toString(), "~");
                        if (st.hasMoreTokens()) {
                            sql += " and tm.t_franchiser_citys_id in (";
                            while (st.hasMoreTokens()) {
                                sql += (Integer.parseInt(st.nextToken()));
                                if (st.hasMoreTokens()) {
                                    sql += ", ";
                                }
                            }
                            sql += ") ";
                        }sql+=" and tmc.t_comp_id="+params.get("comp_id");*/

                        logger.debug("sql6666(==" + sql);
                        return jdbctempl.queryForList(sql, new_params4);

            }

            n_cp=358;
            sql = getSqlAbs(sqlCode);
            n_cp=384;
            if (!s_where.equals("0")) {
                sql+=s_where;
            }
            n_cp=386;
            return jdbctempl.queryForList(sql, new_params);
        }
        catch (Exception e) {
            for (int i =0; i<new_params.length; i++){
                logger.info(" new_params("+ i + ")=" + new_params[i]);
            };
            logger.info(" queryForMapList_4, sql=["+sql+"]");
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
        }
        throw null;
    }

    @Override
    public List<Map<String, Object>> queryForMapList(String sqlCode, Object... params) {
        logger.info("sqlCode="+sqlCode);
        String sql = getSqlAbs(sqlCode);
        for (int i =0; i<params.length; i++){
            logger.info(" params("+ i + ")=" + params[i]);
        };

        try {


        if(sqlCode.equals("sprav/get_serv_emps_lang_text")) {
            logger.info(params[2].toString());
            StringTokenizer st = new StringTokenizer(params[2].toString().substring(0, params[2].toString().length()), ",");
            if (st.hasMoreTokens()) {
                sql += " and tcp.code in (";
                while (st.hasMoreTokens()) {
                    sql += "'"+st.nextToken()+"'";
                    if (st.hasMoreTokens()) {
                        sql += ", ";
                    }
                }
                sql += ")";
            }

            params= ArrayUtils.remove(params, 2);
            return jdbctempl.queryForList(sql, params);
        }

            return jdbctempl.queryForList(sql, params);
        }
        catch (Exception e) {
            logger.info("queryForMapList, exception.sql="+sql);
            for (int i =0; i<params.length; i++){
                logger.info(" params("+ i + ")=" + params[i]);
            }
            logger.error("exception = " + e.getMessage(), e);
            throw null;
        }

    }

    @Override
    public List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, AddressSearch params) {
        MDC.put("action", "queryForMapList_6");
        String sql = getSql(dao, sqlCode);
        int n_cp=188;
        try {
            String s_where="";
            String s_val="";
            int n_not_null = -1;
            Object[] newObj;
            n_cp=194;
            //if (params.getId()==null) {params.setId("");}
            //s_val=params.getId();
            int n_len = 10;
            newObj = new Object[n_len];

            //newObj = new Object[1];
            n_cp=199;
            n_not_null++;
            newObj[n_not_null]= Integer.parseInt(params.getLangId());
            s_where = " where s.t_language_id=cast(? as int)";

            //newObj = new Object[1];
            if (params.getId()!=null && !params.getId().isEmpty()) {
                n_cp = 199;
                n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getId());
                s_where += " and k.id=cast(? as int)";
            }

            //int n_len = 10;
            //newObj = new Object[n_len];
            if (params.getCity()!=null && !params.getCity().isEmpty()) {
                n_cp = 213;
                //  Город
                n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getCity());
                s_where += " and c.id=cast(? as int)";
            }
            if (params.getStreet()!=null && !params.getStreet().isEmpty()) {
                n_cp = 214;
                n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getStreet());
                s_where += " and s.id=cast(? as int)";
            }
            if (params.getBuilding()!=null && !params.getBuilding().isEmpty()) {
                n_cp = 215;
                n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getBuilding());
                s_where += " and b.building_num=cast(? as int)";
            }
            if (params.getFraction()!=null && !params.getFraction().isEmpty()) {
                n_cp = 216;
                n_not_null++;
                newObj[n_not_null] = params.getFraction();
                s_where += " and lower(trim(b.build_subunit))=lower(trim(?))";
            }
            if (params.getPhone()!=null && !params.getPhone().isEmpty()) {
                n_cp = 217;
                n_not_null++;
                newObj[n_not_null] = params.getPhone();
                s_where += " and ds.mobile_phone=?";
            }
            if (params.getDisp()!=null && !params.getDisp().isEmpty()) {
                n_cp = 218;
                n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getDisp());
                s_where += " and ds.t_position_id=?";
            }
			if (params.getStatus()!=null && !params.getStatus().isEmpty() && !params.getStatus().equals("-1")) {
                n_cp = 218;
                n_not_null++;
                //logger.info("111111111111111111111111111111222222222222");
                newObj[n_not_null] = Integer.parseInt(params.getStatus());
                s_where += " and bk.status=cast(? as int)";
            }

            n_cp=308;

            sql += s_where;
            sql += " order by 1";

            Object[] new_params = new Object[n_not_null + 1];

            //if (n_not_null==0) {
            //new_params[n_not_null] = newObj[n_not_null];
            //logger.debug("1par " + n_not_null + " = " + new_params[n_not_null]);
            //}
            //else {
            for(int i1 = 0; i1 <= n_not_null; i1++){
                new_params[i1] = newObj[i1];
                //logger.debug("par " + i1 + " = " + new_params[i1]);
            }
            //}
            //logger.info("sql "+sql);
            //logger.debug("param111 "+new_params);
            return jdbctempl.queryForList(sql, new_params);
        }
        catch (Exception e) {
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            logger.info("sql="+sql);
        }
        throw null;
    }

    @Override
    public List<Map<String, Object>> queryForMapList(SqlAware dao, String sqlCode, KskEmpsSearch params) {
        MDC.put("action", "queryForMapList_5Emps");
        String sql = getSql(dao, sqlCode);
        int n_cp=188;
        try {

            if(sqlCode.equals("ksk_emps_list")) {
                String s_where = "";
                String s_val = "";
                int n_not_null = -1;
                Object[] newObj;
                n_cp = 194;
            //if (params.getId()==null) {params.setId("");}
            //s_val=params.getId();
            int n_len = 10;
            newObj = new Object[n_len];

            //newObj = new Object[1];
                n_cp = 199;
            n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getLangId());
            s_where = " and tlt.t_language_id=cast(? as int)";

            //newObj = new Object[1];
                if (params.getId() != null && !params.getId().isEmpty()) {
                n_cp = 199;
                n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getId());
                s_where += " and p.t_ksk_id=cast(? as int)";
            }

            //int n_len = 10;
            //newObj = new Object[n_len];
            if (params.getLastName()!=null && !params.getLastName().isEmpty()) {
                n_cp = 213;
                //  Город
                n_not_null++;
                newObj[n_not_null] = params.getLastName();
                s_where += " and lower(trim(u.lastname)) like lower(trim(?))";
            }
            if (params.getFirstName()!=null && !params.getFirstName().isEmpty()) {
                n_cp = 214;
                n_not_null++;
                newObj[n_not_null] = params.getFirstName();
                s_where += " and lower(trim(u.firstname)) like lower(trim(?))";
            }
            if (params.getMiddleName()!=null && !params.getMiddleName().isEmpty()) {
                n_cp = 215;
                n_not_null++;
                newObj[n_not_null] = params.getMiddleName();
                s_where += " and lower(trim(u.middlename)) like lower(trim(?))";
            }
            if (params.getPhone()!=null && !params.getPhone().isEmpty()) {
                n_cp = 216;
                n_not_null++;
                newObj[n_not_null] = params.getPhone();
                s_where += " and u.mobile_phone=?";
            }
            if (params.getPriznPos()!=null && !params.getPriznPos().isEmpty()){
                //logger.info("adadadadad=gfgfgfgf");
                s_where += " and pt.code in (";
                String input = params.getPriznPos();
                String[] values = input.replaceAll("^[,\\s]+", "").split("[,\\s]+");

                for(int j = 0; j < values.length; j++) {
                    s_where += "'";
                    s_where += values[j];
                    if (j != values.length - 1)
                    s_where +="',";
                    else
                    s_where +="'";
                }
                logger.info("s_where="+s_where);
                s_where += ")";
            }


			if (params.getStatus()!=null && !params.getStatus().isEmpty()) {
                n_cp = 216;
                //n_not_null++;
                //newObj[n_not_null] = params.getStatus();
                s_where += " and p.date_end is not null";
            }
            else {
                s_where += " and p.date_end is null";
            }

            n_cp=308;

            sql += s_where;
            sql += " order by u.lastname,u.firstname,u.middlename";

            Object[] new_params = new Object[n_not_null + 1];

            //if (n_not_null==0) {
            //new_params[n_not_null] = newObj[n_not_null];
            //logger.debug("1par " + n_not_null + " = " + new_params[n_not_null]);
            //}
            //else {
            for(int i1 = 0; i1 <= n_not_null; i1++){
                new_params[i1] = newObj[i1];
                //logger.debug("par " + i1 + " = " + new_params[i1]);
            }
            //}
            logger.debug("sql "+sql);
            //logger.debug("param111 "+new_params);
            return jdbctempl.queryForList(sql, new_params);
            }else{
                String s_where = "";
                String s_val = "";
                int n_not_null = -1;
                Object[] newObj;
                n_cp = 194;
                int n_len = 10;
                newObj = new Object[n_len];

                n_cp = 199;
                n_not_null++;
                newObj[n_not_null] = Integer.parseInt(params.getLangId());
                s_where = " and tlt.t_language_id=cast(? as int)";

                if (params.getId() != null && !params.getId().isEmpty()) {
                    n_cp = 199;
                    n_not_null++;
                    newObj[n_not_null] = Integer.parseInt(params.getId());
                    s_where += " and p.comp_id=cast(? as int)";
                }

                if (params.getLastName() != null && !params.getLastName().isEmpty()) {
                    n_cp = 213;
                    //  Город
                    n_not_null++;
                    newObj[n_not_null] = params.getLastName();
                    s_where += " and lower(trim(u.lastname)) like lower(trim(?))";
                }
                if (params.getFirstName() != null && !params.getFirstName().isEmpty()) {
                    n_cp = 214;
                    n_not_null++;
                    newObj[n_not_null] = params.getFirstName();
                    s_where += " and lower(trim(u.firstname)) like lower(trim(?))";
                }
                if (params.getMiddleName() != null && !params.getMiddleName().isEmpty()) {
                    n_cp = 215;
                    n_not_null++;
                    newObj[n_not_null] = params.getMiddleName();
                    s_where += " and lower(trim(u.middlename)) like lower(trim(?))";
                }
                if (params.getPhone() != null && !params.getPhone().isEmpty()) {
                    n_cp = 216;
                    n_not_null++;
                    newObj[n_not_null] = params.getPhone();
                    s_where += " and u.mobile_phone=?";
                }
                if (params.getPriznPos() != null && !params.getPriznPos().isEmpty()) {
                    s_where += " and pt.code in (";
                    String input = params.getPriznPos();
                    String[] values = input.replaceAll("^[,\\s]+", "").split("[,\\s]+");

                    for (int j = 0; j < values.length; j++) {
                        s_where += "'";
                        s_where += values[j];
                        if (j != values.length - 1)
                            s_where += "',";
                        else
                            s_where += "'";
                    }
                    logger.info("s_where=" + s_where);
                    s_where += ")";
                }
                if (params.getStatus() != null && !params.getStatus().isEmpty()) {
                    n_cp = 216;
                    s_where += " and p.date_end is not null";
                } else {
                    s_where += " and p.date_end is null";
                }
                n_cp = 308;
                sql += s_where;
                sql += " order by u.lastname,u.firstname,u.middlename";
                Object[] new_params = new Object[n_not_null + 1];
                for (int i1 = 0; i1 <= n_not_null; i1++) {
                    new_params[i1] = newObj[i1];
                }
                logger.debug("sql " + sql);
                return jdbctempl.queryForList(sql, new_params);
            }
        }
        catch (Exception e) {
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            logger.info("sql="+sql);
        }
        throw null;
    }

    @Override
    public Date StrtoDate(String StrDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdtf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        Date date1 = null;
        try {
            date1 = sdtf.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
            try {
                date1 = sdf.parse(StrDate);
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
        }
        return date1;
    }

    @Override
    public String textByID(String sqlpath, Double n_id) {
        logger.info("textByID.sqlpath=" + sqlpath+ ", n_id="+n_id);
        Map<String, Object> par_other = new HashMap<String, Object>();
        List<Map<String, Object>> res;
        String s_val=null;
        par_other.put("sqlpath", sqlpath);
        par_other.put("id", n_id);
        res = queryForMapList(par_other);
        logger.info("textByID.res=" + res.toString()+ ", res.size()="+res.size());
        if (res.size()>0) {
            s_val = res.get(0).get("text").toString();
        }
        return s_val;
    }

    @Override
    public String textByID(String sqlpath, Long n_id) {
        logger.info("textByID.sqlpath=" + sqlpath+ ", n_id="+n_id);
        Map<String, Object> par_other = new HashMap<String, Object>();
        List<Map<String, Object>> res;
        String s_val=null;
        par_other.put("sqlpath", sqlpath);
        par_other.put("id", n_id);
        res = queryForMapList(par_other);
        logger.info("textByID.res=" + res.toString()+ ", res.size()="+res.size());
        if (res.size()>0) {
            s_val = res.get(0).get("text").toString();
        }
        return s_val;
    }

    @Override
    public String textByID(String sqlpath, Object... params) {
        //logger.info("textByID.sqlpath=" + sqlpath+ ", n_id="+n_id);
        List<Map<String, Object>> res;
        String s_val=null;
        res = queryForMapList(sqlpath, params);
        logger.info("textByID.res=" + res.toString()+ ", res.size()="+res.size());
        if (res.size()>0) {
            s_val = res.get(0).get("text").toString();
        }
        return s_val;
    }

    @Override
    public String textByID(String sqlpath, Double n_id, String s_field) {
        logger.info("textByID.sqlpath=" + sqlpath+ ", n_id="+n_id);
        Map<String, Object> par_other = new HashMap<String, Object>();
        List<Map<String, Object>> res;
        String s_val=null;
        par_other.put("sqlpath", sqlpath);
        par_other.put("id", n_id);
        res = queryForMapList(par_other);
        logger.info("textByID.res=" + res.toString()+ ", res.size()="+res.size());
        if (res.size()>0) {
            s_val = res.get(0).get(s_field).toString();
        }
        return s_val;
    }

    @Override
    public int IdById(String sqlpath, Double n_id) {
        logger.info("textByID.sqlpath=" + sqlpath+ ", n_id="+n_id);
        Map<String, Object> par_other = new HashMap<String, Object>();
        List<Map<String, Object>> res;
        int n_val=0;
        par_other.put("sqlpath", sqlpath);
        par_other.put("id", n_id);
        res = queryForMapList(par_other);
        logger.info("textByID.res=" + res.toString()+ ", res.size()="+res.size());
        if (res.size()>0) {
            n_val = Integer.parseInt(res.get(0).get("id").toString());
        }
        return n_val;
    }

    @Override
    public Long IdByParams(String sqlpath, Object... params) {
        List<Map<String, Object>> res;
        Long n_val = null;
        res = queryForMapList(sqlpath, params);
        logger.info("IdByParams.res=" + res.toString()+ ", res.size()="+res.size());
        if (res.size()>0) {
            n_val = (Long) res.get(0).get("id");
        }
        return n_val;
    }

    @Override
    public int IdById(String sqlpath, Object... params) {
        List<Map<String, Object>> res;
        int n_val=0;
        res = queryForMapList(sqlpath, params);
        logger.info("IdById.res=" + res.toString()+ ", res.size()="+res.size());
        if (res.size()>0) {
            n_val = Integer.parseInt(res.get(0).get("id").toString());
        }
        return n_val;
    }

    @Override
    public <V> List<Map<String, V>> queryForTypedMapList(SqlAware dao, String sqlCode, Object... params) {
        List<Map<String, Object>> mapList = queryForMapList(dao, sqlCode, params);
        Stream<Map<String, V>> castedMap = mapList.stream().map(this::castMapValues);
        return castedMap.collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <V> Map<String, V> castMapValues(Map<String, Object> map) {
        return map.entrySet().stream().collect(toMap(Entry::getKey, e -> (V) e.getValue()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V execStoredFunc(String name, int resSqlType, Object... paramVals) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbctempl).withSchemaName("ch_ksk").withFunctionName(name)
                .declareParameters(new SqlOutParameter("result", resSqlType));
        return (V) call.executeFunction(Object.class, paramVals);
    }


    @SuppressWarnings("unchecked")
    public <V> V execStoredFunc2(String name, int resSqlType,Object[] values) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbctempl).withSchemaName("ch_ksk").withFunctionName(name)
                .declareParameters(new SqlOutParameter("result", resSqlType));
        return (V) call.executeFunction(Object.class, values);
    }

    @SuppressWarnings("unchecked")
    public <V> V execStoredFunc3(String name, int resSqlType,Object[] values) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbctempl).withSchemaName("topcore").withFunctionName(name)
                .declareParameters(new SqlOutParameter("result", resSqlType));
        return (V) call.executeFunction(Object.class, values);
    }

    @Override
    public void execStoredProc(String name, Object... paramVals) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbctempl).withSchemaName("auction").withProcedureName(name);
        call.execute(paramVals);
    }

    @Override
    public boolean exists(SqlAware dao, String sqlCode, Object... params) {
        List<?> res = queryScalarList(dao, sqlCode, params);
        return !res.isEmpty();
    }

    @Override
    public KeyHolder dml(SqlAware dao, String sqlCode, Object[] values) {
        MDC.put("action", "dml");
        int n_cp=693;
        String sql="";
        try {
            n_cp++;
            sql = getSql(dao, sqlCode);
            n_cp++;
            GeneratedKeyHolder kh = new GeneratedKeyHolder();
            n_cp++;
            jdbctempl.update(new DMLPrepparedStatement(sql, values), kh);
            n_cp++;
            Object[] new_params;
            if (dao.getSqlPath().equals("auth/session") && sqlCode.equals("insert")){
                Long n_session=(Long) kh.getKeys().get("session_id");
                logger.info("n_session="+n_session);
                new_params = new Object[2];
                new_params[0]="session_id";
                new_params[1]=n_session.toString();
                queryForMapList("auth/session/set_session_id", new_params);
                logger.info("setting session");
            }
            n_cp++;
            return kh;
        }
        catch (Exception e) {
            logger.info("dao.getSqlPath()="+dao.getSqlPath());
            logger.info("dml, sqlCode=" + sqlCode +",sql=" + sql);
            logger.error("dml, exception, n_cp="+n_cp+"," + e.getMessage(), e);
            throw e;
        }
    }

    public KeyHolder dml_for_reg(SqlAware dao, String sqlCode, Object[] values) {
        String sql = getSql(dao, sqlCode);
        logger.debug("DaoJdbcUtil.dml, sqlCode=" + sqlCode +",sql=" + sql);
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbctempl.update(new DMLPrepparedStatement(sql, values), kh);
        return kh;
    }

    @Override
    public KeyHolder insert(SqlAware dao, Object[] values) {
        return dml(dao, "insert", values);
    }

    @Override
    public KeyHolder insertNotifFile(SqlAware dao, Object[] values) {
        return dml(dao, "insert_notif_file", values);
    }

    public KeyHolder insert_for_reg_sn(SqlAware dao, Object[] values) {
        return dml_for_reg(dao, "insert_sn", values);
    }

    public KeyHolder insert_for_reg(SqlAware dao, Object[] values) {
        return dml_for_reg(dao, "insert", values);
    }

    @Override
    public KeyHolder update(SqlAware dao, Object[] values) {
        return dml(dao, "update", values);
    }

    @Override
    public KeyHolder delete(SqlAware dao, Object[] values) {
        return dml(dao, "delete", values);
    }

    @Override
    public KeyHolder inserts(SqlAware dao, String SqlCode, Object[] values) throws Exception {
        MDC.put("action", "inserts");
        try {
            return dml(dao, SqlCode, values);
        }
        catch (Exception e) {
            for (int i =0; i<values.length; i++){
                logger.info(" values("+ i + ")=" + values[i]);
            };
            logger.error("inserts.exception = " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public KeyHolder inserts(String SqlPath, Object[] values) {
        MDC.put("action", "inserts2");
        String sql="";
        try {
            sql = getSqlAbs(SqlPath);
            logger.info("SqlPath=" + SqlPath +", sql=" + sql);
            GeneratedKeyHolder kh = new GeneratedKeyHolder();
            jdbctempl.update(new DMLPrepparedStatement(sql, values), kh);
            return kh;
        }
        catch (Exception e) {
            for (int i =0; i<values.length; i++){
                logger.info(" values("+ i + ")=" + values[i]);
            };
            logger.error("inserts.exception = " + e.getMessage(), e);
            throw null;
        }
    }

    @Override
    public KeyHolder updates(SqlAware dao, String SqlCode, Object[] values) {
        return dml(dao, SqlCode, values);
    }

    @Override
    public KeyHolder updates(String SqlPath, Object[] values) {
        MDC.put("action", "updates2");
        String sql="";
        try {
            sql = getSqlAbs(SqlPath);
            GeneratedKeyHolder kh = new GeneratedKeyHolder();
            jdbctempl.update(new DMLPrepparedStatement(sql, values), kh);
            return kh;
        }
        catch (Exception e) {
            logger.info(sql);
            for (int i =0; i<values.length; i++){
                logger.info(" values("+ i + ")=" + values[i]);
            };
            logger.error("updates2.exception = " + e.getMessage(), e);
            throw null;
        }
    }

    @Override
    public KeyHolder deletes(String SqlPath, Object[] values) {
        MDC.put("action", "deletes");
        String sql="";
        try {
            sql = getSqlAbs(SqlPath);
            GeneratedKeyHolder kh = new GeneratedKeyHolder();
            jdbctempl.update(new DMLPrepparedStatement(sql, values), kh);
            return kh;
        }
        catch (Exception e) {
            logger.info(sql);
            for (int i =0; i<values.length; i++){
                logger.info(" values("+ i + ")=" + values[i]);
            };
            logger.error("deletes.exception = " + e.getMessage(), e);
            throw null;
        }

        //return dml(dao, SqlCode, values);
    }	
	
    @Override
    public int[] batchDML(SqlAware dao, String sqlCode, List<Object[]> values) {
        String sql = getSql(dao, sqlCode);
        return jdbctempl.batchUpdate(sql, new DMLBatchPreparedStatementSetter(values));
    }

    @Override
    public int[] batchInsert(SqlAware dao, List<Object[]> values) {
        return batchDML(dao, "insert", values);
    }

    @Override
    public int[] batchUpdate(SqlAware dao, List<Object[]> values) {
        return batchDML(dao, "update", values);
    }

    @Override
    public int[] batchDelete(SqlAware dao, List<Object[]> values) {
        return batchDML(dao, "delete", values);
    }

    public Long seqNextval(String sequence) {
        String sql = "select nextval('auction." + sequence + "')";
        return jdbctempl.queryForObject(sql, Long.class);
    }

    private static class DMLPrepparedStatement implements PreparedStatementCreator {

        private final String sql;
        private final Object[] values;

        DMLPrepparedStatement(String sql, Object[] values) {
            this.sql = sql;
            this.values = values;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) {
                Object val = values[i];
                if (val instanceof Date && !(val instanceof Timestamp)) {
                    Date d = (Date) val;
                    ps.setTimestamp(i + 1, new Timestamp(d.getTime()));
                } else if (val instanceof String[]) {
                    Array valArray = con.createArrayOf("text", (String[]) val);
                    ps.setArray(i + 1, valArray);
                } else
                    ps.setObject(i + 1, val);
            }
            return ps;
        }
    }

    private static class DMLBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
        private final List<Object[]> values;

        DMLBatchPreparedStatementSetter(List<Object[]> values) {
            this.values = values;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            Object[] currvals = values.get(i);
            for (int k = 0; k < currvals.length; k++) {
                Object val = currvals[k];
                if (val instanceof Date && !(val instanceof Timestamp)) {
                    Date d = (Date) val;
                    ps.setTimestamp(k + 1, new Timestamp(d.getTime()));
                } else if (val instanceof String[]) {
                    Array valArray = ps.getConnection().createArrayOf("text", (String[]) val);
                    ps.setArray(k + 1, valArray);
                } else
                    ps.setObject(k + 1, val);
            }
        }

        @Override
        public int getBatchSize() {
            return values.size();
        }
    }
}
