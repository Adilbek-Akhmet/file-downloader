package sp.config;

import lombok.AllArgsConstructor;
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
import sp.model.Admin;
import sp.model.Role;
import sp.service.impl.UserServiceImpl;



@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig  {

    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;

    @Configuration
    @Order(1)
    @AllArgsConstructor
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final DaoAuthenticationProvider daoAuthenticationProvider;
        private final DownloadPrefix downloadPrefix;

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
            auth.authenticationProvider(daoAuthenticationProvider);
        }

    }

        @Configuration
        @AllArgsConstructor
        public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

            private final Admin admin;
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
                                .hasAuthority("ADMIN")
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
                UserDetails userDetails = User
                        .withUsername(admin.getUsername())
                        .password(passwordEncoder.encode(admin.getPassword()))
                        .authorities(Role.ADMIN.name())
                        .build();
                return new InMemoryUserDetailsManager(userDetails);
            }

            @Override
            protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(userDetailsService());
            }
        }

    @Bean()
    public DaoAuthenticationProvider daoAuthenticationProviderUser() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }
}
