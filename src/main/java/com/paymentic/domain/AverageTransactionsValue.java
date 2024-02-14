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
}
