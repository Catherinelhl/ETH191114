package com.cathy.eth191114.constants;

/**
 * @author catherine.brainwilliam
 * @since 2019/1/23
 * <p>
 * 定义ETH相关参数的常量
 */
public class ETHParamConstants {
    //当前是否测试环境
    public static boolean isTest = true;

    //根据是否是测试环境，返回网络参数
    public static final String NetworkParameter = isTest ? "https://ropsten.infura.io/v3/7a96432ff7074b898ce5ed298a4d8356"
            : "https://mainnet.infura.io/v3/7a96432ff7074b898ce5ed298a4d8356";

}
