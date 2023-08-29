package com.zalopay.zalowallet.entity;

import com.zalopay.zalowallet.enums.WalletStatusEnum;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "zalo_wallet")
@Data
public class ZaloWallet {
    @Id
    private String id;
    private String userId;
    private String username;
    private Long amount;
    @Enumerated(EnumType.STRING)
    private WalletStatusEnum status;
    private String phoneNumber;
    private Timestamp createdTime;
    private Timestamp updatedTime;
}
