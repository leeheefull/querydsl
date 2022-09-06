package com.leeheefull.querydsl.member.Infrastructure;

import com.leeheefull.querydsl.member.domain.Member;
import com.leeheefull.querydsl.member.domain.Team;
import com.leeheefull.querydsl.member.dto.MemberSearchCondition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void 회원_아이디로_조회() {
        // given
        Member member = getMember();

        // when
        var findMember = memberRepository.findById(member.getId()).get();

        // then
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void 회원_전부_조회() {
        // given
        Member member = getMember();

        // when
        var result = memberRepository.findAll();

        // then
        assertThat(result).containsExactly(member);
    }

    @Test
    public void 회원_이름으로_조회() {
        // given
        Member member = getMember();

        // when
        var result = memberRepository.findByUsername("member1");

        // then
        assertThat(result).containsExactly(member);
    }

    @Test
    public void 검색() {
        // given
        var teamA = new Team("teamA");
        var teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        var member1 = new Member("member1", 10, teamA);
        var member2 = new Member("member2", 20, teamA);
        var member3 = new Member("member3", 30, teamB);
        var member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // when
        var condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");
        var result = memberRepository.search(condition);

        // then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    public void 페이지_검색() {
        // given
        var teamA = new Team("teamA");
        var teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        var member1 = new Member("member1", 10, teamA);
        var member2 = new Member("member2", 20, teamA);
        var member3 = new Member("member3", 30, teamB);
        var member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // when
        var condition = new MemberSearchCondition();
        var pageRequest = PageRequest.of(0, 3);

        var result = memberRepository.searchPage(condition, pageRequest);

        // then
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

    private Member getMember() {
        var member = new Member("member1", 10);
        memberRepository.save(member);
        return member;
    }

}