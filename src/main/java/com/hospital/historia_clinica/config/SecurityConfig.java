package com.hospital.historia_clinica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String MEDICO = "MEDICO";
    private static final String RECEPCIONISTA = "RECEPCIONISTA";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Publicas
                        .requestMatchers("/login", "/error").permitAll()

                        // ADMINISTRADOR: usuarios, roles y bitacora
                        .requestMatchers("/usuarios/**", "/roles/**", "/auditoria/**",
                                "/api/usuarios/**", "/api/roles/**").hasRole(ADMIN)

                        // MEDICO: historias clinicas y su informacion
                        .requestMatchers("/historias/**", "/antecedentes/**", "/antecedentes-familiares/**",
                                "/alergias/**", "/condiciones/**",
                                "/api/historias/**", "/api/antecedentes/**", "/api/antecedentes-familiares/**",
                                "/api/alergias/**", "/api/condiciones/**").hasAnyRole(ADMIN, MEDICO)

                        // RECEPCIONISTA: citas (modulo de citas, aun no implementado en este proyecto)
                        .requestMatchers("/citas/**", "/api/citas/**").hasAnyRole(ADMIN, RECEPCIONISTA)

                        // Pacientes: los tres roles
                        .requestMatchers("/pacientes/**", "/api/pacientes/**").hasAnyRole(ADMIN, MEDICO, RECEPCIONISTA)

                        // Todo lo demas (panel) solo requiere haber iniciado sesion
                        .anyRequest().authenticated()
                )
                // Login con formulario propio; despues de entrar siempre se va al panel
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/panel", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }
}
