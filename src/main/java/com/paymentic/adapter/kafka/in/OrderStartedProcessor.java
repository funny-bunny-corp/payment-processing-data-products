package com.paymentic.adapter.kafka.in;

import com.paymentic.domain.UserTransaction;
import com.paymentic.domain.payment.CheckoutId;
import com.paymentic.domain.payment.TransactionDetails;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.ce.IncomingCloudEventMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletionStage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OrderStartedProcessor {
  private static final String PAYMENT_ORDER_APPROVED_EVENT_TYPE = "paymentic.io.payment-processing.v1.payment-order.approved";
  private static final Logger LOGGER = Logger.getLogger(OrderStartedProcessor.class);

  private final Event<UserTransaction> trigger;
  public OrderStartedProcessor(Event<UserTransaction> trigger) {
    this.trigger = trigger;
  }
  @Blocking
  @Incoming("order-started")
  public CompletionStage<Void> process(Message<TransactionDetails> message) {
    var event = message.getMetadata(IncomingCloudEventMetadata.class)
        .orElseThrow(() -> new IllegalArgumentException("Expected a Cloud Event"));
    var transactionDetails = message.getPayload();
    if (PAYMENT_ORDER_APPROVED_EVENT_TYPE.equals(event.getType())) {
      LOGGER.info(String.format("Receiving payment order approved event. Transaction-Id %s Event-Id %s. Start processing....",
          transactionDetails.getTransaction().getId(), event.getId()));
      this.trigger.fire(new UserTransaction(transactionDetails.getTransaction().getId().toString(), transactionDetails.getBuyer().getDocument(),
          new BigDecimal(transactionDetails.getAmount()), transactionDetails.getCheckoutId(),transactionDetails.getPayment(),transactionDetails.getCurrency(),
          transactionDetails.getAt(),transactionDetails.getSeller()));
    }
    return message.ack();
  }

}
