package sp.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import sp.dto.DownloadPrefix;
import sp.model.Admin;
import sp.model.Role;
import sp.model.SuperAdmin;
import sp.repository.AdminRepository;
import sp.service.impl.AdminServiceImpl;
import sp.service.impl.UserServiceImpl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@Slf4j
@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig  {

    @Configuration
    @Order(1)
    @AllArgsConstructor
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final DownloadPrefix downloadPrefix;
        private final UserServiceImpl userService;
        private final PasswordEncoder passwordEncoder;

        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/" + downloadPrefix.getPrefix() + "/**")
                    .authorizeRequests(authorize -> authorize
                            .anyRequest().hasAuthority("USER")
                    )
                    .httpBasic();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(daoAuthenticationProviderUser());
        }

        @Bean()
        public DaoAuthenticationProvider daoAuthenticationProviderUser() {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setPasswordEncoder(passwordEncoder);
            provider.setUserDetailsService(userService);
            return provider;
        }

    }

        @Configuration
        @AllArgsConstructor
        public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

            private final SuperAdmin superAdmin;
            private final AdminRepository adminRepository;
            private final AdminServiceImpl adminService;
            private final PasswordEncoder passwordEncoder;

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                        .authorizeRequests(authorize -> authorize
                                .antMatchers("/")
                                    .permitAll()
                                .antMatchers("/login")
                                    .permitAll()
                                .antMatchers("/upload", "/logout", "/list")
                                    .hasAnyAuthority(Role.ADMIN.name(), Role.SUPER_ADMIN.name())
                                .antMatchers("/admin/create", "/admin/list")
                                .hasAuthority(Role.SUPER_ADMIN.name())
                        )
                        .formLogin()
                            .loginPage("/")
                            .loginProcessingUrl("/login")
                            .failureUrl("/login-error")
                            .defaultSuccessUrl("/upload")
                        .and()
                        .logout();
            }

            @Override
            protected UserDetailsService userDetailsService() {
                if (adminRepository.findByUsername(superAdmin.getUsername()).isPresent()) {
                    throw new IllegalStateException("ошибка: Super Admin с таким именем уже существует в admin table");
                }
                UserDetails userDetails = User
                        .withUsername(superAdmin.getUsername())
                        .password(passwordEncoder.encode(superAdmin.getPassword()))
                        .authorities(Role.SUPER_ADMIN.name())
                        .build();
                return new InMemoryUserDetailsManager(userDetails);
            }

            @Override
            protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(userDetailsService());
                auth.authenticationProvider(daoAuthenticationProviderAdmin());
            }

            @Bean()
            public DaoAuthenticationProvider daoAuthenticationProviderAdmin() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setPasswordEncoder(passwordEncoder);
                provider.setUserDetailsService(adminService);
                return provider;
            }
        }



}
