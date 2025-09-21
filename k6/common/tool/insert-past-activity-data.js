import {insertData} from "../db/util/data-insert-runner.js";
// import {createPastActivityData} from "../db/data/past-activity/variable-past-activity-data.js";
import {createPastActivityData} from "../db/data/past-activity/const-past-activity-data.js";

async function main() {
    await insertData(createPastActivityData);
}

main();
