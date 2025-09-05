package org.example.security.service;

import lombok.RequiredArgsConstructor;
import org.example.security.dto.JoinDto;
import org.example.security.entity.UserEntity;
import org.example.security.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void join(JoinDto joinDto) {

        //중복 검증
        if(userRepository.existsByUsername(joinDto.getUsername())){
            return;
        }

        UserEntity user = new UserEntity();
        user.setUsername(joinDto.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(joinDto.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }
}
