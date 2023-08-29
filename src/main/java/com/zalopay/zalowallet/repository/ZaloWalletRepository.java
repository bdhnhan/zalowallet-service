package com.zalopay.zalowallet.repository;

import com.zalopay.zalowallet.entity.ZaloWallet;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ZaloWalletRepository extends CrudRepository<ZaloWallet, String> {
    Optional<ZaloWallet> findByUserId(String userId);
}
