package com.cathy.eth191114


import com.cathy.eth191114.constants.Constants
import com.cathy.eth191114.constants.ETHParamConstants
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val TAG = ExampleUnitTest::class.java.simpleName
    @Test
    fun addition_isCorrect() {
        //step 1 创建房间
//    PrivateKey:70894486279756432157224976880339036365675198113074183826699610870590116898825
//    PublicKey:6208972924420545558134764276104808487339782137951799675124507908618225250190716508719454803697268030747092323269358473534415193745538444937623819315324500
//    Address:2ffbcc70c95bddbdb4379c8eed4d3260f7da52d7
//    createAccountDirectly()
        //step 2 创建客户端
        createClient(true)
        //step3:获取余额

        println(
            balanceOf(
                "0x3576283a61D0571856A455C165DEf3fd50D18BCF",
                " 0xb72c794effb775197287d767ca80c22ae9094cb5"
            )
        )
    }


    private lateinit var web3j: Web3j
    fun createClient(async: Boolean) {
        web3j =
            Web3j.build(HttpService(ETHParamConstants.NetworkParameter))  // defaults to http://localhost:8545/
        val web3ClientVersion =
            if (async) web3j.web3ClientVersion().sendAsync().get() else web3j.web3ClientVersion().send()
        val clientVersion = web3ClientVersion.web3ClientVersion
        println("current Client Version:$clientVersion")
    }

    /**
     * step 1:直接创建钱包
     */
    fun createAccountDirectly() {
        try {
            val ecKeyPair = Keys.createEcKeyPair()
            println("PrivateKey:" + ecKeyPair.privateKey)
            println("PublicKey:" + ecKeyPair.publicKey)
            println("Address:" + Keys.getAddress(ecKeyPair))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * getBalance
     */
    fun getBalance() {
        /***********查询指定地址的余额 */
//    val address = "0x07757733653a6670a4f7b8d30704378cb4cf89b2"//等待查询余额的地址
        //第二个参数：区块的参数，建议选最新区块
        val balance =
            web3j.ethGetBalance(Constants.address, DefaultBlockParameterName.EARLIEST).send()
        //格式转化 wei-ether
        val balanceETH =
            Convert.fromWei(
                balance.balance.toString(),
                Convert.Unit.ETHER
            ).toPlainString() + " ether"
        println(balanceETH)
    }

    private fun smartContract() {
        val contractAddress = ""
//                //1. 先根据合约地址获取合约的Decimal
//                getDecimalsByContractAddress(contractAddress)
//                        .subscribe(bigInteger -> run {
//            val tokenDecimal: Int = bigInteger.intValue()
//            //2.根据 合约地址、ETH钱包地址获取余额
//            val responseValue: String =
//                callSmartContractFunction(function, contractAddress, Constants.address);
//            println("contractAddress = $contractAddress   responseValue = $responseValue");
//            var tokenBalance: BigDecimal = BigDecimal("0");
//            if (!TextUtils.isEmpty(responseValue)) {
//                val response: List<Type> = FunctionReturnDecoder.decode(
//                    responseValue, function.getOutputParameters()
//                );
//                //3. 根据Decimal计算出实际的余额
//                if (response.size == 1) {
//                    val bigDecimal:BigDecimal =  BigDecimal((response[0] as Uint256).value)
//                    //余额>0
//                    if (bigDecimal.doubleValue() > 0) {
//                        val decimal :BigDecimal =  BigDecimal(Math.pow(10, tokenDecimal))
//                        //除以Decimal 后的余额
//                        tokenBalance = bigDecimal.divide(
//                            decimal,
//                            8,
//                            BigDecimal.ROUND_DOWN
//                        );
//                    } else {
//                        tokenBalance =  BigDecimal ("0");
//                    }
//                }
//            }
//            println("getTokenBalance =${tokenBalance.toString()}");
////            e.onNext(tokenBalance);
////            e.onComplete();
//        }, throwable -> {
////                            e.onNext(new BigDecimal("0"));
////                            e.onComplete();
//                        });
    }

    /**
     * 获取账户代币余额
     * @param account 账户地址
     * @param coinAddress 代币地址
     * @return 代币余额 （单位：代币最小单位）
     * @throws IOException
     */
    private fun balanceOf(owner: String, coinAddress: String): BigDecimal? {
        val function = org.web3j.abi.datatypes.Function(
            "balanceOf",
            Collections.singletonList(Address(owner)) as List<Type<String>>,
            Collections.singletonList(object : TypeReference<Uint256>() {
            }) as List<TypeReference<Uint256>>
        )
        val transactionManager = object : TransactionManager(web3j, owner) {
            override fun sendTransaction(
                gasPrice: BigInteger?,
                gasLimit: BigInteger?,
                to: String?,
                data: String?,
                value: BigInteger?
            ): EthSendTransaction? {
                return null
            }
        }
        val functionCode = FunctionEncoder.encode(function)
        println("functionCode:$functionCode")
        val callTran: Transaction =
            Transaction.createEthCallTransaction(
                transactionManager.fromAddress,
                coinAddress,
                functionCode
            )!!
        val ethCall = web3j.ethCall(callTran, DefaultBlockParameterName.LATEST).send()
        ethCall?.let {
            var result = ethCall.value
            println("value1:${ethCall.result}")
            println("value1:${ethCall.rawResponse}")
            println("value1:${ethCall.revertReason}")
            println("value1:$result")
            result?.let {
                result = "0"
                if ("0x".equalsIgnoreCase(result) || it.length == 2) {
                    result = "0x0"
                }
            }
            println("value2:$result")
            return BigDecimal(CommonUtils.Hex2Decimal(result)).divide(
                BigDecimal(1000000000000000000.0)
            )
        }
        return null
    }

    fun String.equalsIgnoreCase(anotherString: String): Boolean {
        return if (this == anotherString) true
        else (anotherString != null)
                && (anotherString.length == this.length)
                && regionMatches(0, anotherString, 0, this.length, true);
    }


    /**
     * ethCall 方法
     *
     * @param function        data
     * @param contractAddress to 合约地址
     * @param address         from 地址
     * @return
     */
    private fun callSmartContractFunction(
        function: org.web3j.abi.datatypes.Function, contractAddress: String, address: String
    ): String {
        val encodedFunction = FunctionEncoder.encode(function)
        var response: org.web3j.protocol.core.methods.response.EthCall? = null
        try {
//            response = Web3jManager.getInstance().getWeb3j().ethCall(
//                Transaction.createEthCallTransaction(address, contractAddress, encodedFunction),
//                DefaultBlockParameterName.LATEST
//            ).sendAsync().get()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return if (response == null) "" else response.value
    }


    /**
     * 推送交易
     */

    fun pushTrabsaction() {
        val addressTo: String = Constants.addressTo
        val amountString: String = "1"
        val gasPrice: BigInteger = BigInteger("1")
        web3j?.let {
            val amount = BigDecimal(amountString)
            println("addressTo:$addressTo")
            println("amount:$amount")
            println("gasPrice:$gasPrice")
            println("privateKey:")
            val transactionManager = RawTransactionManager(web3j, Credentials.create(""))
            val transfer = Transfer(web3j, transactionManager)
            val disposable = Observable.just(
                transfer.sendFunds(
                    addressTo,
                    amount,
                    Convert.Unit.ETHER,
                    gasPrice,
                    BigInteger.valueOf(21000)
                )
            )
                .map { transactionReceiptRemoteCall ->
                    //                    LogTool.d<String>(TAG, "pushing")
                    transactionReceiptRemoteCall.send()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ send ->
                    if (send == null) {
                        println("failure send is null")
                    } else {
//                        LogTool.d<String>(TAG, "Transaction complete:")
//                        LogTool.d<String>(TAG, "trans hash=" + send.transactionHash)
//                        LogTool.d<String>(TAG, "block hash" + send.blockHash)
//                        LogTool.d<String>(TAG, "from :" + send.from)
//                        LogTool.d<String>(TAG, "to:" + send.to)
//                        LogTool.d<String>(TAG, "gas used=" + send.gasUsed)
//                        LogTool.d<String>(TAG, "status: " + send.status)
                        val transactionHash = send.transactionHash
                        println("success $transactionHash")
                        println("getHashRaw $transactionHash")
                    }
                }, { throwable ->
                    println("failure:${throwable.message}")
//                    LogTool.e(TAG, throwable.message)
                })
        }


    }


    fun shutDownWeb3j() {
        web3j?.let { it.shutdown() }
    }
}