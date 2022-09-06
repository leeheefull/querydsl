package com.leeheefull.querydsl.member.Infrastructure;

import com.leeheefull.querydsl.member.dto.MemberSearchCondition;
import com.leeheefull.querydsl.member.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable);

}
