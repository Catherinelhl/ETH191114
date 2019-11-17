package com.cathy.eth191114.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/22
 * <p>
 * 获取ETH交易列表响应数据
 */
public class ETHTXListResponse implements Serializable {
    private String status;
    private String message;
    private List<TXListBean> result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TXListBean> getResult() {
        return result;
    }

    public void setResult(List<TXListBean> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ETHTXListResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
