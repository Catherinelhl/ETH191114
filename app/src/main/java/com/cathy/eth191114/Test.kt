package com.cathy.eth191114

import com.cathy.eth191114.constants.Constants
import com.cathy.eth191114.constants.ETHParamConstants
import com.cathy.eth191114.tool.LogTool
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.util.test.Test
import org.jetbrains.annotations.TestOnly
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger


/*
+--------------------------------------+
+ Create by Catherine Liu                                  
+--------------------------------------+
+ 2019/11/14 22:32                                  
+--------------------------------------+
+ Des: 
+--------------------------------------+
*/

@TestOnly
fun main() {
    //step 1 创建房间
//    PrivateKey:70894486279756432157224976880339036365675198113074183826699610870590116898825
//    PublicKey:6208972924420545558134764276104808487339782137951799675124507908618225250190716508719454803697268030747092323269358473534415193745538444937623819315324500
//    Address:2ffbcc70c95bddbdb4379c8eed4d3260f7da52d7
//    createAccountDirectly()
    //step 2 创建客户端
    createClient(true)
    //step3:获取余额
    getBalance()
    //step 4:发起交易

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
    val balance = web3j.ethGetBalance(Constants.address, DefaultBlockParameterName.EARLIEST).send()
    //格式转化 wei-ether
    val balanceETH =
        Convert.fromWei(balance.balance.toString(), Convert.Unit.ETHER).toPlainString() + " ether"
    println(balanceETH)
}

/**
 * 推送交易
 */
private val TAG = Test::class.simpleName

fun pushTrabsaction() {
    val addressTo: String = Constants.addressTo
    val amountString: String = "1"
    val gasPrice:BigInteger = BigInteger("1")
        web3j?.let {
            val amount = BigDecimal(amountString)
            LogTool.d<String>(TAG, "addressTo:$addressTo")
            LogTool.d<String>(TAG, "amount:$amount")
            LogTool.d<String>(TAG, "gasPrice:$gasPrice")
            LogTool.d<String>(TAG, "privateKey:")
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
                    LogTool.d<String>(TAG, "pushing")
                    transactionReceiptRemoteCall.send()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ send ->
                    if (send == null) {
                        println("failure send is null")
                    } else {
                        LogTool.d<String>(TAG, "Transaction complete:")
                        LogTool.d<String>(TAG, "trans hash=" + send.transactionHash)
                        LogTool.d<String>(TAG, "block hash" + send.blockHash)
                        LogTool.d<String>(TAG, "from :" + send.from)
                        LogTool.d<String>(TAG, "to:" + send.to)
                        LogTool.d<String>(TAG, "gas used=" + send.gasUsed)
                        LogTool.d<String>(TAG, "status: " + send.status)
                       val transactionHash = send.transactionHash
                        println("success $transactionHash")
                        println("getHashRaw $transactionHash")
                    }
                }, { throwable ->
                    println("failure:${throwable.message}")
                    LogTool.e(TAG, throwable.message)
                })
        }


}


fun shutDownWeb3j() {
    web3j?.let { it.shutdown() }
}