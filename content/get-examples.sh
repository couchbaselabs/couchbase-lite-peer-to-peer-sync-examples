#!/bin/bash


cd ../ios/
touch code-samples.swift
echo "Complete Swift code samples from which these are extracted can be found in the /ios directory at the top-level of this repo." > code-samples.swift

sed '/tag/,/end/!d' ./list-sync/list-sync/*/*.swift >> code-samples.swift

mv ./code-samples.swift ../content/modules/cbl-p2p-sync-websockets/examples/

