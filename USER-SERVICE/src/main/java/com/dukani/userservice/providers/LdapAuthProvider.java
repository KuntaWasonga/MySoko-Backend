package com.dukani.userservice.providers;

import com.dukani.userservice.dtos.LdapAuthResponse;
import com.dukani.userservice.dtos.LdapObjectDTO;
import com.dukani.userservice.exceptions.LdapException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Component
@RequiredArgsConstructor
public class LdapAuthProvider implements AuthenticationProvider {

    @Value("${ldap.server.host}")
    private String ldapUrl;

    @Value("${ldap.server.port}")
    private int ldapPort;

    @Value("${ldap.domain}")
    private String ldapDomain;

    private static final Map<String, String> ldapErrorCodeMap = Map.of(
            "525", "Active Directory User not found",
            "52e", "Invalid Active Directory credentials ",
            "49", "Invalid Active Directory credentials",
            "530", "Not permitted to logon at this time",
            "531", "Not permitted to logon from this workstation",
            "532", "Expired Active Directory Password",
            "533", "Active Directory Account disabled",
            "701", "Active Directory Account expired",
            "773", "User must reset Active Directory password",
            "775", "Active Directory Account locked");

    private static String parseLdapErrorCode(String message) {
        String patternString = "LDAP: error code (\\d+) - ([\\w\\d]+): ([^,]+), comment: ([^,]+), data (\\w+), (\\w+)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            if (ldapErrorCodeMap.entrySet().stream().anyMatch(
                    entry -> matcher.group(5) != null && entry.getKey().equalsIgnoreCase(matcher.group(5))))
                return matcher.group(5);

            if (ldapErrorCodeMap.entrySet().stream().anyMatch(
                    entry -> matcher.group(1) != null && entry.getKey().equalsIgnoreCase(matcher.group(1))))
                return matcher.group(1);

        } else {

            pattern = Pattern.compile("data\\s(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern.matcher(message);

            if (matcher2.find()) {
                return matcher2.group(1);
            }
        }
        return null;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        final String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        if (StringUtils.isEmpty(username)) {
            throw new BadCredentialsException("Invalid login credentials");
        }

        try {
            String password = authentication.getCredentials().toString();
            final LdapAuthResponse authenticationResponse = isLdapAuthenticated(username, password);

            if (authenticationResponse.isAuthenticated()) {
               LdapObjectDTO ldapObject = authenticationResponse.getLdapObject();
                log.info("**====LDAP LOGIN SUCCESS : {} {}====**", ldapObject.getFirstName(), ldapObject.getEmailAddress());

                UserDetails userDetails = User.builder()
                       .username(username)
                       .password("") // do not store password
                       .authorities(new SimpleGrantedAuthority("USER"))
                       .build();
               return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            } else {
                throw new BadCredentialsException("Invalid login credentials");
            }
        } catch (AuthenticationException | LdapException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private LdapObjectDTO searchUserDetails(String searchFilter, DirContext ctx) throws NamingException {
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setTimeLimit(0);
        sc.setCountLimit(1);
        sc.setReturningAttributes(new String[]{"*"});

        String name = "DC=" + ldapDomain + ",DC=CO,DC=KE";
        NamingEnumeration<SearchResult> results = ctx.search(name, searchFilter, sc);
        SearchResult tmp = results.next();
        Attributes at = tmp.getAttributes();

        String firstName = at.get("givenName").toString();
        String lastName = at.get("sn").toString();
        String email = at.get("mail").toString();

        return new LdapObjectDTO(
                firstName.replace("givenName:", "").trim(),
                lastName.replace("sn:", "").trim(),
                email.replace("mail:", "").trim().toLowerCase()
        );

    }

    private LdapAuthResponse isLdapAuthenticated(String username, String password) {
        final LdapObjectDTO ldapObject;

        try {
            LdapContext ctx = getLdapContext(username, password);
            String dn = "@" + ldapDomain.toLowerCase() + ".co.ke";
            ldapObject = searchUserDetails("(&((mail=" + username + dn + ")))", ctx);
            ctx.close();

            return new LdapAuthResponse(true, ldapObject);

        } catch (Exception ex) {
            String errorCode = parseLdapErrorCode(ex.getMessage());
            String reason = ldapErrorCodeMap.getOrDefault(errorCode, "Unknown authentication failure");
            log.error("{}: LDAP authentication failed: {} ({})", username, reason, errorCode, ex);

            throw new LdapException(reason, ex.getCause());
        }
    }

    private LdapContext getLdapContext(String username, String password) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://" + ldapUrl + ":" + ldapPort);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapDomain + "\\" + username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.REFERRAL, "throw");

        // Create the initial context
        return new InitialLdapContext(env, null);
    }


}
