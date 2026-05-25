package hanyang.RentalManagementSystem.common.config;

import hanyang.RentalManagementSystem.common.entity.User;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByUserLoginIdAndIsDeletedFalse("admin")) {
            User admin = User.builder()
                    .userLoginId("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .userName("관리자")
                    .role("ADMIN")
                    .email("admin@team3.com")
                    .contact("")
                    .build();
            userRepository.save(admin);
            log.info("[INIT] 관리자 계정 생성 완료 (admin / admin123)");
        }
    }
}
