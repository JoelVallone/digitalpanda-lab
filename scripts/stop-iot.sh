#!/bin/bash
set -x

ssh pi@192.168.0.211 sudo shutdown
ssh pi@192.168.0.212 sudo shutdown
ssh -t jva@192.168.0.102 'sudo shutdown'
