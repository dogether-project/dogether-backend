import {createCurrentActivityForWriteTestData} from "./for-write-current-activity-test-data.js";
import {insertCurrentActivityData} from "../../../util/db-util.js";

export async function insertForWriteCurrentActivityData() {
    await insertCurrentActivityData(await createCurrentActivityForWriteTestData());
}

insertForWriteCurrentActivityData().then(() => {});
