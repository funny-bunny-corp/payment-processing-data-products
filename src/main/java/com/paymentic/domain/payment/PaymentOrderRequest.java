package com.paymentic.domain.payment;

import java.time.LocalDate;
import org.apache.kafka.common.protocol.types.Field.Str;

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

