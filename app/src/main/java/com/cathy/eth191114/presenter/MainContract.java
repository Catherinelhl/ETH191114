package com.cathy.eth191114.presenter;

import java.math.BigInteger;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/15
 */
public interface MainContract {

    interface View {
        void success(String info);
        void failure(String info);

        void gasPriceSuccess(BigInteger bigInteger);
        void gasPriceFailure(String xbigInteger);

        void getBalanceSuccess(String balance);
        void getBalanceSFailure(String balance);

        void getHashRaw(String hashRaw);

        void getAddressSuccess(String address);
    }

    interface Presenter {
        void createWallet();
        void loadWalletByPrivateKey(String privateKey);
        void getGasPrice();
        void connectETHClient();
        void getBalance(String address);
        void getTXList(String address);
        void publishTX(BigInteger bigInteger, String addressTo, String amountString, String privateKey);
        void checkTXInfo(String txHash);
        void cancelSubscribe();
    }
}
