package com.paymentic.domain.payment;

public enum PaymentType {
  DIGITAL_WALLET("digital-wallet"),
  CREDIT_CARD("credit-card"),
  BANK_TRANSFER("bank-transfer");
  private final String type;
  PaymentType(String type){
    this.type = type;
  }
  public String getType() {
    return type;
  }

}
