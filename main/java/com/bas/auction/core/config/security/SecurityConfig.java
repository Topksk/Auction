package com.bas.auction.core.config.security;

import com.bas.auction.auth.dao.AuthDAO;
import com.bas.auction.auth.dao.SessionDAO;
import com.bas.auction.auth.service.AuthService;
import com.bas.auction.auth.service.UserCertInfoService;
import com.bas.auction.core.Conf;
import com.bas.auction.core.config.MDCFilter;
import com.bas.auction.core.config.RootRedirectFilter;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.req.dao.RequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final int BCRYPT_LOG2_ROUNDS = 12;
    private static final int TOKEN_VALIDITY_SECONDS = 168 * 60 * 60;
    private static final String REMEMBER_ME_KEY = UUID.randomUUID().toString();

    @Autowired
    private SessionDAO sessionDAO;
    @Autowired
    private UserCertInfoService userCertInfoDAO;
    @Autowired
    private AuthService authService;
    @Autowired
    private Conf conf;
    @Autowired
    private CustomerDAO customerDAO;
    @Autowired
    private SupplierDAO supplierDAO;
    @Autowired
    private AuthDAO authDAO;
    @Autowired
    private RequestDAO reqDAO;
    @Autowired
    private com.bas.auction.req.draft.service.ReqDraftService ReqDraftService;



    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, UserDetailsManager userDetailsManager) throws Exception {
        auth.userDetailsService(userDetailsManager)
                .passwordEncoder(new BCryptPasswordEncoder(BCRYPT_LOG2_ROUNDS)).and();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureFormLogin(http);
        configureAuthorization(http);
        configureRememberMeServices(http);
        configureLogout(http);
        http.requestCache().disable()
                .headers().disable()
                .exceptionHandling().accessDeniedHandler(new AccessDeniedHandlerImpl())
                .authenticationEntryPoint(new AuthenticationEntryPointImpl()).and()
                .csrf().disable()
                .addFilterAfter(new MDCFilter(), FilterSecurityInterceptor.class)
                .addFilterAfter(new RootRedirectFilter(conf), MDCFilter.class);
    }

    private void configureFormLogin(HttpSecurity http) throws Exception {
        String loginUrl = conf.getLoginUrl();
        FormLoginConfigurerImpl<HttpSecurity> formLoginConfigurer = new FormLoginConfigurerImpl<>(usernamePasswordAuthenticationFilter());
        http.apply(formLoginConfigurer);
        formLoginConfigurer
                .loginProcessingUrl("/login")
                .loginPage(loginUrl)
                .failureUrl(loginUrl + "#INVALID_CREDENTIALS")
                .successHandler(authenticationSuccessHandler())
                .permitAll();
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandlerImpl(conf, customerDAO, supplierDAO, authDAO, reqDAO, ReqDraftService);
    }

    private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter() {
        return new UsernamePasswordAuthenticationFilterExt(userCertInfoDAO, authService);
    }

    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/auth","/regist/**","/reqs/**","/sprav/**","/plan/**", "/planim/**", "/", "supplierProfile/employees/current").permitAll()
                .antMatchers("/admin/**", "/search/customers/**", "/search/suppliers/**", "plans/templates/**").hasRole("ADMIN")
                .antMatchers("/neg/**",  "/customerProfile/**", "/search/plans/**").hasAnyRole("CUSTOMER", "ADMIN")
                .antMatchers("/customer/suppliers/**").hasAnyRole("CUSTOMER")
                .antMatchers("/bid/**", "/search/bids/**").hasAnyRole("SUPPLIER", "ADMIN")
                .anyRequest().authenticated();
    }

    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout()
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .permitAll();
    }

    private void configureRememberMeServices(HttpSecurity http) throws Exception {
        UserDetailsService userDetailsService = http.getSharedObject(UserDetailsService.class);
        RememberMeServices rememberMeServices =
                new JdbcTokenBasedRememberMeServicesImpl(conf, REMEMBER_ME_KEY, userDetailsService,
                        TOKEN_VALIDITY_SECONDS, sessionDAO);
        http.rememberMe()
                .rememberMeServices(rememberMeServices)
                .key(REMEMBER_ME_KEY);
    }
}