package com.sportzone.interceptors;

import com.sportzone.utils.JwtUtils;
import com.sportzone.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                response.setStatus(401);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
                return false;
            }

            if (!token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }

            Map<String, Object> map = jwtUtils.parseToken(token);
            Object idObj = map.get("id");
            Long id = ((Number) idObj).longValue();

            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String redisToken = ops.get("admin_token" + id);

            if (redisToken == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
                return false;
            }

            String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            if (!redisToken.equals(pureToken)) {
                response.setStatus(401);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":401,\"message\":\"账号在其他设备登录\"}");
                return false;
            }

            ThreadLocalUtil.set(id);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
