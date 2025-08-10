import {insertData} from "../db/util/data-insert-runner.js";

import {createSetUpData} from "../db/data/set-up-data/read-test/read-test-set-up-data-1.js";
// import {createSetUpData} from "../db/data/set-up-data/read-test/read-test-set-up-data-2.js";

async function main() {
    await insertData(createSetUpData);
}

main();
