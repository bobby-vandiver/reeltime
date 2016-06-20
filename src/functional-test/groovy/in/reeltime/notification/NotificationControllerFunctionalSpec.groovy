package in.reeltime.notification

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec
import spock.lang.Unroll

class NotificationControllerFunctionalSpec extends FunctionalSpec {

    // TODO: Determine why PUT throws an exception:
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.FilterChainProxy - /aws/transcoder/notification at position 1 of 6 in additional filter chain; firing Filter: 'SecurityRequestHolderFilter'
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.FilterChainProxy - /aws/transcoder/notification at position 2 of 6 in additional filter chain; firing Filter: 'StatelessSecurityContextPersistenceFilter'
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.FilterChainProxy - /aws/transcoder/notification at position 3 of 6 in additional filter chain; firing Filter: 'SecurityContextHolderAwareRequestFilter'
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.FilterChainProxy - /aws/transcoder/notification at position 4 of 6 in additional filter chain; firing Filter: 'GrailsAnonymousAuthenticationFilter'
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG grails.plugin.springsecurity.web.filter.GrailsAnonymousAuthenticationFilter - Populated SecurityContextHolder with anonymous token: 'grails.plugin.springsecurity.authentication.GrailsAnonymousAuthenticationToken@dc4337e: Principal: org.springframework.security.core.userdetails.User@dc730200: Username: __grails.anonymous.user__; Password: [PROTECTED]; Enabled: false; AccountNonExpired: false; credentialsNonExpired: false; AccountNonLocked: false; Granted Authorities: ROLE_ANONYMOUS; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@957e: RemoteIpAddress: 127.0.0.1; SessionId: null; Granted Authorities: ROLE_ANONYMOUS'
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.FilterChainProxy - /aws/transcoder/notification at position 5 of 6 in additional filter chain; firing Filter: 'ExceptionTranslationFilter'
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.FilterChainProxy - /aws/transcoder/notification at position 6 of 6 in additional filter chain; firing Filter: 'FilterSecurityInterceptor'
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.access.intercept.FilterSecurityInterceptor - Secure object: FilterInvocation: URL: /aws/transcoder/notification; Attributes: [permitAll]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.access.intercept.FilterSecurityInterceptor - Previously Authenticated: grails.plugin.springsecurity.authentication.GrailsAnonymousAuthenticationToken@dc4337e: Principal: org.springframework.security.core.userdetails.User@dc730200: Username: __grails.anonymous.user__; Password: [PROTECTED]; Enabled: false; AccountNonExpired: false; credentialsNonExpired: false; AccountNonLocked: false; Granted Authorities: ROLE_ANONYMOUS; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@957e: RemoteIpAddress: 127.0.0.1; SessionId: null; Granted Authorities: ROLE_ANONYMOUS
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl - getReachableGrantedAuthorities() - From the roles [ROLE_ANONYMOUS] one can reach [ROLE_ANONYMOUS] in zero or more steps.
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.access.intercept.FilterSecurityInterceptor - Authorization successful
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.access.intercept.FilterSecurityInterceptor - RunAsManager did not change Authentication object
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG org.springframework.security.web.FilterChainProxy - /aws/transcoder/notification reached end of additional filter chain; proceeding with original chain
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG grails.plugin.springsecurity.oauthprovider.endpoint.PriorityOrderedFrameworkEndpointHandlerMapping - Looking up handler method for path /aws/transcoder/notification
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG grails.plugin.springsecurity.oauthprovider.endpoint.PriorityOrderedFrameworkEndpointHandlerMapping - Did not find handler method for [/aws/transcoder/notification]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG grails.plugin.springsecurity.oauthprovider.servlet.OAuth2AuthorizationEndpointExceptionResolver - Resolving exception from handler [org.grails.web.mapping.mvc.GrailsControllerUrlMappingInfo@64136803]: java.lang.reflect.InvocationTargetException
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG grails.plugin.springsecurity.oauthprovider.servlet.OAuth2AuthorizationEndpointExceptionResolver - Entering authorization endpoint exception resolver
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG grails.plugin.springsecurity.oauthprovider.servlet.OAuth2TokenEndpointExceptionResolver - Resolving exception from handler [org.grails.web.mapping.mvc.GrailsControllerUrlMappingInfo@64136803]: java.lang.reflect.InvocationTargetException
    // [Mon Jun 20 2016 00:01:21] [SERVER] - DEBUG grails.plugin.springsecurity.oauthprovider.servlet.OAuth2TokenEndpointExceptionResolver - Entering token endpoint exception resolver
    // [Mon Jun 20 2016 00:01:21] [SERVER] - ERROR org.grails.web.errors.GrailsExceptionResolver - IllegalStateException occurred when processing request: [PUT] /aws/transcoder/notification
    // [Mon Jun 20 2016 00:01:21] [SERVER] - getReader() has already been called for this request. Stacktrace follows:
    // [Mon Jun 20 2016 00:01:21] [SERVER] - java.lang.reflect.InvocationTargetException: null
    // [Mon Jun 20 2016 00:01:21] [SERVER] - 	at grails.plugin.springsecurity.web.filter.GrailsAnonymousAuthenticationFilter.doFilter(GrailsAnonymousAuthenticationFilter.groovy:53) ~[spring-security-core-3.0.0.jar!/:na]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - 	at grails.plugin.springsecurity.web.SecurityRequestHolderFilter.doFilter(SecurityRequestHolderFilter.groovy:58) ~[spring-security-core-3.0.0.jar!/:na]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) ~[na:1.8.0_65]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) ~[na:1.8.0_65]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - 	at java.lang.Thread.run(Thread.java:745) [na:1.8.0_65]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - Caused by: java.lang.IllegalStateException: getReader() has already been called for this request
    // [Mon Jun 20 2016 00:01:21] [SERVER] - 	at in.reeltime.notification.NotificationController.handleMessage(NotificationController.groovy:20) ~[classes!/:na]
    // [Mon Jun 20 2016 00:01:21] [SERVER] - 	... 5 common frames omitted

    @Unroll
    void "invalid http method [#method]"() {
        given:
        def request = new RestRequest(url: urlFactory.notificationUrl)

        when:
        def response = "$method"(request)

        then:
        response.status == 400
        response.body == ''

        where:
        method << ['get', /*'put',*/ 'delete']
    }
}
