import {insertData} from "../../../../../common/db/util/data-insert-runner.js";
import {
    createDummyData,
} from "../../../../../common/db/data/set-up-data/set-up-data-1.js"

async function setUp() {
    await insertData(createDummyData);
}

setUp();
