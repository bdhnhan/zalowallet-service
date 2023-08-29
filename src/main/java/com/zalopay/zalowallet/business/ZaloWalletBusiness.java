package com.zalopay.zalowallet.business;

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

import java.io.DataOutputStream;
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

    public Zalowallet.TopUpWalletResponse topUpWallet(Zalowallet.TopUpWalletRequest request) {
        Zalowallet.TopUpWalletResponse.Result.Builder resultBuilder = Zalowallet.TopUpWalletResponse.Result.newBuilder();
        log.info("Receive request top up wallet userId:: {}", request.getUserId());

        UUID uuid = UUID.randomUUID();
        resultBuilder.setTransId(uuid.toString());
        resultBuilder.setStatus(TransactionStatusEnum.PROCESSING.name());

        ZaloWalletTransaction zaloWalletTransaction = new ZaloWalletTransaction();
        zaloWalletTransaction.setId(uuid.toString());
        zaloWalletTransaction.setUserId(request.getUserId());
        zaloWalletTransaction.setStatus(TransactionStatusEnum.PROCESSING);
        zaloWalletTransaction.setAmount(request.getAmount());
        zaloWalletTransaction.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        zaloWalletTransaction.setTransType(TransactionType.TOP_UP);
        zaloWalletTransactionRepository.save(zaloWalletTransaction);

        callBackTopUpTransId(request, uuid.toString());
        return Zalowallet.TopUpWalletResponse.newBuilder()
                .setResult(resultBuilder)
                .setStatus(200)
                .build();
    }

    public void callBackTopUpTransId(Zalowallet.TopUpWalletRequest request, String transId) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Long amount = request.getAmount();
            Optional<ZaloWallet> zaloWalletOptional = zaloWalletRepository.findByUserId(request.getUserId());
            Optional<ZaloWalletTransaction> zaloWalletTransactionOpt =
                    zaloWalletTransactionRepository.findById(transId);

            String url = "http://" + tranferHost + ":" + transferPort + "/transfer/callback";
            if (zaloWalletOptional.isPresent()) {
                Long amountCurrent = zaloWalletOptional.get().getAmount();
                zaloWalletOptional.get().setAmount(amountCurrent + amount);
                zaloWalletOptional.get().setUpdatedTime(new Timestamp(System.currentTimeMillis()));
                zaloWalletRepository.save(zaloWalletOptional.get());
                String json = "{\"transId\":\""+transId+"\",\"status\":\"COMPLETED\"}";
                callBack(url, json);

                zaloWalletTransactionOpt.ifPresent(trans -> {
                        trans.setStatus(TransactionStatusEnum.COMPLETED);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            } else {
                String json = "{\"transId\":\""+transId+"\",\"status\":\"FAILED\"}";
                callBack(url, json);
                zaloWalletTransactionOpt.ifPresent(trans -> {
                        trans.setStatus(TransactionStatusEnum.FAILED);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            }
        });
        thread.start();
    }

    public void callBack(String url, String json) {
        System.out.println(url + " :: " + json);
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
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

    public Zalowallet.WithdrawWalletResponse withdrawWallet(Zalowallet.WithdrawWalletRequest request) {
        Zalowallet.WithdrawWalletResponse.Result.Builder resultBuilder = Zalowallet.WithdrawWalletResponse.Result.newBuilder();
        log.info("Receive request withdraw wallet userId :: {}", request.getUserId());

        UUID uuid = UUID.randomUUID();
        resultBuilder.setTransId(uuid.toString());
        resultBuilder.setStatus(TransactionStatusEnum.PROCESSING.name());

        ZaloWalletTransaction zaloWalletTransaction = new ZaloWalletTransaction();
        zaloWalletTransaction.setId(uuid.toString());
        zaloWalletTransaction.setUserId(request.getUserId());
        zaloWalletTransaction.setStatus(TransactionStatusEnum.PROCESSING);
        zaloWalletTransaction.setAmount(-request.getAmount());
        zaloWalletTransaction.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        zaloWalletTransaction.setTransType(TransactionType.WITHDRAW);
        zaloWalletTransactionRepository.save(zaloWalletTransaction);

        callBackWithdrawTransId(request, uuid.toString());

        return Zalowallet.WithdrawWalletResponse.newBuilder()
                .setResult(resultBuilder)
                .setStatus(200)
                .build();
    }

    public void callBackWithdrawTransId(Zalowallet.WithdrawWalletRequest request, String transId) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Long amount = request.getAmount();
            Optional<ZaloWallet> zaloWalletOptional = zaloWalletRepository.findByUserId(request.getUserId());
            Optional<ZaloWalletTransaction> zaloWalletTransactionOpt =
                    zaloWalletTransactionRepository.findById(transId);

            String url = "http://" + tranferHost + ":" + transferPort + "/transfer/callback";
            if (zaloWalletOptional.isPresent() && zaloWalletOptional.get().getAmount() - amount >= 0L) {
                Long amountCurrent = zaloWalletOptional.get().getAmount();
                zaloWalletOptional.get().setAmount(amountCurrent - amount);
                zaloWalletOptional.get().setUpdatedTime(new Timestamp(System.currentTimeMillis()));
                zaloWalletRepository.save(zaloWalletOptional.get());
                String json = "{\"transId\":\""+transId+"\",\"status\":\"COMPLETED\"}";
                callBack(url, json);
                zaloWalletTransactionOpt.ifPresent(trans -> {
                    trans.setStatus(TransactionStatusEnum.COMPLETED);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            } else {
                String json = "{\"transId\":\""+transId+"\",\"status\":\"FAILED\"}";
                callBack(url, json);
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
                        zaloWalletRepository.findByUserId(zaloWalletTransactionOpt.get().getUserId());
                Long amountCurrent = zaloWalletOptional.get().getAmount();
                zaloWalletOptional.get().setUpdatedTime(new Timestamp(System.currentTimeMillis()));
                zaloWalletOptional.get().setAmount(amountCurrent - zaloWalletTransactionOpt.get().getAmount());
                zaloWalletRepository.save(zaloWalletOptional.get());
                String json = "{\"transId\":\""+transId+"\",\"status\":\"COMPLETED\"}";
                callBack(url, json);
                zaloWalletTransactionOpt.ifPresent(trans -> {
                    trans.setStatus(TransactionStatusEnum.ROLLBACK);
                    zaloWalletTransactionRepository.save(zaloWalletTransactionOpt.get());
                });
            } else {
                String json = "{\"transId\":\""+transId+"\",\"status\":\"FAILED\"}";
                callBack(url, json);
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
}
