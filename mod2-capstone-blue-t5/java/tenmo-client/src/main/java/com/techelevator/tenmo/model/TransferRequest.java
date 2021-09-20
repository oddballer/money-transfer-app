package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferRequest {
    Integer recipientUserID;
    BigDecimal amount;

    public TransferRequest(Integer recipientUserID, BigDecimal amount){
        this.recipientUserID = recipientUserID;
        this.amount=amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getRecipientUserID() {
        return recipientUserID;
    }

    public void setRecipientUserID(Integer recipientUserID) {
        this.recipientUserID = recipientUserID;
    }


}
