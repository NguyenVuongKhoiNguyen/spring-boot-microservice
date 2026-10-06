package com.poly.user.repositories;

import com.poly.user.models.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTokenRepository
    extends JpaRepository<DeviceToken, Long>, JpaSpecificationExecutor<DeviceToken> {}
