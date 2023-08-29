package com.zalopay.zalowallet.repository;

import com.zalopay.zalowallet.entity.ZaloWallet;
import com.zalopay.zalowallet.entity.ZaloWalletTransaction;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ZaloWalletTransactionRepository extends CrudRepository<ZaloWalletTransaction, String> {
}
