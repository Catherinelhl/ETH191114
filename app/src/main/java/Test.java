/*
+--------------------------------------+
+ Create by Catherine Liu                                  
+--------------------------------------+
+ 2019/11/21 9:35                                  
+--------------------------------------+
+ Des: 
+--------------------------------------+
*/

import android.text.TextUtils;

import com.cathy.eth191114.constants.ETHParamConstants;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.sql.DriverManager.println;

public class Test {

    private static Web3j web3j;

    private static String owner = "0x3576283a61D0571856A455C165DEf3fd50D18BCF";
    private static String contractAddress = "0xb72c794effb775197287d767ca80c22ae9094cb5";

    public static void main(String[] args){
        System.out.println("main");
       createClient();
        balanceOf(owner, contractAddress);

    }


    private static void createClient(){
        boolean async = true;
        web3j = Web3j.build(new HttpService(ETHParamConstants.NetworkParameter));  // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion = null;
        try {
            web3ClientVersion = async ? web3j.web3ClientVersion().sendAsync().get() : web3j.web3ClientVersion().send();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        println("current Client Version:$clientVersion:" + clientVersion);
    }


//    /**
//
//     * 查询代币余额
//
//     *
//
//     * @param address  账户地址
//
//     * @param contract 合约地址
//
//     * @return
//
//     * @throws IOException
//
//     */
//
//    public BigDecimal getTokenBalance(String address, String contract) throws Exception {
//
//        BigInteger balance = BigInteger.ZERO;
//
//        Function fn = new Function("balanceOf", Collections.singletonList(new Address(address)), Collections.<TypeReference<?>>emptyList());
//
//        String data = FunctionEncoder.encode(fn);
//
//        Map<String, String> map = new HashMap<String, String>();
//
//        map.put("to", contract);
//
//        map.put("data", data);
//
//        try {
//
//            String methodName = "eth_call";
//
//            Object[] params = new Object[]{map, "latest"};
//
//            String result = jsonRpcHttpClient.invoke(methodName, params, Object.class).toString();
//
//            if (!TextUtils.isEmpty(result)) {
//
//                if ("0x".equalsIgnoreCase(result) || result.length() == 2) {
//
//                    result = "0x0";
//
//                }
//
//                balance = Numeric.decodeQuantity(result);
//
//            }
//
//        } catch (Throwable e) {
//
//            throw new Exception("查询接口ERROR");
//
//        }
//
//        return new BigDecimal(balance);
//
//    }

    /**
     * 获取合约的 balanceOf 方法
     *
     * @param owner 用户ETH钱包地址
     * @return
     */
    private static void balanceOf(String owner, String contractAddress)  {
        Function function = new org.web3j.abi.datatypes.Function(
                "balanceOf",
                Collections.singletonList(new Address(owner)),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));

        String functionEncoder = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(owner, contractAddress, functionEncoder);
        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!ethCall.hasError()) {
            String value = ethCall.getResult();
            if (value.startsWith("0x")) {
                value = value.substring(2);
            }
            BigInteger data = new BigInteger(value, 16);

            println("value:$value:" + value);
            println("data:$data:" + data);
        }
//        //1. 先根据合约地址获取合约的Decimal
//        getDecimalsByContractAddress(contractAddress)
//                .subscribe(bigInteger -> {
//                    int tokenDecimal = bigInteger.intValue();
//                    //2.根据 合约地址、ETH钱包地址获取余额
//                    String responseValue = callSmartContractFunction(function, contractAddress, owner);
//                    System.out.println("contractAddress = " + contractAddress + "   responseValue = " + responseValue);
//                    BigDecimal tokenBalance = new BigDecimal("0");
//                    if (!TextUtils.isEmpty(responseValue)) {
//                        List<Type> response = FunctionReturnDecoder.decode(
//                                responseValue, function.getOutputParameters());
//                        //3. 根据Decimal计算出实际的余额
//                        if (response.size() == 1) {
//                            BigDecimal bigDecimal = new BigDecimal(((Uint256) response.get(0)).getValue());
//                            //余额>0
//                            if (bigDecimal.doubleValue() > 0) {
//                                BigDecimal decimal = new BigDecimal(Math.pow(10, tokenDecimal));
//                                //除以Decimal 后的余额
//                                tokenBalance = bigDecimal.divide(decimal, com.qbao.library.utility.Constants.ETH_DECIMAL, BigDecimal.ROUND_DOWN);
//                            } else {
//                                tokenBalance = new BigDecimal("0");
//                            }
//                        }
//                    }
//                    System.out.println("getTokenBalance = " + tokenBalance.toString());
////                    e.onNext(tokenBalance);
////                    e.onCompletepleteete();
//                }, throwable -> {
//                    System.out.println("throwable");
////                    e.onNext(new BigDecimal("0"));
////                    e.onComplete();
//                });
    }

    /**
     * ethCall 方法
     *
     * @param function        data
     * @param contractAddress to 合约地址
     * @param address         from 地址
     * @return
     */
    private String callSmartContractFunction(
            org.web3j.abi.datatypes.Function function, String contractAddress, String address) {
        String encodedFunction = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.response.EthCall response = null;
        try {
            response = web3j.ethCall(
                    Transaction.createEthCallTransaction(address, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST)
                    .sendAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return response == null ? "" : response.getValue();
    }
}

