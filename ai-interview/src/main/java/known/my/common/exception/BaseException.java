package known.my.common.exception;
import known.my.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public final class BaseException extends RuntimeException {
    /**
     * 状态码
     */
    private final Integer code;


    public BaseException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BaseException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode.getCode();
    }

    public BaseException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BaseException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

}
