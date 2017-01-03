CURRENT_DIR=$(pwd)
cd "${0%/*}"
./node_modules/babel-cli/bin/babel.js src/ --out-dir lib/
cd ${CURRENT_DIR}
