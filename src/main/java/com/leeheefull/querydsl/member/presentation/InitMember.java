package com.leeheefull.querydsl.member.presentation;

import com.leeheefull.querydsl.member.domain.Member;
import com.leeheefull.querydsl.member.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.stream.IntStream;

@Profile("local")
@RequiredArgsConstructor
@Component
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {

        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            var teamA = new Team("teamA");
            var teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            IntStream.range(0, 100)
                    .forEach(i -> {
                        var selectedTeam = i % 2 == 0 ? teamA : teamB;
                        em.persist(new Member("member" + i, i, selectedTeam));
                    });
        }

    }

}
