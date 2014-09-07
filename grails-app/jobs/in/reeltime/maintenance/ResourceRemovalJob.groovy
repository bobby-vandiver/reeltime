package in.reeltime.maintenance

class ResourceRemovalJob {

    static triggers = {
        cron name: 'resourceRemovalTrigger', cronExpression: '0 0 3 * * ?'
    }

    static numberToRemovePerExecution

    def description = "Removes resources scheduled for deletion every night at 3AM"
    def resourceRemovalService

    def execute() {
        log.info "Executing resource removal job"
        resourceRemovalService.executeScheduledRemovals(numberToRemovePerExecution)
    }
}
