package com.paymentic.domain.payment.key;

import com.paymentic.domain.payment.PaymentType;

public class PaymentByTypeKey {
  private final PaymentType type;
  private final String date;
  public PaymentByTypeKey(PaymentType type, String date) {
    this.type = type;
    this.date = date;
  }
  public String getDate() {
    return date;
  }
  public PaymentType getType() {
    return type;
  }
  public static PaymentByTypeKey newPaymentByTypeKey(PaymentType type, String date) {
    return new PaymentByTypeKey(type, date);
  }

}
