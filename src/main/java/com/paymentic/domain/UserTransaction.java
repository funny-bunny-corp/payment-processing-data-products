package com.paymentic.domain;

import com.paymentic.domain.payment.CheckoutId;
import com.paymentic.domain.payment.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.kafka.common.protocol.types.Field.Str;

public class UserTransaction {
  private String id;
  private String document;
  private BigDecimal value;
  private CheckoutId checkout;
  private Payment payment;
  private String currency;
  private String at;
  public UserTransaction() {
  }
  public UserTransaction(String id, String document, BigDecimal value, CheckoutId checkout,
      Payment payment, String currency, String at) {
    this.id = id;
    this.document = document;
    this.value = value;
    this.checkout = checkout;
    this.payment = payment;
    this.currency = currency;
    this.at = at;
  }
  public String getId() {
    return this.id;
  }
  public String getDocument() {
    return this.document;
  }
  public BigDecimal getValue() {
    return this.value;
  }
  public CheckoutId getCheckout() {
    return this.checkout;
  }
  public Payment getPayment() {
    return this.payment;
  }
  public String getCurrency() {
    return this.currency;
  }
  public String getAt() {
    return at;
  }
  public LocalDateTime at(){
    return LocalDateTime.parse(this.at);
  }
}
