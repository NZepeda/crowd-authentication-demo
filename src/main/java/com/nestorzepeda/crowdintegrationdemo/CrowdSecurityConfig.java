package com.nestorzepeda.crowdintegrationdemo;

import com.atlassian.crowd.integration.http.HttpAuthenticator;
import com.atlassian.crowd.integration.http.HttpAuthenticatorImpl;
import com.atlassian.crowd.integration.springsecurity.RemoteCrowdAuthenticationProvider;
import com.atlassian.crowd.integration.springsecurity.user.CrowdUserDetailsServiceImpl;
import com.atlassian.crowd.service.AuthenticationManager;
import com.atlassian.crowd.service.GroupManager;
import com.atlassian.crowd.service.UserManager;
import com.atlassian.crowd.service.cache.*;
import com.atlassian.crowd.service.soap.client.SecurityServerClient;
import com.atlassian.crowd.service.soap.client.SecurityServerClientImpl;
import com.atlassian.crowd.service.soap.client.SoapClientPropertiesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@EnableWebSecurity
public class CrowdSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .inMemoryAuthentication()
//                .withUser("user").password("password").roles("ROLE_USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/securedEndpoint").authenticated()
                .antMatchers("/", "/home").permitAll()
                .antMatchers("/login*").permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }
    public static Properties getProps() throws IOException{
        Properties prop = new Properties();
        try(InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("crowd.properties")){
            prop.load(in);
        }
        return prop;
    }
    @Bean
    public SecurityServerClient securityServerClient() throws IOException{
        return new SecurityServerClientImpl(SoapClientPropertiesImpl.newInstanceFromProperties(getProps()));
    }
    private final BasicCache cache = new CacheImpl(Thread.currentThread().getContextClassLoader().getResource("crowd-ehcache.xml"));

    @Bean
    public AuthenticationManager crowdAuthenticationManager() throws IOException{

        return new SimpleAuthenticationManager(securityServerClient());
    }
    @Bean
    public HttpAuthenticator httpAuthenticator() throws IOException{
        return new HttpAuthenticatorImpl(crowdAuthenticationManager());
    }
    @Bean
    public UserManager userManager() throws IOException{
        return new CachingUserManager(securityServerClient(), cache);
    }
    @Bean
    public GroupManager groupManager() throws IOException{
        return new CachingGroupManager(securityServerClient(), cache);
    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(crowdAuthenticationProvider());
    }
    @Bean
    public CrowdUserDetailsServiceImpl crowdUserDetailsService() throws IOException{
        CrowdUserDetailsServiceImpl cusd = new CrowdUserDetailsServiceImpl();
        cusd.setUserManager(userManager());
        cusd.setGroupMembershipManager(new CachingGroupMembershipManager(securityServerClient(), userManager(),groupManager(),cache));
        cusd.setAuthorityPrefix("ROLE_");
        return cusd;
    }
    @Bean
    RemoteCrowdAuthenticationProvider crowdAuthenticationProvider() throws IOException{
        return new RemoteCrowdAuthenticationProvider(crowdAuthenticationManager(), httpAuthenticator(), crowdUserDetailsService());
    }
}
