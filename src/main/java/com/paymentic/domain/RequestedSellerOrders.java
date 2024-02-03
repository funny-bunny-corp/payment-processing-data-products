package com.paymentic.domain;

import com.paymentic.domain.payment.PaymentOrderRequest;
import java.math.BigDecimal;

public class RequestedSellerOrders {
  private String sellerId;
  private BigDecimal amount = BigDecimal.ZERO;
  public RequestedSellerOrders() {
  }
  public RequestedSellerOrders(String sellerId, BigDecimal amount) {
    this.sellerId = sellerId;
    this.amount = amount;
  }
  public RequestedSellerOrders aggregate(PaymentOrderRequest order) {
    return new RequestedSellerOrders(order.getSellerInfo().getSellerId(), this.amount.add(BigDecimal.valueOf(Double.parseDouble(order.getAmount()))));
  }
  public String getSellerId() {
    return sellerId;
  }
  public BigDecimal getAmount() {
    return amount;
  }
}
