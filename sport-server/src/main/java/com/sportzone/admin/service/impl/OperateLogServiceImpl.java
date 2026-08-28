package com.sportzone.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sportzone.admin.entity.OperateLog;
import com.sportzone.admin.mapper.OperateLogMapper;
import com.sportzone.admin.service.OperateLogService;
import com.sportzone.admin.vo.OperateLogVO;
import com.sportzone.common.dto.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class OperateLogServiceImpl implements OperateLogService {

    private final OperateLogMapper operateLogMapper;

    public OperateLogServiceImpl(OperateLogMapper operateLogMapper) {
        this.operateLogMapper = operateLogMapper;
    }

    @Override
    public PageResult<OperateLogVO> listOperateLogs(String module, String type, LocalDate startDate, LocalDate endDate, long page, long size) {
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null) {
            wrapper.eq(OperateLog::getModule, module);
        }
        if (type != null) {
            wrapper.eq(OperateLog::getType, type);
        }
        if (startDate != null) {
            wrapper.ge(OperateLog::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(OperateLog::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(OperateLog::getCreateTime);

        Page<OperateLog> p = operateLogMapper.selectPage(new Page<>(page, size), wrapper);
        PageResult<OperateLogVO> result = new PageResult<>();
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        result.setPages(p.getPages());
        result.setRecords(p.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    @Override
    public OperateLogVO getOperateLogById(Long id) {
        OperateLog log = operateLogMapper.selectById(id);
        if (log == null) {
            throw new RuntimeException("操作日志不存在");
        }
        return toVO(log);
    }

    @Override
    public void cleanOperateLog(LocalDate beforeDate) {
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperateLog::getCreateTime, beforeDate.atStartOfDay());
        operateLogMapper.delete(wrapper);
    }

    private OperateLogVO toVO(OperateLog log) {
        OperateLogVO vo = new OperateLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }
}
