package com.bas.auction.auth.dao.impl;

import com.bas.auction.auth.dao.UserDAO;
import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


@Repository
public class UserDAOImpl implements UserDAO, GenericDAO<User> {
    private final DaoJdbcUtil daoutil;
    private final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);


    @Autowired
    public UserDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "user";
    }

    @Override
    public Class<User> getEntityType() {
        return User.class;
    }

    @Override
    public String findPasswordHashById(long userId) {
        return daoutil.queryScalar(this, "get_password_hash_by_id", userId);
    }

    @Override
    public String findPasswordHashByEmail(String email) {
        return daoutil.queryScalar(this, "get_password_hash_by_email", email);
    }


    @Override
    public String findPasswordHashByLogin(String login) {
        return daoutil.queryScalar(this, "get_password_hash_by_login", login);
    }

    @Override
    public User findById(Long userId) {
        return daoutil.queryForObject(this, "get", userId);
    }

    @Override
    public List<User> findBySupplierId(Long supplierId) {
        return daoutil.query(this, "get_by_supplier_id", supplierId);
    }

    @Override
    public boolean userLoginExists(String login) {
        return daoutil.exists(this, "user_exists", login);
    }

    public List<Map<String, Object>>  getSupplEmail(Long userId) {
        return daoutil.queryForMapList(this, "supplier_email", userId);
    }

    public List<Map<String, Object>>  getSupplEmailByRelId(Long relId) {
        List<Map<String, Object>> kskEmail = null;
        kskEmail = daoutil.queryForMapList(this, "supplier_email_by_rel_id", relId);
        if(kskEmail!=null && !kskEmail.isEmpty() ) {
            return kskEmail;
        }else {
            return daoutil.queryForMapList(this, "supplier_email_by_rel_id", relId);
        }
    }

    public List<Map<String, Object>>  getSupplEmail2(Long userId) {
        return daoutil.queryForMapList(this, "supplier_email2", userId);
    }

    public List<Map<String, Object>>  getKskByUserId(Long userId) {
        return daoutil.queryForMapList(this, "get_ksk_by_user_id", userId);
    }

    public List<Map<String, Object>> getRelIdByUserParams(Map<String, Object> params){
        return daoutil.queryForMapListParams(this, "get_rel_id_by_user_params", params);
    };

    public List<Map<String, Object>> getKskByUserParams(Map<String, Object> params){
        return daoutil.queryForMapListParams(this, "get_ksk_by_user_params", params);
    };

    public  List<Map<String, Object>> getKskEmail(Long userId, Object relId) {
        return daoutil.queryForMapList(this, "ksk_email",relId, userId);
    }

    public String getSupplAddress(Long userId, Long relId) {
        if (relId!=null){
            logger.debug("get_address_by_relid kirdi");
            return daoutil.queryScalar(this, "get_address_by_relid", relId);
        }else {
            logger.debug("supplier_address kirdi");
        return daoutil.queryScalar(this, "supplier_address", userId);
    }
    }



    @Override
    public int findIsActive(Long userId) {
        return daoutil.queryScalar(this, "is_active", userId);
    }

    @Override
    public boolean findIsMainUser(Long userId) {
        return daoutil.queryScalar(this, "is_main_user", userId);
    }

    @Override
    public Entry<Long, Boolean> findIdAndIsActiveByLogin(String login) {
        Map<String, Object> map = daoutil.queryForMap(this, "get_is_active_userid_by_login", login);
        return new SimpleEntry<>((Long) map.get("user_id"), (Boolean) map.get("active"));
    }

    @Override
    public Entry<String, String> findUserIinAndBin(Long userId) {
        Map<String, Object> userIin = daoutil.queryForMap(this, "get_user_iin", userId);
        String iin = (String) userIin.get("iin");
        Long customerId = (Long) userIin.get("customer_id");
        Long supplierId = (Long) userIin.get("supplier_id");
        String bin = null;
        if (customerId != null)
            bin = findCustomerUserBin(customerId);
        else if (supplierId != null)
            bin = findSupplierUserBin(supplierId);
        return new SimpleEntry<>(iin, bin);
    }

    private String findCustomerUserBin(Long customerId) {
        return daoutil.queryScalar(this, "get_user_customer_bin", customerId);
    }

    private String findSupplierUserBin(Long supplierId) {
        return daoutil.queryScalar(this, "get_user_supplier_bin", supplierId);
    }

    public String getUserName(Long userId) {
        return daoutil.queryScalar(this, "get_user_name", userId);
    }

    @Override
    @SpringTransactional
    public String changePassword(Long userId, String newPassword) {
        //Object[] params = {newPassword, userId, userId};  --2016_08_16
        Object[] params = {newPassword, userId};
        KeyHolder kh = daoutil.dml(this, "update_password_by_id", params);
        return (String) kh.getKeys().get("usermail1");
    }


    @SpringTransactional
    public void activateEmail(String userEmail) {
        Object[] params = {userEmail};
        logger.debug("email activation 2 step {}"+userEmail);
        daoutil.dml(this, "activate_email", params);
    }

    @Override
    @SpringTransactional
    public String resetPassword(String userEmail, String newPassword) {
        Object[] params = {newPassword, userEmail};
        KeyHolder kh = daoutil.dml(this, "update_password", params);
        return (String) kh.getKeys().get("email");
    }

    @Override
    public Map<String, Object> findCustomerUserInfoForEmailNotif(String role) {
        return daoutil.queryForMap(this, "cust_user_info_for_email_notif", role);
    }

    @Override
    public Map<String, Object> findSupplierUserInfoForEmailNotif(String iin, String bin) {
        return daoutil.queryForMap(this, "sup_user_info_for_email_notif", iin, bin);
    }

    @Override
    public List<Map<String, Object>> findEmailNotActivatedUserInfo(String iin, String bin) {
        return daoutil.queryForMapList(this, "email_not_activated_users_info_for_notif", iin, bin, iin, bin);
    }

    @Override
    public long findUserIdByEmail(String login) {
        return daoutil.queryScalar(this,"findUserIdByEmail",login);
    }


    @Override
    public List<String> findMainUserEmailsByIin(String bin) {
        return daoutil.queryScalarList(this, "get_main_user_emails_by_bin", bin, bin);
    }

    @Override
    public Long nonResidentSupplierUserSeqNextVal() {
        return daoutil.seqNextval("non_resident_user_seq");
    }

    @Override
    @SpringTransactional
    public void disableMainUserFlag(User user, User data) {
        if (data.getCustomerId() != null) {
            Object[] params = {user.getUserId(), data.getCustomerId()};
            daoutil.dml(this, "disable_cust_main_user_flag", params);
        }
        if (data.getSupplierId() != null) {
            Object[] params = {user.getUserId(), data.getSupplierId()};
            daoutil.dml(this, "disable_supp_main_user_flag", params);
        }
    }

    @Override
    @SpringTransactional
    public User insert(User user, User data) {
        logger.debug("parameterrrrrrrrrrrrr={}1111155555");
        Date d = new Date();
        String fract="null", flatSubUnit="null";

      logger.debug("data.getEmail()="+data.getEmail()+" data.getName()="+data.getName()+
                " data.getSurname()="+data.getSurname()+" data.getMidname()="+data.getMidname()+" data.getBirthday()="+ data.getBirthday()+
                "  data.getIin()"+data.getIin()+" data.getMobilePhone()="+data.getMobilePhone()+" data.getPhoneNumber()="+data.getPhoneNumber());
                int actSt;
                if (data.getSntrue()!= null){
                    actSt=1;
                }else{
                    actSt=0;
                }

        Long snId = null;
        if (data.getSntrue()!= null) {
            Object[] values1 = {
                    data.getSntrue(),
                    data.getEmail(),
                    data.getFbpass()

            };
            logger.debug("data.getSntrue()==="+data.getSntrue()+" data.getEmail()="+data.getEmail()+" data.getFbpass()="+data.getFbpass());
            KeyHolder kh1 = daoutil.insert_for_reg_sn(this, values1);
            snId=(Long) kh1.getKeys().get("id");
        }

         Map <String, Object> snpass= daoutil.queryForMap(this, "get_t_user_sn_pass", data.getEmail());
        logger.debug("kakakak");
        if (data.getSntrue()!= null &&  snpass!=null ){
                if  (snpass.get("sn_pass")==null ) {
                    Object[] values2 = {snId, data.getEmail()};
                    KeyHolder kh2 = daoutil.updates(this, "update_sn_id", values2);
                }
        }else{
           
                Object[] values = {data.getEmail(),
                           data.getPassword(),
                    actSt,
                           data.getName(),
                           data.getSurname(),
                           data.getMidname(),
                           data.getBirthday(),
                           data.getIin(),
                           data.getMobilePhone(),
                           data.getPhoneNumber()    ,
                           1,
                    d,
                    snId
                };

        KeyHolder kh = daoutil.insert_for_reg(this, values);
            logger.debug("(String) kh.getKeys().get(usermail1)"+(String) kh.getKeys().get("usermail1")+ (Long) kh.getKeys().get("id"));
            data.setUserId((Long) kh.getKeys().get("id"));
            data.setEmail((String) kh.getKeys().get("usermail1"));
            String name=(((String) kh.getKeys().get("lastname"))+" "+((String) kh.getKeys().get("firstname"))+((((String) kh.getKeys().get("middlename")) == null)?"":(" "+((String) kh.getKeys().get("middlename")))));
            data.setName(name);
        }



/* for case when reg form has information about user address
      logger.debug(" data.getRelation()="+ data.getRelation()+"data.getStreet()="+data.getStreet()+"data.getCity()="+data.getCity()+"data.getFlat()="+data.getFlat()+" data.getHome()="+data.getHome()+" fract="+fract+" flatSubUnit"+flatSubUnit
                +" data.getFlat()="+data.getFlat()+" data.getFraction()="+data.getFraction() +" data.getFractflat()="+data.getFractflat()+"data.getUserId()"+data.getUserId()+" user.id="+kh.getKeys().get("id") );
          Object[] valuesForFlat = {
                    data.getCity(),
                    data.getStreet(),
                    data.getHome(),
                    data.getFraction(),
                    data.getFlat(),
                    data.getFractflat(),
                    data.getRelation(),
                  kh.getKeys().get("id")
          };

            KeyHolder kh1 = daoutil.updates(this,"insert_flat", valuesForFlat);*/

          //  Object[] valuesForRelation={kh.getKeys().get("id"),kh1.getKeys().get("id")
           // };
           // KeyHolder kh2 = daoutil.insertRelation(this, valuesForRelation);


        return data;
    }



    @SpringTransactional
    public void updateRelation(Long userId) {

        logger.debug("updateRelation userId="+userId);
        Object[] valuesForFlat = {
                userId
        };

        KeyHolder kh1 = daoutil.updates(this, "update_relation", valuesForFlat);

    }


    @SpringTransactional
    public void activateRelation(Long userId, Object status, Object status1, Object rel_id, String sqlPath) {

        logger.debug("activateRelation userId="+userId+" rel_id="+rel_id+ " sqlPath="+sqlPath+ " status1="+status.toString() );
        Object[] valuesForFlat = {
                status,
                status1,
                rel_id,
                userId
        };

        KeyHolder kh1 = daoutil.updates(this, sqlPath, valuesForFlat);
    }


    @Override
    @SpringTransactional
    public User update(User user, User data) {
        Object[] values = { data.getName(), data.getSurname(), data.getMobilePhone(), data.getUserId()};
        daoutil.update(this, values);
        return data;
    }

    @Override
    @SpringTransactional
    public User updatePersonal(User user, User data) {
        Object[] values = {data.getEmail(), data.getPhoneNumber(), data.getUserPosition(), user.getUserId(),
                data.getUserId()};
        daoutil.dml(this, "update_personal", values);
        return data;
    }
}
