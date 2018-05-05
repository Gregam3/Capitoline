//package com.greg.dao.userholding;
//
//import com.greg.dao.AbstractDaoImpl;
//import com.greg.entity.user.UserHolding;
//import org.springframework.stereotype.Repository;
//
//import org.springframework.transaction.annotation.Transactional;
//import java.util.List;
//
///**
// * @author Greg Mitten (i7676925)
// * gregoryamitten@gmail.com
// */
//@Transactional
//@Repository
//public class UserHoldingDaoImpl extends AbstractDaoImpl<UserHolding> implements UserHoldingDao{
//    public void delete(UserHolding userHolding) {
//        entityManager.remove(userHolding);
//    }
//
//    @Override
//    public List list() {
//        throw new AssertionError("Please get user instead of attempting to list all holdings");
//    }
//}