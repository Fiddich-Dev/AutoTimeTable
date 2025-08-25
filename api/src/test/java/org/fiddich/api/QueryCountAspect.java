package org.fiddich.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class QueryCountAspect {

    private final QueryCounter queryCounter;

    @Around("execution(* javax.sql.DataSource.getConnection(..))")
    public Object getConnection(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object connection = proceedingJoinPoint.proceed();
        return Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                connection.getClass().getInterfaces(),
                new ConnectionProxyHandler(connection, queryCounter)
        );
    }
}
