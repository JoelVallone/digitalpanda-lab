#!/usr/bin/env bash

#set -x


function processTemplate(){
    TEMPLATE_FILE=$1
    OUTPUT_FILE=$2
    VAR_NAMES_AND_VALUES=($3)

    TMP_FILE=/tmp/partial.txt

    cp ${TEMPLATE_FILE} ${OUTPUT_FILE}
    for NAME_AND_VALUE in "${VAR_NAMES_AND_VALUES[@]}"; do
        VAR_NAME=${NAME_AND_VALUE%,*}
        VAR_VALUE=${NAME_AND_VALUE#*,}
        cat ${OUTPUT_FILE} | sed "s/{{${VAR_NAME}}}/${VAR_VALUE}/g" > ${TMP_FILE}
        cp ${TMP_FILE} ${OUTPUT_FILE}
    done
    rm ${TMP_FILE}
}

#SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
#processTemplate ${SCRIPT_FOLDER}/test.txt.tmpl ${SCRIPT_FOLDER}/test.txt "name,bob program,borg"
