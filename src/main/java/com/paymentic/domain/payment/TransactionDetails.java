package com.paymentic.domain.payment;

import com.paymentic.domain.Buyer;
import java.util.UUID;

public class TransactionDetails {
  private Transaction transaction;
  private Seller seller;
  private Payment payment;
  private CheckoutId checkoutId;
  private String refundId;
  private String amount;
  private String currency;
  private String at;
  private Buyer buyer;
  private String status;
  public TransactionDetails() {
  }
  public Transaction getTransaction() {
    return transaction;
  }

  public Seller getSeller() {
    return seller;
  }

  public Payment getPayment() {
    return payment;
  }

  public CheckoutId getCheckoutId() {
    return checkoutId;
  }

  public String getRefundId() {
    return refundId;
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

  public Buyer getBuyer() {
    return buyer;
  }

  public String getStatus() {
    return status;
  }
}
