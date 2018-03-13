//package com.greg.service.userholding;
//
//import com.greg.dao.userholding.UserHoldingDao;
//import com.greg.entity.user.UserHolding;
//import com.greg.service.transaction.TransactionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// * @author Greg Mitten (i7676925)
// * gregoryamitten@gmail.com
// */
//@Service
//public class UserHoldingService {
//
//    private UserHoldingDao userHoldingDao;
//    private TransactionService transactionService;
//
//    @Autowired
//    public UserHoldingService(UserHoldingDao userHoldingDao, TransactionService transactionService) {
//        this.userHoldingDao = userHoldingDao;
//        this.transactionService = transactionService;
//    }
//
//    public void remove(UserHolding userHolding) {
//        transactionService.deleteAll(userHolding.getTransactions());
//        userHoldingDao.delete(userHolding);
//    }
//}
