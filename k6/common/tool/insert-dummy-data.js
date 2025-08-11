import { createDummyData } from "../db/data/dummy-data/only-member-info-data.js";
// import { createDummyData } from "../db/data/dummy-data/maximum-finished-activity-data.js";

import {insertData} from "../db/util/data-insert-runner.js";

async function main() {
    await insertData(createDummyData);
}

main();
