import {insertData} from "../db/util/data-insert-runner.js";
import {createCurrentActivityData} from "../db/data/current-activity/const-current-activity-data-for-read-api.js";

async function main() {
    await insertData(createCurrentActivityData);
}

main();
