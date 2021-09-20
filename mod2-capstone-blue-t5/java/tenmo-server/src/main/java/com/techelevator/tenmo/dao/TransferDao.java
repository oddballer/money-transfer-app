package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public interface TransferDao {

    BigDecimal viewCurrentBalance(Principal principal);

    List<Transfer> viewTransferHistory(Principal principal);

    Transfer getTransfer(int accountID);

    List<Transfer> viewPendingRequests(Principal principal);

    boolean pendingAcceptReject(int statusID, int transferID, BigDecimal amount, int accountFrom, int accountTo);

    boolean sendBucks(Principal principal, Integer receiveID, BigDecimal amount);

    boolean requestBucks(int sendID, int receiveID);


}
