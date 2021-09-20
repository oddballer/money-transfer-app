package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferRequest {

    BigDecimal amount;
    Integer recipientUserID;

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
