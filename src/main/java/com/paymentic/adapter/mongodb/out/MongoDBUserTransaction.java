package com.paymentic.adapter.mongodb.out;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.paymentic.domain.AverageTransactionsValue;
import com.paymentic.domain.UserTransaction;
import com.paymentic.domain.repositories.UserTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import org.bson.Document;
import org.bson.conversions.Bson;

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

}
