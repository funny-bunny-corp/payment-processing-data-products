package com.paymentic.domain.repositories;


import com.paymentic.domain.AverageTransactionsValue;
import com.paymentic.domain.LastUserTransaction;
import com.paymentic.domain.UserTransaction;
import java.time.LocalDate;

public interface UserTransactionRepository {
  void add(UserTransaction transaction);
  AverageTransactionsValue totalPerMonth(String document, LocalDate at);
  LastUserTransaction lastUserTransaction(String document);
}
