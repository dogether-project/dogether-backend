import {removeAllTestDataCreateToday} from "../db/util/data-clear-runner.js";

async function clear() {
    await removeAllTestDataCreateToday();
}

clear();
