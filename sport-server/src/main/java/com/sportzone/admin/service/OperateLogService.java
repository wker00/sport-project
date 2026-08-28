package com.sportzone.admin.service;

import com.sportzone.admin.vo.OperateLogVO;
import com.sportzone.common.dto.PageResult;

import java.time.LocalDate;

public interface OperateLogService {

    PageResult<OperateLogVO> listOperateLogs(String module, String type, LocalDate startDate, LocalDate endDate, long page, long size);

    OperateLogVO getOperateLogById(Long id);

    void cleanOperateLog(LocalDate beforeDate);
}
