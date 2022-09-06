package com.leeheefull.querydsl.member.domain;

import com.leeheefull.querydsl.member.dto.MemberDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.List;

import static com.leeheefull.querydsl.member.domain.QMember.member;
import static com.leeheefull.querydsl.member.domain.QTeam.team;
import static com.querydsl.core.types.Projections.constructor;
import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
//@Commit
public class MemberTest {

    @PersistenceContext
    private EntityManager em;

    @PersistenceUnit
    private EntityManagerFactory emf;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void JPQL로_member_찾기() {
        var findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void Querydsl로_member_찾기() {
        // 같은 테이블을 조인할 경우
//        QMember m1 = new QMember("m1");

        var findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * <h1>querydsl 검색 조건 정리 </h1></br>
     * <p>member.username.eq("member1") // username = 'member1'</p>
     * <p>member.username.ne("member1") //username != 'member1'</p>
     * <p>member.username.eq("member1").not() // username != 'member1'</p>
     * <p>member.username.isNotNull() //이름이 is not null</p>
     * <p>member.age.in(10, 20) // age in (10,20)</p>
     * <p>member.age.notIn(10, 20) // age not in (10, 20)</p>
     * <p>member.age.between(10,30) //between 10, 30</p>
     * <p>member.age.goe(30) // age >= 30</p>
     * <p>member.age.gt(30) // age > 30</p>
     * <p>member.age.loe(30) // age <= 30</p>
     * <p>member.age.lt(30) // age < 30</p>
     * <p>member.username.like("member%") //like 검색</p>
     * <p>member.username.contains("member") // like ‘%member%’ 검색</p>
     * <p>member.username.startsWith("member") //like ‘member%’ 검색</p>
     */
    @Test
    public void 검색조건_사용() {
        var findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void 검색조건_param으로_사용() {
        var findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * <h1>fetch 종류</h1>
     *
     * <p>fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환</p>
     * <p>fetchOne() : 단 건 조회</p>
     * <p>***** 결과가 없으면 : null</p>
     * <p>***** 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException</p>
     * <p>fetchFirst() : limit(1).fetchOne()</p>
     * <p>fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행</p>
     * <p>fetchCount() : count 쿼리로 변경해서 count 수 조회</p>
     */
    @Test
    public void fetch_비교() {
        var results = queryFactory
                .selectFrom(member)
                .fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
    }

    /**
     * <h1>회원 정렬 순서</h1>
     *
     * <p>1. 회원 나이 내림차순(desc)</p>
     * <p>2. 회원 이름 올림차순(asc)</p>
     * <p>단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)</p>
     */
    @Test
    public void 정렬() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        var result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        assertThat(result.get(0).getUsername()).isEqualTo("member5");
        assertThat(result.get(1).getUsername()).isEqualTo("member6");
        assertThat(result.get(2).getUsername()).isNull();
    }

    @Test
    public void 페이징_처리() {
        var result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void 전체_조회_수_페이징_처리() {
        var queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * <h1>집합 함수</h1>
     * <p>JPQL ver.</p>
     * <p>select</p>
     * <p>****COUNT(m), //회원수</p>
     * <p>****SUM(m.age), //나이 합</p>
     * <p>****AVG(m.age), //평균 나이</p>
     * <p>****MAX(m.age), //최대 나이</p>
     * <p>****MIN(m.age) //최소 나이</p>
     * <p>from Member m</p>
     */
    @Test
    public void 집합_합수() {
        var tuple = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetchOne();

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * <h1>group by</h1
     */
    @Test
    public void 팀의_이름과_각_팀의_평균_연령_구하기() {
        var result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        var teamA = result.get(0);
        var teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * <h1>기본 조인</h1>
     */
    @Test
    public void 팀A에_소속된_모든_회원_찾기() {
        var result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .contains("member1", "member2");
    }

    /**
     * <h1>theta join</h1>
     *
     * <p>연관관계가 없는 두 테이블 조인</p>
     * <p>on 절을 사용해서도 가능함.</p>
     * <p>물론 DB가 성능 최적화함.</p>
     */
    @Test
    public void 연관관계가_없는_두_테이블_조인() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        var result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * <h1>조인 대상 필터링</h1>
     * <p>JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'</p>
     * <p>SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'</p>
     */
    @Test
    public void 회원과_팀을_조인하면서_팀_이름이_teamA인_팀만_조인_단_회원은_모두_조회() {
        var result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))  // = .where(team.name.eq("teamA"))
                .fetch();

        result.forEach(tuple -> System.out.println("tuple=" + tuple));
    }

    /**
     * <h1>연관관계 없는 엔티티 외부 조인</h1>
     * <p>예)회원의 이름과 팀의 이름이 같은 대상 외부 조인</p>
     * <p>JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name</p>
     * <p>SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name</p>
     */
    @Test
    public void 회원의_이름과_팀의_이름이_같은_대상_외부조인() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        var result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team) // outer join
                .on(member.username.eq(team.name))
                .fetch();

        result.forEach(tuple -> System.out.println("tuple=" + tuple));
    }

    /**
     * <h1>페치 조인 미적용</h1>
     */
    @Test
    public void 지연로딩으로_회원과_팀_SQL_쿼리_각각_실행() {
        em.flush();
        em.clear();

        var findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        var loaded = emf.getPersistenceUnitUtil()
                .isLoaded(findMember.getTeam());

        assertThat(loaded)
                .as("페치 조인 미적용")
                .isFalse();
    }

    /**
     * <h1>페치 조인 적용</h1>
     */
    @Test
    public void 즉시로딩으로_회원과_팀_SQL_쿼리_조인으로_한번에_조회() {
        em.flush();
        em.clear();

        var findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        var loaded = emf.getPersistenceUnitUtil()
                .isLoaded(findMember.getTeam());

        System.out.println(findMember);

        assertThat(loaded).as("페치 조인 적용")
                .isTrue();
    }

    /**
     * <h1>서브 쿼리, where절</h1>
     */
    @Test
    public void 나이가_가장_많은_회원_조회() {
        var memberSub = new QMember("memberSub");

        var result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                )).fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * <h1>서브 쿼리, select절</h1>
     */
    @Test
    public void 나이가_평균_나이_이상인_회원() {
        var memberSub = new QMember("memberSub");

        var fetch = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();

        fetch.forEach(System.out::println);
    }

    /**
     * stringValue(), enum에서 자주 사용
     */
    @Test
    public void concat_사용() {
        var result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        result.forEach(System.out::println);
    }

    /**
     * <h1>프로젝션</h1>
     * <p>table data를 검색할 때, 반환되는 열을 말함</p>
     */
    @Test
    public void 프로젝션_대상이_하나일_경우() {
        var result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    public void 프로젝션_대상이_tuple인_경우() {
        var result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        result.forEach(tuple -> {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " + tuple.get(member.age));
        });
    }

    @Test
    public void 프로젝션_대상이_DTO인_경우_byJPQL() {
        var result = em.createQuery("select new com.leeheefull.querydsl.member.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        result.forEach(System.out::println);
    }

    @Test
    public void 프로젝션_대상이_DTO인_경우_byQuerydsl() {
        var result = queryFactory
                .select(constructor(
                        MemberDto.class,
                        member.username.as("name"), // = ExpressionUtils.as(member.username, "name") : 필드나, 서브 쿼리에 별칭 적용
                        member.age
                )).from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    /**
     * <h1>동적 쿼리</h1>
     * <p>BooleanBuilder 사용</p>
     */
    @Test
    public void 이름과_나이로_회원_검색() {
        var usernameParam = "member1";
        var ageParam = 10;

        var result = searchMember1(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, int ageParam) {
        var builder = new BooleanBuilder();

        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }
        builder.and(member.age.eq(ageParam));

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    /**
     * <h1>동적 쿼리</h1>
     * <p>다중 parameter</p>
     */
    @Test
    public void 다중_이름과_나이로_회원_검색() {
        var result = queryFactory
                .selectFrom(member)
                .where(allEq("member1", 10))
                .fetch();

        assertThat(result.size()).isEqualTo(1);
    }

    private BooleanExpression allEq(String usernameParam, int ageParam) {
        return usernameParamEq(usernameParam).and(ageParamEq(ageParam));
    }

    private BooleanExpression usernameParamEq(String usernameParam) {
        if (usernameParam == null) {
            return null;    // where 절의 null은 무시함.
        }
        return member.username.eq(usernameParam);
    }

    private BooleanExpression ageParamEq(int ageParam) {
        return member.age.eq(ageParam);
    }

    /**
     * <h1>Bulk 연산</h1>
     */
    @Test
    public void 쿼리_한_번으로_대량_데이터_수정() {
        var count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        em.flush();
        // 영속성 컨텍스트 테이터를 해제하지 않으면,
        // 변경된 DB 값과 기존에 있던 영속성 컨텍스트 값이 일치하지 않는 문제가 발생하게 됨
        em.clear();

        var result = queryFactory
                .selectFrom(member)
                .fetch();

        assertThat(count).isEqualTo(2);
        assertThat(result)
                .extracting("username")
                .contains("비회원");
    }

}
