import {insertData} from "../db/util/data-insert-runner.js";

import {createSetUpData} from "../db/data/set-up-data/read-test/variable-running-activity-data.js";
// import {createSetUpData} from "../db/data/set-up-data/read-test/maximum-running-activity-data.js";

async function main() {
    await insertData(createSetUpData);
}

main();
