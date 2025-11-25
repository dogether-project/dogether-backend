import {insertData} from "../../../../common/db/util/data-insert-runner.js";
import { createCurrentActivityData } from "../../../../common/db/data/current-activity/const-current-activity-data-for-write-api.js";

async function setUp() {
    await insertData(createCurrentActivityData);
}

setUp();
