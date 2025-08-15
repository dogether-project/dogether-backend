import {insertData} from "../../../../../common/db/util/data-insert-runner.js";
import {createSetUpData} from "../../../../../common/db/data/set-up-data/write-test/review-todo-certification-v1-set-up-data.js";

async function setUp() {
    await insertData(createSetUpData);
}

setUp();
