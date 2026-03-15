package com.substring.auth.auth_app_backend.Config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class projectconfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
