package in.reeltime.common.deadlock

import com.mysql.jdbc.exceptions.DeadlockTimeoutRollbackMarker
import groovy.util.logging.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.codehaus.groovy.grails.orm.hibernate.HibernateSession
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.core.Ordered
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

import java.sql.SQLException

// Based on article:
// http://java.dzone.com/articles/automatic-deadlock-retry
//
// And example 9.2.7 in Spring AOP:
// http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/aop.html

@Aspect
@Slf4j
class RetryableOnDeadlockAspect implements Ordered {

    SessionFactory sessionFactory
    PlatformTransactionManager transactionManager

    @Override
    int getOrder() {
        return 99
    }

    @Around(value = "@annotation(retryableOnDeadlock)", argNames = "retryableOnDeadlock")
    Object concurrencyRetry(final ProceedingJoinPoint joinPoint, final RetryableOnDeadlock retryableOnDeadlock) {
        Object result = null

        final Integer maxRetries = retryableOnDeadlock.retryCount()
        Integer deadlockCounter = 0

        while(deadlockCounter < maxRetries) {
            def session = openSession()

            def definition = new DefaultTransactionDefinition(
                    propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW
            )

            try {
                joinPoint.proceed()
                commitTransaction(definition)

                // Force loop termination
                deadlockCounter = maxRetries
            }
            catch (SQLException e) {
                rollbackTransaction(definition)
                deadlockCounter = handleException(e, deadlockCounter, maxRetries)
            }
            finally {
                log.debug("Closing the Hibernate session")
                session.close()
            }
        }
        return result
    }

    private Session openSession() {
        def session = sessionFactory.openSession()
        if(!(session instanceof HibernateSession)) {
            throw new IllegalStateException("Could not get a Hibernate session!")
        }
        return session
    }

    private void commitTransaction(TransactionDefinition definition) {
        log.debug("Commiting transaction definition: ${definition}")
        def status = transactionManager.getTransaction(definition)
        transactionManager.commit(status)
    }

    private void rollbackTransaction(TransactionDefinition definition) {
        log.debug("Rolling back transaction definition: ${definition}")
        def status = transactionManager.getTransaction(definition)
        transactionManager.rollback(status)
    }

    private Integer handleException(final SQLException e, Integer deadlockCounter, final Integer maxRetries) {
        rethrowIfNotDeadlock(e)

        log.debug("Encountered deadlock: ", e)
        deadlockCounter++

        if(deadlockCounter >= maxRetries) {
            log.debug("Exceeded max retries -- rethrowing exception")
            throw e
        }

        return deadlockCounter
    }

    private void rethrowIfNotDeadlock(final SQLException e) {
        if(!(e instanceof DeadlockTimeoutRollbackMarker)) {
            log.debug("Encountered exception is not a deadlock -- rethrowing")
            throw e
        }
    }
}
