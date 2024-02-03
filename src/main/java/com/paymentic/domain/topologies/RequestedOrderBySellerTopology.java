package com.paymentic.domain.topologies;

import static io.cloudevents.core.CloudEventUtils.mapData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentic.domain.RequestedSellerOrders;
import com.paymentic.domain.payment.PaymentOrderRequest;
import com.paymentic.domain.payment.key.SellerTransactionKey;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.net.URI;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;

@ApplicationScoped
public class RequestedOrderBySellerTopology {
  private final static String SOURCE_TOPIC = "payment-processing";
  private final static String BY_SELLER_REQUEST = "payment-requested-by-seller";
  private final static String EVENT_TYPE = "paymentic.io.payment-processing.v1.data.product.sellers-requested-payments-total";
  private final static String SOURCE_TYPE = "paymentic-processing-data-products";
  private final static String SUBJECT = "new-total-requested-payments-by-seller";
  private final static String PAYMENT_REQUESTED_CREATED = "paymentic.io.payment-processing.v1.payment-order.started";

  @Produces
  public Topology totalOrderSellerPerDay(Serde<CloudEvent> ceSerde, ObjectMapper mapper) {
    final ObjectMapperSerde<PaymentOrderRequest> paymentOrderSerde = new ObjectMapperSerde<>(
        PaymentOrderRequest.class);
    final ObjectMapperSerde<SellerTransactionKey> sellerTransactionKeySerde = new ObjectMapperSerde<>(
        SellerTransactionKey.class);
    final ObjectMapperSerde<RequestedSellerOrders> requestedSellerOrdersSerde = new ObjectMapperSerde<>(
        RequestedSellerOrders.class);
    final Initializer<RequestedSellerOrders> initializer = RequestedSellerOrders::new;

    KeyValueBytesStoreSupplier requestedOrdersBySeller = Stores.persistentKeyValueStore(
        "REQUESTED_ORDERS_BY_SELLER");

    var builder = new StreamsBuilder();
    final KStream<String, CloudEvent> cePaymentOrdersStream = builder.stream(SOURCE_TOPIC,
        Consumed.with(Serdes.String(), ceSerde));

    final Aggregator<SellerTransactionKey, PaymentOrderRequest, RequestedSellerOrders> bySellerAggregator = (key, paymentOrder, sellerOrders) -> sellerOrders.aggregate(
        paymentOrder);

    final Predicate<String, CloudEvent> isPaymentOrderRequest = (key, value) -> PAYMENT_REQUESTED_CREATED.equals(value.getType());

    cePaymentOrdersStream.filter(isPaymentOrderRequest).map(((key, value) -> {
          var ce = mapData(value, PojoCloudEventDataMapper.from(mapper, PaymentOrderRequest.class));
          PaymentOrderRequest request = ce.getValue();
          return KeyValue.pair(
              SellerTransactionKey.newSellerTransactionKey(request.getSellerInfo().getSellerId(), request.getAt()),
              request);
        }))
        .groupByKey(Grouped.with(sellerTransactionKeySerde, paymentOrderSerde))
        .aggregate(initializer, bySellerAggregator,
            Materialized.<SellerTransactionKey, RequestedSellerOrders>as(requestedOrdersBySeller)
                .withKeySerde(sellerTransactionKeySerde)
                .withValueSerde(requestedSellerOrdersSerde)).toStream().map(((key, value) -> {
          CloudEvent event = CloudEventBuilder.v1()
              .withId(UUID.randomUUID().toString())
              .withType(EVENT_TYPE)
              .withSource(URI.create(SOURCE_TYPE))
              .withSubject(SUBJECT)
              .withDataContentType("application/json")
              .withData(PojoCloudEventData.wrap(value, mapper::writeValueAsBytes))
              .build();
          return KeyValue.pair(event.getId(), event);
        })).to(BY_SELLER_REQUEST, Produced.with(Serdes.String(), ceSerde));
    return builder.build();
  }
}
