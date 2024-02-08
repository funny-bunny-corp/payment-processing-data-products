package com.paymentic.domain.payment;

public class PaymentOrderRequest {
  private String id;
  private Seller sellerInfo;
  private String amount;
  private String currency;
  private String at;
  public PaymentOrderRequest() {
  }
  public String getId() {
    return id;
  }
  public Seller getSellerInfo() {
    return sellerInfo;
  }
  public String getAmount() {
    return amount;
  }
  public String getCurrency() {
    return currency;
  }
  public String getAt() {
    return at;
  }
}

