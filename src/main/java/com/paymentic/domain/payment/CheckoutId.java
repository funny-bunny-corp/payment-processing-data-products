package com.paymentic.domain.payment;

import java.util.UUID;

public class CheckoutId {
  private UUID id;
  public CheckoutId() {
  }
  public CheckoutId(UUID id) {
    this.id = id;
  }
  public UUID getId() {
    return id;
  }
}
