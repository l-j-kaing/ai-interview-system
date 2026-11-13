package known.my.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private Integer code; //编码
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 200;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 200;
        return result;
    }

    public static <T> Result<T> error(Integer code ,String msg, T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.msg = msg;
        result.code = code;
        return result;
    }

    public static <T> Result<T> error(Integer code ,String msg) {
        return error(code,msg,null);
    }
    public static <T> Result<T> error(String msg) {
        return error(500,msg,null);
    }
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMsg());
    }

}
