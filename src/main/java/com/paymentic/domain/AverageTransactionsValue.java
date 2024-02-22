package com.paymentic.domain;

import java.math.BigDecimal;
import org.apache.kafka.common.protocol.types.Field.Str;

public class AverageTransactionsValue {
  private String document;
  private BigDecimal average;
  public String getDocument() {
    return document;
  }
  public BigDecimal getAverage() {
    return average;
  }
  public AverageTransactionsValue() {
  }
  public AverageTransactionsValue(String document, BigDecimal average) {
    this.document = document;
    this.average = average;
  }
  public static AverageTransactionsValue defaultAverage(String document) {
    return new AverageTransactionsValue(document, new BigDecimal("200.00"));
  }
}
