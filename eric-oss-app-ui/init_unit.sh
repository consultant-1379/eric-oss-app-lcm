#!/bin/bash
npm run test &
BACK_PID=$!

wait $BACK_PID

chmod 777 /eui_client/coverage/lcov.info
cp /eui_client/coverage/lcov.info /eui_client/coverage-results/lcov.info
