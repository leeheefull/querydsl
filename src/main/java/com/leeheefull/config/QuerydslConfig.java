package com.leeheefull.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class QuerydslConfig {

    /**
     * <p>의문점: 싱글톤인데, 여러 곳에서 사용하면 동시성 문제가 발생하지 않을까?</p>
     * <p>-> em은 프록시 객체를 통해 주입하기 때문에 괜찮음, 트랜잭션 단위로 각각 바인딩하여 동작함.</p>
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

}
