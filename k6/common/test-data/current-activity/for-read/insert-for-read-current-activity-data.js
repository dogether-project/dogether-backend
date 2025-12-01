import {createCurrentActivityForReadTestData} from "./for-read-current-activity-test-data.js";
import {insertCurrentActivityData} from "../../../util/db-util.js";

async function insertForReadCurrentActivityData() {
    await insertCurrentActivityData(createCurrentActivityForReadTestData());
}

insertForReadCurrentActivityData().then(() => {});
