package com.sparta.bluemoon.lot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findByNicknameAndPersonalInfo(String nickname, boolean personalInfo);

}
