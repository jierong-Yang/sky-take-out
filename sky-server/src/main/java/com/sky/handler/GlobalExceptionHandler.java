package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
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
     * 处理sql异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        log.info("SQL异常信息：{}", message); // 添加日志记录原始异常消息
        
        // 更灵活地检测唯一性冲突
        if(message.contains("Duplicate") || message.contains("duplicate")){
            // 尝试从异常消息中提取重复的值
            try {
                // 针对MySQL格式: Duplicate entry 'username' for key 'idx_username'
                if(message.contains("'")){
                    int start = message.indexOf("'") + 1;
                    int end = message.indexOf("'", start);
                    if(start > 0 && end > start){
                        String duplicateValue = message.substring(start, end);
                        return Result.error(duplicateValue + MessageConstant.ALREADY_EXISTS);
                    }
                }
                // 备用方案：使用正则表达式提取
                return Result.error("数据已存在");
            } catch (Exception e) {
                log.error("提取重复值失败", e);
                return Result.error("数据已存在");
            }
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
}