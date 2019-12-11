#!/bin/bash
set -x #echo on
name=`basename $(pwd)`
zip -r ${name}.zip src pom.xml build.sh
fission env create --name java --image fission/jvm-env --builder fission/jvm-builder --keeparchive --version 2
fission pkg delete --name ${name} -f
fission pkg create --name ${name} --sourcearchive ${name}.zip --env java
fission fn delete --name ${name}-usage
fission fn create --name ${name}-usage --pkg ${name} --fntimeout 180 --entrypoint com.lingk.faas.fission.aws.session.provider.AWSSessionTokenUsage
fission route delete --name ${name}-usage
fission route create --name ${name}-usage --url /${name}-usage --function ${name}-usage --createingress