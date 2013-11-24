package org.minig.security;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MailAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MailAuthenticationToken authenticated;

        String[] split = authentication.getName().split("@");

        if (split == null || split.length != 2) {
            throw new UsernameNotFoundException("not a valid authentication object");
        }

        String domain = split[1];
        String password = (String) authentication.getCredentials();
        Store store = null;

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imap");

            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imap");
            store.connect(domain, authentication.getName(), password);

            List<SimpleGrantedAuthority> ga = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
            authenticated = new MailAuthenticationToken(authentication.getName(), authentication.getCredentials(), ga, domain);

            // TODO
            Properties javaMailProperties = new Properties();
            javaMailProperties.setProperty("mail.store.protocol", "imap");
            // javaMailProperties.setProperty("mail.debug", "true");

            javaMailProperties.put("mail.smtp.starttls.enable", "true");
            javaMailProperties.put("mail.smtp.auth", "true");
            javaMailProperties.put("mail.imap.port", "143");
            javaMailProperties.put("mail.smtp.host", domain);

            authenticated.setConnectionProperties(javaMailProperties);
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getMessage());
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                }
            }
        }

        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
