package com.sportzone.common.event;

import com.sportzone.admin.entity.OperateLog;
import com.sportzone.admin.mapper.OperateLogMapper;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OperateLogListener {

    private final OperateLogMapper operateLogMapper;

    public OperateLogListener(OperateLogMapper operateLogMapper) {
        this.operateLogMapper = operateLogMapper;
    }

    @Async
    @EventListener
    public void handleOperateLog(OperateLogEvent event) {
        OperateLog log = new OperateLog();
        log.setAdminId(event.getAdminId());
        log.setUsername(event.getUsername());
        log.setModule(event.getModule());
        log.setType(event.getType());
        log.setDescription(event.getDescription());
        log.setMethod(event.getMethod());
        log.setUrl(event.getUrl());
        log.setParams(event.getParams());
        log.setResult(event.getResult());
        log.setErrorMsg(event.getErrorMsg());
        log.setIp(event.getIp());
        log.setCostTime(event.getCostTime());
        operateLogMapper.insert(log);
    }
}
