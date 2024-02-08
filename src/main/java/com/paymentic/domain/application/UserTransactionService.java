package com.paymentic.domain.application;


import com.paymentic.domain.UserTransaction;
import com.paymentic.domain.repositories.UserTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class UserTransactionService {
  private final UserTransactionRepository userTransactionRepository;
  public UserTransactionService(UserTransactionRepository userTransactionRepository) {
    this.userTransactionRepository = userTransactionRepository;
  }
  public void storeTransaction(@Observes UserTransaction transaction) {
    this.userTransactionRepository.add(transaction);
  }

}
