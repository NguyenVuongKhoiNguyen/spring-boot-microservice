package com.poly.user.repositories;

import com.poly.user.models.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserImageRepository
    extends JpaRepository<UserImage, Long>, JpaSpecificationExecutor<UserImage> {}
