package com.leeheefull.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class AopComponent {

    /**
     * cut() 메서드 실행 이전에
     */
    @Before(value = "cut()")
    public void before(JoinPoint joinPoint) {
        log.info(">>>>>>> before: {}", joinPoint);
    }

    /**
     * cut 메서드가 정상 종료 됐을 경우
     */
    @AfterReturning(value = "cut()", returning = "obj")
    public void afterReturning(JoinPoint joinPoint, Object obj) {
        log.info(">>>>>>> afterReturning: {}, {}", joinPoint, obj);
    }

    /**
     * cut 메서드가 실행된 후
     */
    @After(value = "cut()")
    public void after(JoinPoint joinPoint) {
        log.info(">>>>>>> after: {}", joinPoint);
    }

    /**
     * cut 메서드에서 예외가 발생했을 경우
     */
    @AfterThrowing(value = "cut()")
    public void afterThrowing(JoinPoint joinPoint) {
        log.info(">>>>>>> afterThrowing: {}", joinPoint);
    }

    /**
     * 앞뒤 체크
     */
    @Around(value = "cut() && enableTimer()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        joinPoint.proceed();
        stopWatch.stop();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        log.info(">>>>>>> around -> 실행 메서드: {}, 실행 시간: {}", methodName, stopWatch.getTotalTimeMillis());
    }

    /**
     * 해당 패키지 하위 클래스들 전부 적용하겠다고 지점 설정
     */
    @Pointcut("execution(* com.leeheefull.aop.controller..*.*(..))")
    private void cut() {
    }

    @Pointcut("@annotation(com.leeheefull.aop.annotation.Timer)")
    private void enableTimer() {
    }

}
