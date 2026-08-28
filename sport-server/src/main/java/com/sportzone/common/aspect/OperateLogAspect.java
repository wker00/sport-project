package com.sportzone.common.aspect;

import cn.hutool.json.JSONUtil;
import com.sportzone.admin.dto.AdminLoginDTO;
import com.sportzone.admin.entity.Admin;
import com.sportzone.admin.mapper.AdminMapper;
import com.sportzone.common.annotation.OperateLog;
import com.sportzone.common.event.OperateLogEvent;
import com.sportzone.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class OperateLogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final AdminMapper adminMapper;
    private final HttpServletRequest request;
    private final ExpressionParser parser = new SpelExpressionParser();

    public OperateLogAspect(ApplicationEventPublisher eventPublisher, AdminMapper adminMapper, HttpServletRequest request) {
        this.eventPublisher = eventPublisher;
        this.adminMapper = adminMapper;
        this.request = request;
    }

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        long start = System.currentTimeMillis();
        String params = serializeParams(joinPoint.getArgs());
        String description = resolveDescription(operateLog.description(), joinPoint);

        String result = "success";
        String errorMsg = null;
        try {
            Object ret = joinPoint.proceed();
            return ret;
        } catch (Exception e) {
            result = "fail";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - start;
            Long adminId = null;
            String adminName = null;
            try {
                adminId = ThreadLocalUtil.getUserId();
            } catch (Exception ignored) {
            }
            if (adminId != null) {
                Admin admin = adminMapper.selectById(adminId);
                if (admin != null) {
                    adminName = admin.getUsername();
                }
            } else {
                for (Object arg : joinPoint.getArgs()) {
                    if (arg instanceof AdminLoginDTO dto) {
                        adminName = dto.getUsername();
                        break;
                    }
                }
            }

            OperateLogEvent event = new OperateLogEvent(
                    adminId, adminName,
                    operateLog.module(), operateLog.type(), description,
                    request.getMethod(), request.getRequestURI(),
                    params, result, errorMsg,
                    getClientIp(), costTime
            );
            eventPublisher.publishEvent(event);
        }
    }

    private String serializeParams(Object[] args) {
        if (args == null || args.length == 0) return null;
        List<Object> filtered = Arrays.stream(args)
                .filter(arg -> !(arg instanceof MultipartFile)
                        && !(arg instanceof MultipartFile[])
                        && !(arg instanceof HttpServletRequest)
                        && !(arg instanceof jakarta.servlet.http.HttpServletResponse))
                .collect(Collectors.toList());
        if (filtered.isEmpty()) return null;
        try {
            return JSONUtil.toJsonStr(filtered.size() == 1 ? filtered.get(0) : filtered);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveDescription(String template, ProceedingJoinPoint joinPoint) {
        if (template == null || template.isEmpty()) return "";
        if (!template.contains("#")) return template;
        try {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = getParamNames(joinPoint);
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    if (!(args[i] instanceof MultipartFile)
                            && !(args[i] instanceof MultipartFile[])
                            && !(args[i] instanceof HttpServletRequest)) {
                        ctx.setVariable(paramNames[i], args[i]);
                    }
                }
            }
            return parser.parseExpression(template).getValue(ctx, String.class);
        } catch (Exception e) {
            return template;
        }
    }

    private String[] getParamNames(ProceedingJoinPoint joinPoint) {
        try {
            var method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
            var params = method.getParameters();
            return java.util.Arrays.stream(params)
                    .map(java.lang.reflect.Parameter::getName)
                    .toArray(String[]::new);
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
