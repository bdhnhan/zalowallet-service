package com.zalopay.zalowallet.entity;

import com.zalopay.zalowallet.enums.TransactionStatusEnum;
import com.zalopay.zalowallet.enums.TransactionType;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "zalo_wallet_transaction")
@Data
public class ZaloWalletTransaction {
    @Id
    private String id;
    private String userId;
    private Long amount;
    @Enumerated(EnumType.STRING)
    private TransactionStatusEnum status;
    @Enumerated(EnumType.STRING)
    private TransactionType transType;
    private Timestamp createdTime;
}
