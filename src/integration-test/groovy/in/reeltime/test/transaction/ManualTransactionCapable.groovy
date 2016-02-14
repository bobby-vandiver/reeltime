package in.reeltime.test.transaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition

trait ManualTransactionCapable {

    @Autowired
    PlatformTransactionManager transactionManager

    TransactionStatus startTransaction() {
        startTransaction([:])
    }

    TransactionStatus startTransaction(Map props) {
        def definition = new DefaultTransactionDefinition()
        props.each { key, value ->
            definition."$key" = value
        }
        return transactionManager.getTransaction(definition)
    }

    void commitTransaction(TransactionStatus status) {
        transactionManager.commit(status)
    }

    void rollbackTransaction(TransactionStatus status) {
        transactionManager.rollback(status)
    }
}