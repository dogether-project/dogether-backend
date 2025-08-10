import { createDummyData } from "../db/data/dummy-data/dummy-data-1.js";
// import { createDummyData } from "../db/data/dummy-data/dummy-data-2.js";

import {insertData} from "../db/util/data-insert-runner.js";

async function main() {
    await insertData(createDummyData);
}

main();
