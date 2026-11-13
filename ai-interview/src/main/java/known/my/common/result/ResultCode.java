
package known.my.common.result;

public enum ResultCode {

    SUCCESS(200, "请求成功"),

    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token无效"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    BUSINESS_CONFLICT(409, "业务状态冲突"),

    FILE_TOO_LARGE(413, "上传文件过大"),
    FILE_TYPE_NOT_SUPPORTED(415, "不支持的文件类型"),

    INTERNAL_ERROR(500, "服务器内部错误"),
    AI_SERVICE_ERROR(502, "AI服务处理异常"),
    AI_SERVICE_UNAVAILABLE(503, "AI服务暂不可用");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
