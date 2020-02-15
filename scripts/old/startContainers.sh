#!/bin/bash
sudo docker rm digitalpanda-frontend-0 digitalpanda-backend-0 digitalpanda-dynDnsClient
sudo docker run -d --name digitalpanda-dynDnsClient dyndnsclient:latest
sudo docker run -d -p 192.168.0.102:5000:5000 registry
sudo docker run -d --name digitalpanda-frontend-0 -p 192.168.0.102:8000:8000 192.168.0.102:5000/jva/frontend-angular2:latest
sudo docker run -d --name digitalpanda-backend-0 -p 192.168.0.102:8080:8080 192.168.0.102:5000/jva/backend-java:lates
