package com.aiops.platform.auth.application;

import com.aiops.platform.auth.domain.Role;
import com.aiops.platform.auth.domain.User;
import com.aiops.platform.auth.infrastructure.RoleRepository;
import com.aiops.platform.auth.infrastructure.UserRepository;
import com.aiops.platform.ingestion.domain.Application;
import com.aiops.platform.ingestion.domain.Service;
import com.aiops.platform.ingestion.infrastructure.ApplicationRepository;
import com.aiops.platform.ingestion.infrastructure.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
        UserRepository userRepository,
        RoleRepository roleRepository,
        ApplicationRepository applicationRepository,
        ServiceRepository serviceRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.applicationRepository = applicationRepository;
        this.serviceRepository = serviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Roles if missing
        Role adminRole = seedRoleIfMissing("ADMIN");
        seedRoleIfMissing("OPERATOR");
        seedRoleIfMissing("VIEWER");

        // Seed Admin user if missing
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@aiops.com");
            admin.setPassword(passwordEncoder.encode("adminpass"));
            admin.setRole(adminRole);
            admin.setActive(true);
            userRepository.save(admin);
        }

        // Seed Applications if missing
        Application app = applicationRepository.findByName("demo-app")
            .orElseGet(() -> applicationRepository.save(new Application("demo-app", "Demo application for AIOps platform")));

        // Seed Services in order (will get auto-incremented IDs 1, 2, 3)
        if (serviceRepository.count() == 0) {
            serviceRepository.save(new Service(app, "payment-service", "production")); // ID 1
            serviceRepository.save(new Service(app, "auth-service", "production"));    // ID 2
            serviceRepository.save(new Service(app, "order-service", "production"));   // ID 3
        }
    }

    private Role seedRoleIfMissing(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
