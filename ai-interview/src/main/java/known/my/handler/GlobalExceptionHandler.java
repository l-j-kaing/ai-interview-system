package known.my.handler;

import known.my.common.exception.BaseException;
import known.my.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    /**
     * 捕获用户名重复异常
     * @param ex
     * @return
     */
   @ExceptionHandler
    public Result usernameRepeatExceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")){
            //获取重复的用户名
            String username = message.substring(message.indexOf("Duplicate entry") + 15, message.indexOf("for key"));
            return Result.error(username + "已存在");
    }else {
            return Result.error("未知错误");
        }

}}

