package com.zalopay.zalowallet.controller;

import com.zalopay.zalowallet.business.ZaloWalletBusiness;
import com.zalopay.zalowallet.controller.response.ResultResponse;
import com.zalopay.zalowallet.entity.ZaloWallet;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final ZaloWalletBusiness zaloWalletBusiness;

    public WalletController(ZaloWalletBusiness zaloWalletBusiness) {
        this.zaloWalletBusiness = zaloWalletBusiness;
    }

    @GetMapping("/{phoneNumber}")
    public ResultResponse<ZaloWallet> getWalletDetail(@PathVariable String phoneNumber) {
        return zaloWalletBusiness.getWalletInfo(phoneNumber);
    }
}
