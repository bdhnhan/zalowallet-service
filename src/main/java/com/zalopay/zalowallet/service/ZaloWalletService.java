package com.zalopay.zalowallet.service;

import com.zalopay.zalowallet.business.ZaloWalletBusiness;
import com.zalowallet.protobuf.ZalopayServiceGrpc;
import com.zalowallet.protobuf.Zalowallet.*;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ZaloWalletService extends ZalopayServiceGrpc.ZalopayServiceImplBase {
    private final ZaloWalletBusiness zaloWalletBusiness;

    public ZaloWalletService(ZaloWalletBusiness zaloWalletBusiness) {
        this.zaloWalletBusiness = zaloWalletBusiness;
    }

    @Override
    public void topUpWallet(TopUpWalletRequest request, StreamObserver<TopUpWalletResponse> responseObserver) {
        TopUpWalletResponse response = zaloWalletBusiness.topUpWallet(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void withdrawWallet(WithdrawWalletRequest request, StreamObserver<WithdrawWalletResponse> responseObserver) {
        WithdrawWalletResponse response = zaloWalletBusiness.withdrawWallet(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void revertTransferWallet(RevertTransferWalletRequest request, StreamObserver<RevertTransferWalletResponse> responseObserver) {
        RevertTransferWalletResponse response = zaloWalletBusiness.revertTransferWallet(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
