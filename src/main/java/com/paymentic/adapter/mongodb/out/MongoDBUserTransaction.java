package com.paymentic.adapter.mongodb.out;

import com.mongodb.client.MongoCollection;
import com.paymentic.domain.UserTransaction;
import com.paymentic.domain.repositories.UserTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;

@ApplicationScoped
public class MongoDBUserTransaction implements UserTransactionRepository {
  private final MongoCollection userTransactionCollection;
  public MongoDBUserTransaction(MongoCollection userTransactionCollection) {
    this.userTransactionCollection = userTransactionCollection;
  }
  @Override
  public void add(UserTransaction transaction) {
    this.userTransactionCollection.insertOne(document(transaction));
  }

  private Document document(UserTransaction transaction) {
    var metadata = new Document()
        .append("transaction_id", transaction.getId())
        .append("document", transaction.getDocument())
        .append("checkout_id", transaction.getCheckout().getId())
        .append("payment_id", transaction.getPayment().getId())
        .append("currency", transaction.getCurrency());
    return new Document().append("metadata", metadata).append("value",transaction.getValue()).append("at",transaction.at());
  }

}
