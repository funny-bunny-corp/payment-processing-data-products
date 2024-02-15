package com.paymentic.adapter.mongodb.out;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.paymentic.domain.AverageTransactionsValue;
import com.paymentic.domain.LastUserTransaction;
import com.paymentic.domain.UserTransaction;
import com.paymentic.domain.repositories.UserTransactionRepository;
import com.paymentic.infra.mongodb.qualifiers.LastUserTransactionsCollection;
import com.paymentic.infra.mongodb.qualifiers.UserTransactionsCollection;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Arrays;
import org.bson.Document;
import org.bson.conversions.Bson;

@ApplicationScoped
public class MongoDBUserTransaction implements UserTransactionRepository {
  private final MongoCollection userTransactionCollection;
  private final MongoCollection lastUserTransactionCollection;
  public MongoDBUserTransaction(@UserTransactionsCollection MongoCollection userTransactionCollection,
      @LastUserTransactionsCollection MongoCollection lastUserTransactionCollection) {
    this.userTransactionCollection = userTransactionCollection;
    this.lastUserTransactionCollection = lastUserTransactionCollection;
  }
  @Override
  public void add(UserTransaction transaction) {
    this.userTransactionCollection.insertOne(document(transaction));
    this.lastUserTransactionCollection.replaceOne(Filters.eq("document", transaction.getDocument()),document(new LastUserTransaction(transaction.getDocument(),transaction.getValue(), transaction.getCurrency(), transaction.getSeller().getSellerId())), new ReplaceOptions().upsert(true));
  }
  @Override
  public AverageTransactionsValue totalPerMonth(String document, LocalDate at){
    AggregateIterable<Document> aggregate = this.userTransactionCollection.aggregate(Arrays.asList(
        new Document("$addFields", new Document("year", new Document("$year", "$at"))
            .append("month", new Document("$month", "$at"))),
        new Document("$match", new Document("year", at.getYear())
            .append("month", at.getMonth().getValue())).append("metadata.document",document),
        new Document("$group", new Document("_id", "$metadata.document")
            .append("totalValue", new Document("$sum", "$value"))),
        new Document("$sort", new Document("_id", 1))
    ));
    return null;
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
  private Document document(LastUserTransaction transaction) {
    var doc = new Document()
        .append("document", transaction.getDocument())
        .append("seller_id", transaction.getSellerId())
        .append("value", transaction.getValue())
        .append("currency", transaction.getCurrency());
    return doc;
  }

}
