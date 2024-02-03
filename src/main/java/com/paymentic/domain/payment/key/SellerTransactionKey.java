
package com.paymentic.domain.payment.key;

import java.time.LocalDate;
import java.util.Objects;
import org.apache.kafka.common.protocol.types.Field.Str;

public class SellerTransactionKey {
  private final String sellerId;
  private final String transactionDate;
  private SellerTransactionKey(String sellerId, String transactionDate) {
    this.sellerId = sellerId;
    this.transactionDate = transactionDate;
  }
  public static SellerTransactionKey newSellerTransactionKey(String sellerId, String transactionDate) {
    return new SellerTransactionKey(sellerId, transactionDate);
  }
  public String getTransactionDate() {
    return transactionDate;
  }
  public String getSellerId() {
    return sellerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SellerTransactionKey that = (SellerTransactionKey) o;
    return Objects.equals(sellerId, that.sellerId) && Objects.equals(
        transactionDate, that.transactionDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sellerId, transactionDate);
  }
}
