//package com.greg.service.transaction;
//
//import com.greg.dao.transaction.TransactionDao;
//import com.greg.entity.user.Transaction;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * @author Greg Mitten (i7676925)
// * gregoryamitten@gmail.com
// */
//@Service
//public class TransactionService {
//    private TransactionDao transactionDao;
//
//    @Autowired
//    public TransactionService(TransactionDao transactionDao) {
//        this.transactionDao = transactionDao;
//    }
//
//    public void deleteAll(List<Transaction> transactions) {
//        for (Transaction transaction : transactions) {
//            transactionDao.delete(transaction);
//        }
//    }
//}
