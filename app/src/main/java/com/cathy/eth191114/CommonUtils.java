package com.cathy.eth191114;

/*
+--------------------------------------+
+ Create by Catherine Liu                                  
+--------------------------------------+
+ 2019/11/21 18:24                                  
+--------------------------------------+
+ Des: 
+--------------------------------------+
*/

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class CommonUtils {
    public static BigDecimal getSTAmount(String input) {
        if (input.startsWith("0x")) {
            input = input.replace("0x", "");
        }
        String hexAmount = input.substring(input.length() - 64, input.length());
        BigDecimal amount = new BigDecimal(new BigInteger(hexAmount, 16).toString());
        BigDecimal bigDecimal = amount.divide(new BigDecimal(1000000000000000000d));
        return bigDecimal;
    }

    public static String getContractAddressTo(String input) {
        if (input == null || "".equals(input) || input.length() != 138) return null;
        String addressTo = "";
        try {
            addressTo = "0x" + input.substring(34, input.length() - 64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addressTo;
    }

    public static BigDecimal bit18(BigInteger bigInteger) {
        BigDecimal amount = new BigDecimal(bigInteger);
        BigDecimal bigDecimal = amount.divide(new BigDecimal(1000000000000000000d));
        return bigDecimal;
    }

    /**
     * @param transactionReceipt * @return
     *                           * @throws IOException
     */
    public static boolean verifyTransaction(TransactionReceipt transactionReceipt, BigInteger ethBlockNumber) throws IOException {
        String status = transactionReceipt.getStatus();
        boolean statusIsSuccess = "0x1".equals(status);
        boolean verifyTrain = ethBlockNumber.subtract(transactionReceipt.getBlockNumber()).compareTo(new BigInteger("12")) > 0;
        return statusIsSuccess && verifyTrain;
    }

    /**
     * @param x * @return
     */
    public static BigInteger Hex2Decimal(String x) {
        if (x.startsWith("0x")) {
            x = x.replace("0x", "");
        }
        return new BigInteger(x, 16);
    }

    /**
     * @param num * @return
     */
    public static String decimal2Hex(Long num) {
        if (num == null) return null;
        return Long.toHexString(num);
    }

}
