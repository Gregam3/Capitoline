//package com.greg.dao.transaction;
//
//import com.greg.dao.AbstractDaoImpl;
//import com.greg.entity.user.Transaction;
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
//public class TransactionDaoImpl extends AbstractDaoImpl<Transaction> implements TransactionDao {
//
//    @Override
//    public void delete(Transaction transaction) {
//        entityManager.remove(transaction);
//    }
//
//    @Override
//    public List list() {
//        throw new AssertionError("Please get user instead");
//    }
//
//    @Override
//    public void update(Transaction transaction) {
//        throw new AssertionError("Please update through user");
//    }
//}
