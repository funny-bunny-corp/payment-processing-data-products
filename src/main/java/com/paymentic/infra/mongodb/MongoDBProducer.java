package com.paymentic.infra.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.TimeSeriesOptions;
import com.paymentic.infra.mongodb.qualifiers.LastUserTransactionsCollection;
import com.paymentic.infra.mongodb.qualifiers.UserTransactionsCollection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class MongoDBProducer {
  @Produces @UserTransactionsCollection
  public MongoCollection userTransactionCollection(MongoClient mongoClient){
    MongoDatabase database = mongoClient.getDatabase("payment-processing");
    TimeSeriesOptions tsOptions = new TimeSeriesOptions("at");
    CreateCollectionOptions collectionOptions = new CreateCollectionOptions().timeSeriesOptions(tsOptions);
    if(mongoClient.getDatabase("payment-processing").getCollection("user_transactions") == null){
      database.createCollection("user_transactions", collectionOptions);
    }
    return mongoClient.getDatabase("payment-processing").getCollection("user_transactions");
  }
  @Produces @LastUserTransactionsCollection
  public MongoCollection lastUserTransactionCollection(MongoClient mongoClient){
    MongoDatabase database = mongoClient.getDatabase("payment-processing");
    CreateCollectionOptions collectionOptions = new CreateCollectionOptions();
    database.createCollection("last_user_transaction", collectionOptions);
    var collection = mongoClient.getDatabase("payment-processing").getCollection("last_user_transaction");
    IndexOptions indexOptions = new IndexOptions().unique(true);
    collection.createIndex(Indexes.descending("document"), indexOptions);
    return collection;
  }

}
