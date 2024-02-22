package com.paymentic.adapter.grpc.in;

import com.paymentic.adapter.in.grpc.LastUserTransactionRequest;
import com.paymentic.adapter.in.grpc.LastUserTransactionResponse;
import com.paymentic.adapter.in.grpc.UserMonthAverageRequest;
import com.paymentic.adapter.in.grpc.UserMonthAverageResponse;
import com.paymentic.adapter.in.grpc.UserTransactionsService;
import com.paymentic.domain.AverageTransactionsValue;
import com.paymentic.domain.application.UserTransactionService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import java.time.LocalDate;
import java.util.Objects;

@GrpcService
public class GrpcUserTransactionsService implements UserTransactionsService {
  private final UserTransactionService userTransactionService;
  public GrpcUserTransactionsService(UserTransactionService userTransactionService) {
    this.userTransactionService = userTransactionService;
  }
  @Override
  public Uni<UserMonthAverageResponse> getUserMonthAverage(UserMonthAverageRequest request) {
    var at = LocalDate.parse(request.getMonth());
    AverageTransactionsValue average = this.userTransactionService.totalPerMonth(
        request.getDocument(), at);
    if (Objects.isNull(average)) {
      var defaultAverage = AverageTransactionsValue.defaultAverage(request.getDocument());
      return Uni.createFrom().item(() ->  UserMonthAverageResponse.newBuilder()
          .setMonth(request.getMonth())
          .setDocument(defaultAverage.getDocument()).setTotal(defaultAverage.getAverage().toString()).build());
    }
    return Uni.createFrom().item(() -> UserMonthAverageResponse.newBuilder()
        .setDocument(average.getDocument())
        .setTotal(average.getAverage().toString())
        .setMonth(at.getMonth().toString())
        .build());
  }
  @Override
  public Uni<LastUserTransactionResponse> getLastUserTransaction(
      LastUserTransactionRequest request) {
    var transaction = this.userTransactionService.lastUserTransaction(request.getDocument());
    if (Objects.isNull(transaction)) {
      return Uni.createFrom().nullItem();
    }
    return Uni.createFrom().item(() -> LastUserTransactionResponse.newBuilder()
        .setDocument(request.getDocument())
        .setValue(transaction.getValue().toString())
        .setCurrency(transaction.getCurrency())
        .setSellerId(transaction.getSellerId())
        .build());
  }

}
