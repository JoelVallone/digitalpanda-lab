#!/bin/bash
echo "run startup command: $@"
eval $@
echo "container started - press enter to stop"
read val

