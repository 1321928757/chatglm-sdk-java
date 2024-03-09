package cn.bugstack.chatglm.model;

import lombok.Data;

/**
 * @author 刘仕杰
 * @description 返回结果
 * @Copyright 刘仕杰
 */
@Data
public class ChatCompletionResponse {

    private String data;
    private String meta;

    @Data
    public static class Meta {
        private String task_status;
        private Usage usage;
        private String task_id;
        private String request_id;
    }

    @Data
    public static class Usage {
        private int completion_tokens;
        private int prompt_tokens;
        private int total_tokens;
    }

}
