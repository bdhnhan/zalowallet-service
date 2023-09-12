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
    public void addMoneyWallet(AddMoneyWalletRequest request, StreamObserver<AddMoneyWalletResponse> responseObserver) {
        AddMoneyWalletResponse response = zaloWalletBusiness.addMoneyWallet(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deductMoneyWallet(DeductMoneyWalletRequest request, StreamObserver<DeductMoneyWalletResponse> responseObserver) {
        DeductMoneyWalletResponse response = zaloWalletBusiness.deductMoneyWallet(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void revertTransferWallet(RevertTransferWalletRequest request, StreamObserver<RevertTransferWalletResponse> responseObserver) {
        RevertTransferWalletResponse response = zaloWalletBusiness.revertTransferWallet(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getStatusTransaction(GetStatusTransactionRequest request, StreamObserver<GetStatusTransactionResponse> responseObserver) {
        GetStatusTransactionResponse response = zaloWalletBusiness.getStatusTransaction(request.getTransId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
