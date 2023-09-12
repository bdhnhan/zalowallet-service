package com.zalopay.zalowallet.business;

import com.zalopay.zalowallet.configuration.ConfigHttpConnect;
import com.zalopay.zalowallet.controller.response.ResultResponse;
import com.zalopay.zalowallet.data.CallBackResponse;
import com.zalopay.zalowallet.entity.ZaloWallet;
import com.zalopay.zalowallet.entity.ZaloWalletTransaction;
import com.zalopay.zalowallet.enums.TransactionStatusEnum;
import com.zalopay.zalowallet.enums.TransactionType;
import com.zalopay.zalowallet.repository.ZaloWalletRepository;
import com.zalopay.zalowallet.repository.ZaloWalletTransactionRepository;
import com.zalowallet.protobuf.Zalowallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class ZaloWalletBusiness {
    private final ZaloWalletRepository zaloWalletRepository;
    private final ZaloWalletTransactionRepository zaloWalletTransactionRepository;

    @Value("${external.transfer-service.host}")
    private String tranferHost;
    @Value("${external.transfer-service.port}")
    private String transferPort;

    public ZaloWalletBusiness(ZaloWalletRepository zaloWalletRepository, ZaloWalletTransactionRepository zaloWalletTransactionRepository) {
        this.zaloWalletRepository = zaloWalletRepository;
        this.zaloWalletTransactionRepository = zaloWalletTransactionRepository;
    }

    public Zalowallet.AddMoneyWalletResponse addMoneyWallet(Zalowallet.AddMoneyWalletRequest request) {
        Zalowallet.AddMoneyWalletResponse.Result.Builder resultBuilder = Zalowallet.AddMoneyWalletResponse.Result.newBuilder();
        log.info("Receive request top up wallet PhoneNumber:: {}", request.getPhoneNumber());

        UUID uuid = UUID.randomUUID();
        resultBuilder.setTransId(uuid.toString());
        resultBuilder.setStatus(TransactionStatusEnum.PROCESSING.name());

        ZaloWalletTransaction zaloWalletTransaction = new ZaloWalletTransaction();
        zaloWalletTransaction.setId(uuid.toString());
        zaloWalletTransaction.setPhoneNumber(request.getPhoneNumber());
        zaloWalletTransaction.setStatus(TransactionStatusEnum.PROCESSING);
        zaloWalletTransaction.setAmount(request.getAmount());
        zaloWalletTransaction.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        zaloWalletTransaction.setTransType(TransactionType.TOP_UP);
        zaloWalletTransactionRepository.save(zaloWalletTransaction);

        callBackAddMoneyTransId(request, uuid.toString());
        return Zalowallet.AddMoneyWalletResponse.newBuilder()
                .setResult(resultBuilder)
                .setStatus(200)
                .build();
    }

    public void callBackAddMoneyTransId(Zalowallet.AddMoneyWalletRequest request, String transId) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Long amount = request.getAmount();
            Optional<ZaloWallet> zaloWalletOptional = zaloWalletRepository.findByPhoneNumber(request.getPhoneNumber());
            Optional<ZaloWalletTransaction> zaloWalletTransactionOpt =
                    zaloWalletTransactionRepository.findById(transId);

            if (zaloWalletOptional.isPresent()) {
                Long amountCurrent = zaloWalletOptional.get().getAmount();
                zaloWalletOptional.get().setAmount(amountCurrent + amount);
                zaloWalletOptional.get().setUpdatedTime(new Timestamp(System.currentTimeMillis()));
                zaloWalletRepository.save(zaloWalletOptional.get());
                String json = CallBackResponse.generateJsonString(transId, "COMPLETED");
                callBack(json);

                zaloWalletTransactionOpt.ifPresent(trans -> {
                        trans.setStatus(TransactionStatusEnum.COMPLETED);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            } else {
                String json = CallBackResponse.generateJsonString(transId, "FAILED");
                callBack(json);
                zaloWalletTransactionOpt.ifPresent(trans -> {
                        trans.setStatus(TransactionStatusEnum.FAILED);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            }
        });
        thread.start();
    }

    public void callBack(String json) {
        String url = "http://" + tranferHost + ":" + transferPort + "/transfer/callback";
        try {
            HttpURLConnection con = ConfigHttpConnect.connect(url);
            con.getOutputStream().write(json.getBytes());
            con.connect();
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Connect successfully!");
            } else {
                System.out.println("Error: " + responseCode);
            }
            con.disconnect();
        } catch (IOException e) {
            log.error("LOST CONNECTION");
        }
    }

    public Zalowallet.DeductMoneyWalletResponse deductMoneyWallet(Zalowallet.DeductMoneyWalletRequest request) {
        Zalowallet.DeductMoneyWalletResponse.Result.Builder resultBuilder = Zalowallet.DeductMoneyWalletResponse.Result.newBuilder();
        log.info("Receive request deductMoney wallet phoneNumber :: {}", request.getPhoneNumber());

        UUID uuid = UUID.randomUUID();
        resultBuilder.setTransId(uuid.toString());
        resultBuilder.setStatus(TransactionStatusEnum.PROCESSING.name());

        ZaloWalletTransaction zaloWalletTransaction = new ZaloWalletTransaction();
        zaloWalletTransaction.setId(uuid.toString());
        zaloWalletTransaction.setPhoneNumber(request.getPhoneNumber());
        zaloWalletTransaction.setStatus(TransactionStatusEnum.PROCESSING);
        zaloWalletTransaction.setAmount(-request.getAmount());
        zaloWalletTransaction.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        zaloWalletTransaction.setTransType(TransactionType.WITHDRAW);
        zaloWalletTransactionRepository.save(zaloWalletTransaction);

        callBackDeductMoneyTransId(request, uuid.toString());

        return Zalowallet.DeductMoneyWalletResponse.newBuilder()
                .setResult(resultBuilder)
                .setStatus(200)
                .build();
    }

    public void callBackDeductMoneyTransId(Zalowallet.DeductMoneyWalletRequest request, String transId) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Long amount = request.getAmount();
            Optional<ZaloWallet> zaloWalletOptional = zaloWalletRepository.findByPhoneNumber(request.getPhoneNumber());
            Optional<ZaloWalletTransaction> zaloWalletTransactionOpt = zaloWalletTransactionRepository.findById(transId);

            if (zaloWalletOptional.isPresent() && zaloWalletOptional.get().getAmount() - amount >= 0L) {
                Long amountCurrent = zaloWalletOptional.get().getAmount();
                zaloWalletOptional.get().setAmount(amountCurrent - amount);
                zaloWalletOptional.get().setUpdatedTime(new Timestamp(System.currentTimeMillis()));
                zaloWalletRepository.save(zaloWalletOptional.get());
                String json = CallBackResponse.generateJsonString(transId, "COMPLETED");
                callBack(json);
                zaloWalletTransactionOpt.ifPresent(trans -> {
                    trans.setStatus(TransactionStatusEnum.COMPLETED);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            } else {
                String json = CallBackResponse.generateJsonString(transId, "FAILED");
                callBack(json);
                zaloWalletTransactionOpt.ifPresent(trans -> {
                    trans.setStatus(TransactionStatusEnum.FAILED);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            }
        });
        thread.start();
    }

    public void callBackRevertTransferTransId(Zalowallet.RevertTransferWalletRequest request) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String transId = request.getTransId();
            Optional<ZaloWalletTransaction> zaloWalletTransactionOpt =
                    zaloWalletTransactionRepository.findById(request.getTransId());
            String url = "http://" + tranferHost + ":" + transferPort + "/transfer/callback";
            if (zaloWalletTransactionOpt.isPresent()) {
                Optional<ZaloWallet> zaloWalletOptional =
                        zaloWalletRepository.findByPhoneNumber(zaloWalletTransactionOpt.get().getPhoneNumber());
                Long amountCurrent = zaloWalletOptional.get().getAmount();
                zaloWalletOptional.get().setUpdatedTime(new Timestamp(System.currentTimeMillis()));
                zaloWalletOptional.get().setAmount(amountCurrent - zaloWalletTransactionOpt.get().getAmount());
                zaloWalletRepository.save(zaloWalletOptional.get());
                String json = CallBackResponse.generateJsonString(transId, "COMPLETED");
                callBack(json);
                zaloWalletTransactionOpt.ifPresent(trans -> {
                    trans.setStatus(TransactionStatusEnum.ROLLBACK);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            } else {
                String json = CallBackResponse.generateJsonString(transId, "FAILED");
                callBack(json);
            }
        });
        thread.start();
    }

    public Zalowallet.RevertTransferWalletResponse revertTransferWallet(Zalowallet.RevertTransferWalletRequest request) {
        Zalowallet.RevertTransferWalletResponse.Result.Builder resultBuilder = Zalowallet.RevertTransferWalletResponse.Result.newBuilder();
        log.info("Receive request revert transfer wallet :: {}", request.getTransId());

        resultBuilder.setStatus(TransactionStatusEnum.PROCESSING.name());

        callBackRevertTransferTransId(request);

        return Zalowallet.RevertTransferWalletResponse.newBuilder()
                .setResult(resultBuilder)
                .setStatus(200)
                .build();
    }

    public ResultResponse<ZaloWallet> getWalletInfo(String phoneNumber) {
        Optional<ZaloWallet> zaloWalletOptional = zaloWalletRepository.findByPhoneNumber(phoneNumber);
        if (zaloWalletOptional.isPresent()) {
            ZaloWallet zaloWallet = zaloWalletOptional.get();
            return ResultResponse.<ZaloWallet>builder()
                    .status(200L)
                    .result(zaloWallet)
                    .build();
        }
        return ResultResponse.<ZaloWallet>builder()
                .status(400L)
                .result(null)
                .build();
    }

    public Zalowallet.GetStatusTransactionResponse getStatusTransaction(String transId) {
        Optional<ZaloWalletTransaction> zaloWalletTransactionOpt = zaloWalletTransactionRepository.findById(transId);
        return zaloWalletTransactionOpt.map(zaloWalletTransaction -> Zalowallet.GetStatusTransactionResponse.newBuilder()
                .setStatus(200)
                .setResult(
                        Zalowallet.GetStatusTransactionResponse.Result.newBuilder()
                                .setTransId(transId)
                                .setStatus(zaloWalletTransaction.getStatus().name())
                                .build()
                ).build()).orElseGet(() -> Zalowallet.GetStatusTransactionResponse.newBuilder()
                .setStatus(400)
                .setResult(
                        Zalowallet.GetStatusTransactionResponse.Result.newBuilder()
                                .setTransId(transId)
                                .setStatus("Can not found transactionID :: " + transId)
                                .build()
                ).build());

    }
}
