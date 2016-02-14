package in.reeltime.springsecurity

import grails.plugin.springsecurity.InterceptedUrl
import grails.plugin.springsecurity.web.access.intercept.AnnotationFilterInvocationDefinition
import org.springframework.http.HttpMethod

import java.lang.annotation.Annotation
import java.lang.reflect.Method

/**
 * Reverts the changes made in commit 6bcf93923fc057580e6cc879070b34f14306e92c of
 * the Spring Security Core plugin.
 */
class PatchedAnnotationFilterInvocationDefinition extends AnnotationFilterInvocationDefinition {

    @Override
    protected List<InterceptedUrl> findActionRoles(Class<?> clazz) {

        log.trace 'finding @Secured annotations for actions in {}', clazz.name

        List<InterceptedUrl> actionRoles = []
        for (Method method in clazz.methods) {
            Annotation annotation = findSecuredAnnotation(method)
            if (annotation) {
                Collection<String> values = getValue(annotation)
                if (values) {
                    log.trace 'found annotated method {} in {} with value(s) {}', method.name, clazz.name, values
                    HttpMethod httpMethod = getHttpMethod(annotation)
                    actionRoles << new InterceptedUrl(grailsUrlConverter.toUrlElement(method.name), values, httpMethod)
                }
            }
        }
        actionRoles
    }
}
