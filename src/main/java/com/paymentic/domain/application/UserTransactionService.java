package com.paymentic.domain.application;


import com.paymentic.domain.AverageTransactionsValue;
import com.paymentic.domain.UserTransaction;
import com.paymentic.domain.repositories.UserTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ApplicationScoped
public class UserTransactionService {
  private final UserTransactionRepository userTransactionRepository;
  public UserTransactionService(UserTransactionRepository userTransactionRepository) {
    this.userTransactionRepository = userTransactionRepository;
  }
  public void storeTransaction(@Observes UserTransaction transaction) {
    this.userTransactionRepository.add(transaction);
  }
  public AverageTransactionsValue totalPerMonth(String document, LocalDate at) {
    return this.userTransactionRepository.totalPerMonth(document, at);
  }

}
