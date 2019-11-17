package com.cathy.eth191114.presenter;

import android.text.TextUtils;

import com.cathy.eth191114.constants.ETHParamConstants;
import com.cathy.eth191114.tool.LogTool;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Future;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kotlin.Deprecated;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/15
 */
public class MainPresenterImp implements MainContract.Presenter {

    private String TAG = MainPresenterImp.class.getSimpleName();

    private Web3j web3j;
    private Credentials credentials;

    private MainContract.View view;
    private String transactionHash;//得到此次交易的hash

    public MainPresenterImp(MainContract.View view) {

        this.view = view;
    }

    @Override
    public void createWallet() {
        try {

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            System.out.println("PrivateKey:" + ecKeyPair.getPrivateKey());
            System.out.println("PublicKey:" + ecKeyPair.getPublicKey());
            System.out.println("Address:" + Keys.getAddress(ecKeyPair));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过加载私钥来得到当前钱包的元数据
     */
    @Override
    public void loadWalletByPrivateKey(String privateKey) {

        byte[] array = new BigInteger(privateKey).toByteArray();
        if (array[0] == 0) {
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }
        String privateKeyConvert = Hex.toHexString(array);
        credentials = Credentials.create(privateKeyConvert);
        String address = credentials.getAddress();
        LogTool.d(TAG, "address=" + address);
        view.getAddressSuccess(address);
        LogTool.d(TAG, " public key=" + credentials.getEcKeyPair().getPublicKey());
        LogTool.d(TAG, " private key=" + credentials.getEcKeyPair().getPrivateKey());
    }

    /**
     * 连接以太坊客户端
     */

    @Override
    public void connectETHClient() {
        LogTool.d(TAG, "connectETHClient");
        //连接方式:使用infura 提供的客户端
        web3j = Web3j.build(new HttpService(ETHParamConstants.NetworkParameter));

        Disposable subscribe = Observable.just(web3j.web3ClientVersion())
                .map(new Function<Request<?, Web3ClientVersion>, String>() {
                    @Override
                    public String apply(Request<?, Web3ClientVersion> web3ClientVersionRequest) throws Exception {
                        return web3ClientVersionRequest.send().getWeb3ClientVersion();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String web3ClientVersion) throws Exception {
                        //测试是否连接成功
                        view.success("version:" + web3ClientVersion);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.failure(throwable.getMessage());
                    }
                });
    }


    /***********查询指定地址的余额***********/
    /**
     * 其中核心方法 web3j.ethGetBalance(address, defaultBlockParameter）
     * 中的第二个参数比较特殊，指默认的区块参数。当请求余额的方法作用与以太坊的区块网络时，
     * 这个参数决定了查询区块的高度。
     * <p>
     * <p>
     * 一般情况下，选择“latest”即可。
     * <p>
     * 以太坊中，如果没有特殊标示，数字的单位都是小数点后18位，因此查询账户余额有必要将wei转化成ether。
     * <p>
     * HEX String - 一个整数块号
     * String "earliest" 为最早/起源块
     * String "latest" - 为最新的采矿块
     * String "pending" - 待处理状态/交易
     *
     * @throws IOException
     */

    @Override
    public void getBalance(String address) {
        if (web3j == null) return;
        //第二个参数：区块的参数，建议选最新区块
        Disposable subscribe = Observable.just(web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST))
                .map(new Function<Request<?, EthGetBalance>, Future<EthGetBalance>>() {
                    @Override
                    public Future<EthGetBalance> apply(Request<?, EthGetBalance> ethGetBalanceRequest) throws Exception {
                        //这里可以有异步，也可以同步：send()
                        return ethGetBalanceRequest.sendAsync();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Future<EthGetBalance>>() {
                    @Override
                    public void accept(Future<EthGetBalance> ethGetBalanceFuture) throws Exception {
                        EthGetBalance ethGetBalance = ethGetBalanceFuture.get();
                        //格式转化 wei-ether
                        String balanceETH = Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER).toPlainString().concat(" ether");
                        view.getBalanceSuccess(balanceETH);
                        LogTool.d(TAG, balanceETH);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogTool.e(TAG, throwable.getMessage());
                        view.getBalanceSFailure(throwable.getMessage());
                    }
                });
    }

    @Override
    public void getGasPrice() {
        if (web3j == null) return;
        Disposable subscribe = Observable.just(web3j.ethGasPrice()).map(new Function<Request<?, EthGasPrice>, Future<EthGasPrice>>() {
            @Override
            public Future<EthGasPrice> apply(Request<?, EthGasPrice> ethGasPriceRequest) throws Exception {
                return ethGasPriceRequest.sendAsync();
            }
        }).subscribe(new Consumer<Future<EthGasPrice>>() {
            @Override
            public void accept(Future<EthGasPrice> ethGasPriceFuture) throws Exception {
                BigInteger bigInteger = ethGasPriceFuture.get().getGasPrice();
                view.gasPriceSuccess(bigInteger);
                LogTool.d(TAG, "gasPrice:" + bigInteger);

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                view.gasPriceFailure(throwable.getMessage());
            }
        });
    }

    @Override
    public void getTXList(String address) {
        if (web3j == null) return;
        getTxListByEtherscan(address);


    }

    /**
     * 通过Etherscan直接网络请求进行查询
     * <p>
     * module=account
     * * &action=txlist
     * * &address=0xddbd2b932c763ba5b1b7ae3b362eac3e8d40121a
     * * &startblock=0
     * * &endblock=99999999
     * * &sort=asc
     * * &apikey=YourApiKeyToken
     * <p>
     * apikey 这里为null也行
     */
    private void getTxListByEtherscan(String address) {
//        //desc 从高到低
//        //asc 从低到高
//        interactor.getTXList("account", "txlist", address, "0",
//                "99999999", "desc", Constants.APIKEY, new Callback<ETHTXListResponse>() {
//                    @Override
//                    public void onResponse(Call<ETHTXListResponse> call, Response<ETHTXListResponse> response) {
//                        if (response != null) {
//                            ETHTXListResponse ethtxListResponse = response.body();
//                            view.success(ethtxListResponse.toString());
//                            if (ethtxListResponse != null) {
//                                List<TXListBean> txListBeans = ethtxListResponse.getResult();
//                                if (txListBeans != null && txListBeans.size() > 0) {
//                                    for (TXListBean txListBean : txListBeans) {
////                                        LogTool.d(TAG, response.body().getResult());
//                                        LogTool.d(TAG, "Hash:" + txListBean.getHash() + ";status:" + txListBean.getConfirmations());
//                                    }
//                                }
//
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ETHTXListResponse> call, Throwable t) {
//                        LogTool.e(TAG, t.getMessage());
//
//                    }
//                });

    }

    @Deprecated(message = "unuse")
    private void getTXListByWeb3j() {
        Disposable subscribe = Observable.just(web3j.ethGetTransactionReceipt(transactionHash)).
                map(new Function<Request<?, EthGetTransactionReceipt>, EthGetTransactionReceipt>() {
                    @Override
                    public EthGetTransactionReceipt apply(Request<?, EthGetTransactionReceipt> ethGetTransactionReceiptRequest) throws Exception {
                        return ethGetTransactionReceiptRequest.send();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<EthGetTransactionReceipt>() {
                    @Override
                    public void accept(EthGetTransactionReceipt ethGetTransactionReceipt) throws Exception {
                        LogTool.d(TAG, ethGetTransactionReceipt.getError());
                        LogTool.d(TAG, ethGetTransactionReceipt.getId());
                        LogTool.d(TAG, ethGetTransactionReceipt.getResult());
                        view.success(ethGetTransactionReceipt.getRawResponse());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.failure(throwable.getMessage());
                    }
                });
    }

    /****************交易*****************/
    /**
     * 作为一个钱包，除了保存账户资产外，最重要的就是转账或交易了，利用web3j可以便捷的实现eth的转移。
     * 核心方法需要提供4个参数：
     * <p>
     * web3j实体
     * Credentials 源账户所有信息
     * address 转出地址
     * value 数量
     * unit 单位
     * 等待片刻后，会返回转账结果
     *
     * @throws Exception
     */
    @Override
    public void publishTX(BigInteger gasPrice, String addressTo, String amountString, String privateKey) {
        if (web3j == null) return;
        if (credentials == null) {
            loadWalletByPrivateKey(privateKey);
        }

        BigDecimal amount = new BigDecimal(amountString);
        LogTool.d(TAG, "addressTo:" + addressTo);
        LogTool.d(TAG, "amount:" + amount);
        LogTool.d(TAG, "gasPrice:" + gasPrice);
        LogTool.d(TAG, "privateKey:" + credentials.getEcKeyPair().getPrivateKey());
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials);
        Transfer transfer = new Transfer(web3j, transactionManager);
        Disposable disposable = Observable.just(transfer.sendFunds(addressTo, amount, Convert.Unit.ETHER, gasPrice, BigInteger.valueOf(21000)))
                .map(new Function<RemoteCall<TransactionReceipt>, TransactionReceipt>() {
                    @Override
                    public TransactionReceipt apply(RemoteCall<TransactionReceipt> transactionReceiptRemoteCall) throws Exception {
                        LogTool.d(TAG, "pushing");
                        return transactionReceiptRemoteCall.send();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TransactionReceipt>() {
                    @Override
                    public void accept(TransactionReceipt send) throws Exception {
                        if (send == null) {
                            view.failure("failure send is null");
                        } else {
                            LogTool.d(TAG, "Transaction complete:");
                            LogTool.d(TAG, "trans hash=" + send.getTransactionHash());
                            LogTool.d(TAG, "block hash" + send.getBlockHash());
                            LogTool.d(TAG, "from :" + send.getFrom());
                            LogTool.d(TAG, "to:" + send.getTo());
                            LogTool.d(TAG, "gas used=" + send.getGasUsed());
                            LogTool.d(TAG, "status: " + send.getStatus());
                            transactionHash = send.getTransactionHash();
                            view.success(transactionHash);
                            view.getHashRaw(transactionHash);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.failure(throwable.getMessage());
                        LogTool.e(TAG, throwable.getMessage());
                    }
                });


    }

    @Override
    public void checkTXInfo(String txHash) {
        if (web3j == null) return;
        if (TextUtils.isEmpty(txHash)) {
            view.failure("transactionHash is empty!!");
            return;
        }
//        Disposable subscribe = Observable.just(web3j.ethGetTransactionByHash(txHash))
//                .map(new Function<Request<?, EthTransaction>, Future<EthTransaction>>() {
//
//                    @Override
//                    public Future<EthTransaction> apply(Request<?, EthTransaction> ethTransactionRequest) throws Exception {
//                        return ethTransactionRequest.sendAsync();
//                    }
//                }).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Future<EthTransaction>>() {
//                    @Override
//                    public void accept(Future<EthTransaction> ethTransactionFuture) throws Exception {
//                        LogTool.d(TAG, "ethTransactionFuture:" + ethTransactionFuture);
//                        if (ethTransactionFuture == null) {
//                            return;
//
//                        }
//                        EthTransaction ethTransaction = ethTransactionFuture.get();
//                        LogTool.d(TAG, "ethTransaction:" + ethTransaction);
//                        if (ethTransaction == null) {
//                            return;
//                        }
//                        Transaction transaction = ethTransaction.getTransaction();
//                        LogTool.d(TAG, "transaction:" + transaction);
//                        if (transaction == null) {
//                            return;
//                        }
//                        LogTool.d(TAG, "getBlockNumber:" + transaction.getBlockNumber());
//                        LogTool.d(TAG, "getBlockHash:" + transaction.getBlockHash());
//                        LogTool.d(TAG, "getBlockNumberRaw:" + transaction.getBlockNumberRaw());
//                        LogTool.d(TAG, "getGasPriceRaw:" + transaction.getGasPriceRaw());
//                        LogTool.d(TAG, "getGasPrice:" + transaction.getGasPrice());
//                        LogTool.d(TAG, "getGasRaw:" + transaction.getGasRaw());
//                        LogTool.d(TAG, "getHash:" + transaction.getHash());
//                        LogTool.d(TAG, "getInput:" + transaction.getInput());
//                        LogTool.d(TAG, "getNonceRaw:" + transaction.getNonceRaw());
//                        LogTool.d(TAG, "getNonce:" + transaction.getNonce());
//                        LogTool.d(TAG, "getS:" + transaction.getS());
//                        LogTool.d(TAG, "getR:" + transaction.getR());
//                        LogTool.d(TAG, "getRaw:" + transaction.getRaw());
//                        LogTool.d(TAG, "getFrom:" + transaction.getFrom());
//                        LogTool.d(TAG, "getTo:" + transaction.getTo());
//                        LogTool.d(TAG, "getTransactionIndexRaw:" + transaction.getTransactionIndexRaw());
//                        LogTool.d(TAG, "getTransactionIndex:" + transaction.getTransactionIndex());
//                        LogTool.d(TAG, "getValue:" + transaction.getValue());
//                        LogTool.d(TAG, "getValueRaw:" + transaction.getValueRaw());
//                        LogTool.d(TAG, "getCreates:" + transaction.getCreates());
//                        if (transaction == null) {
//                            view.failure("transaction is empty!!");
//                            return;
//                        }
//                        view.success("getBlockNumber:" + transaction.getBlockNumber().toString());
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        LogTool.e(TAG, throwable.getMessage());
//                        view.failure(throwable.getMessage());
//                    }
//                });

    }

    @Override
    public void cancelSubscribe() {
    }

}
