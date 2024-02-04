package com.paymentic.domain.topologies;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.Topology;

@ApplicationScoped
public class PaymentsByBuyerTopology {
  private final static String SOURCE_TOPIC = "payment-processing";
  private final static String BY_SELLER_REQUEST = "payment-requested-by-seller";
  private final static String EVENT_TYPE = "paymentic.io.payment-processing.v1.data.product.payment-by-type";
  private final static String SOURCE_TYPE = "paymentic-processing-data-products";
  private final static String SUBJECT = "total-payments-by-type";
  private final static String PAYMENT_ORDER_APPROVED = "paymentic.io.payment-processing.v1.payment-order.approved";

  @Produces
  public Topology paymentsByType(Serde<CloudEvent> ceSerde, ObjectMapper mapper) {



    return null;
  }

}
