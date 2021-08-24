package sp.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import sp.dto.DownloadPrefix;
import sp.model.Role;
import sp.model.Admin;
import sp.repository.UserRepository;
import sp.service.impl.UserServiceImpl;
import sp.service.impl.FileUserServiceImpl;


@Slf4j
@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig  {

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final DownloadPrefix downloadPrefix;
        private final FileUserServiceImpl userService;
        private final PasswordEncoder passwordEncoder;

        public ApiWebSecurityConfigurationAdapter(DownloadPrefix downloadPrefix, FileUserServiceImpl userService, @Qualifier("v1") PasswordEncoder passwordEncoder) {
            this.downloadPrefix = downloadPrefix;
            this.userService = userService;
            this.passwordEncoder = passwordEncoder;
        }

        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/" + downloadPrefix.getPrefix() + "/**")
                    .authorizeRequests(authorize -> authorize
                            .anyRequest().hasAuthority(Role.FILE_USER.name())
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
        public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

            private final Admin admin;
            private final UserRepository userRepository;
            private final UserServiceImpl userService;
            private final PasswordEncoder passwordEncoder;

            public FormLoginWebSecurityConfigurerAdapter(Admin admin, UserRepository userRepository, UserServiceImpl userService, @Qualifier("v2") PasswordEncoder passwordEncoder) {
                this.admin = admin;
                this.userRepository = userRepository;
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
            }

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                        .authorizeRequests(authorize -> authorize
                                .antMatchers("/")
                                    .permitAll()
                                .antMatchers("/login")
                                    .permitAll()
                                .antMatchers("/user/profile/update")
                                    .hasAuthority(Role.USER.name())
                                .antMatchers("/upload", "/logout", "/list")
                                    .hasAnyAuthority(Role.USER.name(), Role.ADMIN.name())
                                .antMatchers("/user/create", "/user/list")
                                    .hasAuthority(Role.ADMIN.name())
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
                if (userRepository.findByUsername(admin.getUsername()).isPresent()) {
                    throw new IllegalStateException("ошибка: Пользователь с таким именем уже существует");
                }
                UserDetails userDetails = User
                        .withUsername(admin.getUsername())
                        .password("{bcrypt}" + passwordEncoder.encode(admin.getPassword()))
                        .authorities(Role.ADMIN.name())
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
                provider.setUserDetailsService(userService);
                return provider;
            }
        }



}
