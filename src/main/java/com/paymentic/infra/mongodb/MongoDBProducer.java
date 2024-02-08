package com.paymentic.infra.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class MongoDBProducer {
  @Produces
  public MongoCollection createCollection(MongoClient mongoClient){
    MongoDatabase database = mongoClient.getDatabase("payment-processing");
    TimeSeriesOptions tsOptions = new TimeSeriesOptions("at");
    CreateCollectionOptions collectionOptions = new CreateCollectionOptions().timeSeriesOptions(tsOptions);
    if(mongoClient.getDatabase("payment-processing").getCollection("user_transactions") == null){
      database.createCollection("user_transactions", collectionOptions);
    }
    return mongoClient.getDatabase("payment-processing").getCollection("user_transactions");
  }


}
