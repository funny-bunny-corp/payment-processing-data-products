package com.paymentic.domain;

import com.paymentic.domain.payment.CheckoutId;
import com.paymentic.domain.payment.Payment;
import java.math.BigDecimal;
import org.apache.kafka.common.protocol.types.Field.Str;

public class LastUserTransaction {

  private String document;

  private BigDecimal value;

  private String currency;

  private String sellerId;

  public LastUserTransaction() {
  }

  public LastUserTransaction(String document, BigDecimal value, String currency, String sellerId) {
    this.document = document;
    this.value = value;
    this.currency = currency;
    this.sellerId = sellerId;
  }

  public String getDocument() {
    return this.document;
  }

  public BigDecimal getValue() {
    return this.value;
  }

  public String getCurrency() {
    return this.currency;
  }

  public String getSellerId() {
    return sellerId;
  }

}
