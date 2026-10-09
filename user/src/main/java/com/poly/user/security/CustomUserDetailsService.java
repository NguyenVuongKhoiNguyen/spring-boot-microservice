package com.poly.user.security;

import com.poly.user.models.User;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmailAndDelIfFalse(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (Boolean.TRUE.equals(user.getDelIf()) || Boolean.FALSE.equals(user.getActive())) {
      throw new UsernameNotFoundException("User is not active or deleted");
    }

    List<String> roles =
        userRoleRepository.findByUserIdAndDelIfFalse(user.getId()).stream()
            .map(ur -> ur.getRole().getName())
            .toList();

    return new CustomUserDetails(user, roles);
  }
}
