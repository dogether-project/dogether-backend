import {removeAllTestDataCreateToday} from "../../../../../common/db/util/data-clear-runner.js";

async function tearDown() {
    await removeAllTestDataCreateToday();
}

tearDown();
