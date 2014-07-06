
includeTargets << new File("${basedir}/scripts/_DeployWar.groovy")

target(deploy: "Deploys the application to AWS and sets up any necessary resources") {
    depends(deployWar)
}

setDefaultTarget(deploy)