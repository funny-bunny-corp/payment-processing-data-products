package com.paymentic.domain.payment.key;

public class PaymentsByBuyerKey {
  private final String buyerDocument;
  private final String month;
  public PaymentsByBuyerKey(String buyerDocument, String month) {
    this.buyerDocument = buyerDocument;
    this.month = month;
  }
  public String getBuyerDocument() {
    return buyerDocument;
  }
  public String getMonth() {
    return month;
  }
}
