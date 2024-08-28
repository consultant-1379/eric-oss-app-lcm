#!/bin/bash
rm -rf /etc/apache2/*
rm -rf /app/*
rm -rf /var/log/apache/*

mkdir ${APP_DIR}/log; mkdir ${APP_DIR}/log/apache2; > ${APP_DIR}/log/apache2/error_log
cp /tmp/apache2/httpd.conf /etc/apache2/httpd.conf
/usr/sbin/apachectl -D FOREGROUND