package com.substring.auth.auth_app_backend;

import com.substring.auth.auth_app_backend.Config.AppConstants;
import com.substring.auth.auth_app_backend.Repositories.RoleRepository;
import com.substring.auth.auth_app_backend.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class AuthAppBackendApplication implements CommandLineRunner {

	@Autowired
	public RoleRepository roleRepository;
	public static void main(String[] args) {
		SpringApplication.run(AuthAppBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
     roleRepository.findByname("ROLE_"+AppConstants.ADMIN_ROLE).ifPresentOrElse(role->{
		 System.out.println("ROLE already exists");
	 },()->{
		 Role role=new Role();
		 role.setName("ROLE_"+AppConstants.ADMIN_ROLE);
		 role.setId(UUID.randomUUID());
		 roleRepository.save(role);
	 });
	roleRepository.findByname("ROLE_"+AppConstants.GUEST_ROLE).ifPresentOrElse(role->{
		System.out.println("ROLE already exists");
	},()->{
		Role role=new Role();
		role.setName("ROLE_"+AppConstants.GUEST_ROLE);
		role.setId(UUID.randomUUID());
		roleRepository.save(role);
	});
	}
}
