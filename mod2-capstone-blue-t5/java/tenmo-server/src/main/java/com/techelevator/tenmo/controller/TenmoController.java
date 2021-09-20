package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferRequest;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api")

public class TenmoController {

    @Autowired
    private TransferDao transferDao;
    @Autowired
    private UserDao userDao;

    @RequestMapping (path = "/balance", method = RequestMethod.GET)
    public BigDecimal viewCurrentBalance (Principal principal){
        return transferDao.viewCurrentBalance(principal);
    }

    @RequestMapping (path = "/transfer/history", method = RequestMethod.GET)
    public List<Transfer> viewTransferHistory (Principal principal){
        return transferDao.viewTransferHistory(principal);
    }

    @RequestMapping (path = "/transfer", method = RequestMethod.POST)
    public String sendBucks (@RequestBody TransferRequest transferRequest, Principal principal){
        BigDecimal amountDec = new BigDecimal(transferRequest.getAmount().toString());
        Integer recipientUserID = transferRequest.getRecipientUserID();
        boolean result = transferDao.sendBucks(principal, recipientUserID, amountDec);

        if (result){
            return "Transfer Completed";
        } else {
            return "Transfer Failed. Insufficient Funds";
        }
    }

    @RequestMapping (path = "/users", method = RequestMethod.GET)
    public List<User> findAll () {
        return userDao.findAll();
    }


}
