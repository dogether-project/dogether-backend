import {clearAndReCreateAllTable} from "../db/util/data-clear-runner.js";

async function main() {
    await clearAndReCreateAllTable();
}

main();
