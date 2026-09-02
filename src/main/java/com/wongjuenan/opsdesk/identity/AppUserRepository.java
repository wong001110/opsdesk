package com.wongjuenan.opsdesk.identity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmailNormalized(String emailNormalized);
}
